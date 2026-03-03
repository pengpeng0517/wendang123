import argparse
import json
from pathlib import Path
from datetime import datetime

import torch

from train_dqn import QNet


def export(args):
    state_dim = 16
    action_dim = args.actions

    model = QNet(state_dim, action_dim)
    model.load_state_dict(torch.load(args.checkpoint, map_location='cpu'))
    model.eval()

    wrapper = torch.nn.Sequential(model, torch.nn.Linear(action_dim, 1, bias=False))
    with torch.no_grad():
        wrapper[1].weight.fill_(1.0 / max(1, action_dim))

    out_dir = Path(args.output_dir) / args.version
    out_dir.mkdir(parents=True, exist_ok=True)

    onnx_path = out_dir / 'model.onnx'
    dummy = torch.zeros(1, state_dim, dtype=torch.float32)
    torch.onnx.export(
        wrapper,
        dummy,
        onnx_path.as_posix(),
        input_names=['input'],
        output_names=['score'],
        dynamic_axes=None,
        opset_version=13,
    )

    metadata = {
        'version': args.version,
        'algo': 'dqn',
        'feature_schema_hash': args.feature_schema_hash,
        'train_time': datetime.utcnow().isoformat() + 'Z',
        'metrics': {
            'note': 'fill with evaluate.py output in CI pipeline'
        },
        'normalization_params': {
            'state_dim': state_dim
        },
        'action_space_map': [f'LOC-{i:03d}' for i in range(action_dim)]
    }

    with open(out_dir / 'metadata.json', 'w', encoding='utf-8') as f:
        json.dump(metadata, f, ensure_ascii=False, indent=2)

    print('exported:', onnx_path)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--checkpoint', type=str, required=True)
    parser.add_argument('--output-dir', type=str, default='models/location-rl')
    parser.add_argument('--version', type=str, default='dqn-v1')
    parser.add_argument('--actions', type=int, default=20)
    parser.add_argument('--feature-schema-hash', type=str, default='v1')
    export(parser.parse_args())
