import argparse
import json
from pathlib import Path

import numpy as np
import torch

from env.warehouse_env import WarehouseLocationEnv
from train_dqn import QNet


def evaluate(args):
    env = WarehouseLocationEnv(n_locations=args.actions, seed=args.seed)
    state_dim = env.observation_space.shape[0]
    action_dim = env.action_space.n

    model = QNet(state_dim, action_dim)
    model.load_state_dict(torch.load(args.checkpoint, map_location='cpu'))
    model.eval()

    reward_sum = 0.0
    overflow_count = 0

    for _ in range(args.episodes):
        state, info = env.reset()
        with torch.no_grad():
            q = model(torch.tensor(state).float().unsqueeze(0))
            action = int(torch.argmax(q, dim=1).item())
        _, reward, _, _, step_info = env.step(action)
        reward_sum += reward
        if step_info['reward_detail']['overflow_penalty'] > 0:
            overflow_count += 1

    metrics = {
        'episodes': args.episodes,
        'reward_mean': reward_sum / max(1, args.episodes),
        'overflow_rate': overflow_count / max(1, args.episodes),
    }

    print(json.dumps(metrics, ensure_ascii=False, indent=2))

    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    with open(output, 'w', encoding='utf-8') as f:
        json.dump(metrics, f, ensure_ascii=False, indent=2)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--checkpoint', type=str, required=True)
    parser.add_argument('--output', type=str, default='models/eval_metrics.json')
    parser.add_argument('--episodes', type=int, default=500)
    parser.add_argument('--actions', type=int, default=20)
    parser.add_argument('--seed', type=int, default=42)
    evaluate(parser.parse_args())
