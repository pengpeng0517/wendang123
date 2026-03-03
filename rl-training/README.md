# RL Training Workspace

This directory is for offline RL training only.

## Structure
- `env/warehouse_env.py`: Gymnasium environment for dynamic location assignment.
- `train_dqn.py`: DQN training entry.
- `evaluate.py`: Offline evaluation script.
- `export_onnx.py`: Export PyTorch model to ONNX + metadata.
- `feature_schema.json`: Feature contract used by Java `FeatureEncoder`.

## Quick Start
1. Create python env and install dependencies.
2. Run `python train_dqn.py --episodes 5000`.
3. Run `python evaluate.py --checkpoint models/dqn_latest.pt`.
4. Run `python export_onnx.py --checkpoint models/dqn_latest.pt --version dqn-v1`.

## Notes
- Do not train on production server.
- Exported model package should be copied to:
  `/opt/wendang123/models/location-rl/{version}/`
