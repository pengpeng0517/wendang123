import argparse
import json
from collections import deque
from pathlib import Path

import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim

from env.warehouse_env import WarehouseLocationEnv


class QNet(nn.Module):
    def __init__(self, state_dim: int, action_dim: int):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(state_dim, 128),
            nn.ReLU(),
            nn.Linear(128, 128),
            nn.ReLU(),
            nn.Linear(128, action_dim),
        )

    def forward(self, x):
        return self.net(x)


def train(args):
    env = WarehouseLocationEnv(n_locations=args.actions, seed=args.seed)
    state_dim = env.observation_space.shape[0]
    action_dim = env.action_space.n

    policy = QNet(state_dim, action_dim)
    target = QNet(state_dim, action_dim)
    target.load_state_dict(policy.state_dict())

    optimizer = optim.Adam(policy.parameters(), lr=args.lr)
    memory = deque(maxlen=args.buffer_size)

    epsilon = args.epsilon_start
    gamma = args.gamma

    rewards = []

    for episode in range(args.episodes):
        state, info = env.reset()
        done = False
        total_reward = 0.0

        while not done:
            if np.random.rand() < epsilon:
                action = np.random.randint(0, action_dim)
            else:
                with torch.no_grad():
                    q = policy(torch.tensor(state).float().unsqueeze(0))
                    action = int(torch.argmax(q, dim=1).item())

            next_state, reward, done, truncated, step_info = env.step(action)
            memory.append((state, action, reward, next_state, done))

            state = next_state
            total_reward += reward

            if len(memory) >= args.batch_size:
                batch_idx = np.random.choice(len(memory), args.batch_size, replace=False)
                batch = [memory[i] for i in batch_idx]

                states = torch.tensor(np.array([b[0] for b in batch]), dtype=torch.float32)
                actions = torch.tensor([b[1] for b in batch], dtype=torch.long)
                rewards_t = torch.tensor([b[2] for b in batch], dtype=torch.float32)
                next_states = torch.tensor(np.array([b[3] for b in batch]), dtype=torch.float32)
                dones = torch.tensor([b[4] for b in batch], dtype=torch.float32)

                q_values = policy(states).gather(1, actions.unsqueeze(1)).squeeze(1)
                with torch.no_grad():
                    next_q = target(next_states).max(dim=1)[0]
                    expected = rewards_t + gamma * next_q * (1.0 - dones)

                loss = nn.functional.mse_loss(q_values, expected)
                optimizer.zero_grad()
                loss.backward()
                optimizer.step()

        rewards.append(total_reward)

        epsilon = max(args.epsilon_end, epsilon * args.epsilon_decay)
        if episode % args.target_update == 0:
            target.load_state_dict(policy.state_dict())

        if episode % 100 == 0:
            print(f'episode={episode} reward={total_reward:.4f} epsilon={epsilon:.4f}')

    model_dir = Path(args.output_dir)
    model_dir.mkdir(parents=True, exist_ok=True)
    checkpoint = model_dir / 'dqn_latest.pt'
    torch.save(policy.state_dict(), checkpoint)

    metrics = {
        'episodes': args.episodes,
        'reward_mean_last_100': float(np.mean(rewards[-100:])) if rewards else 0.0,
        'reward_mean_all': float(np.mean(rewards)) if rewards else 0.0,
    }
    with open(model_dir / 'train_metrics.json', 'w', encoding='utf-8') as f:
        json.dump(metrics, f, ensure_ascii=False, indent=2)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--episodes', type=int, default=3000)
    parser.add_argument('--actions', type=int, default=20)
    parser.add_argument('--seed', type=int, default=42)
    parser.add_argument('--lr', type=float, default=1e-3)
    parser.add_argument('--gamma', type=float, default=0.99)
    parser.add_argument('--epsilon-start', type=float, default=1.0)
    parser.add_argument('--epsilon-end', type=float, default=0.05)
    parser.add_argument('--epsilon-decay', type=float, default=0.995)
    parser.add_argument('--batch-size', type=int, default=64)
    parser.add_argument('--buffer-size', type=int, default=20000)
    parser.add_argument('--target-update', type=int, default=20)
    parser.add_argument('--output-dir', type=str, default='models')
    train(parser.parse_args())
