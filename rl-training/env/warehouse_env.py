import math
import random
from dataclasses import dataclass
from typing import Dict, List, Tuple

import gymnasium as gym
import numpy as np


@dataclass
class Location:
    code: str
    zone: str
    capacity: int
    current_load: int
    priority: int
    temperature_level: str


class WarehouseLocationEnv(gym.Env):
    metadata = {"render_modes": []}

    def __init__(self, n_locations: int = 20, seed: int = 42):
        super().__init__()
        self._rng = random.Random(seed)
        self._np_rng = np.random.default_rng(seed)
        self.n_locations = n_locations
        self.locations: List[Location] = []
        self.current_state: Dict = {}

        self.observation_space = gym.spaces.Box(
            low=-10.0,
            high=10.0,
            shape=(16,),
            dtype=np.float32,
        )
        self.action_space = gym.spaces.Discrete(n_locations)

    def reset(self, *, seed=None, options=None):
        if seed is not None:
            self._rng.seed(seed)
            self._np_rng = np.random.default_rng(seed)

        self.locations = self._build_locations()
        self.current_state = self._sample_request()
        obs = self._encode(self.current_state, self.locations[0], 0)
        info = {"request": self.current_state, "locations": self.locations}
        return obs, info

    def step(self, action: int):
        action = int(action)
        if action < 0 or action >= len(self.locations):
            action = 0

        request = self.current_state
        selected = self.locations[action]
        reward, detail = self._reward(request, selected)

        inbound_qty = request["inbound_qty"]
        selected.current_load += inbound_qty
        if selected.current_load > selected.capacity:
            selected.current_load = selected.capacity

        done = True
        truncated = False
        next_state = self._sample_request()
        next_obs = self._encode(next_state, self.locations[0], 0)
        self.current_state = next_state
        info = {"reward_detail": detail, "request": request, "selected": selected.code}
        return next_obs, reward, done, truncated, info

    def _build_locations(self) -> List[Location]:
        locations = []
        zones = ["A区", "B区", "C区"]
        temps = ["常温", "低温"]
        for i in range(self.n_locations):
            zone = zones[i % len(zones)]
            capacity = self._rng.randint(400, 1500)
            current_load = self._rng.randint(0, int(capacity * 0.8))
            priority = self._rng.randint(1, 10)
            temp = temps[self._rng.randint(0, 1)]
            code = f"{chr(65 + (i % 3))}-{(i // 10) + 1:02d}-{(i % 10) + 1:02d}"
            locations.append(Location(code, zone, capacity, current_load, priority, temp))
        return locations

    def _sample_request(self) -> Dict:
        return {
            "material_id": self._rng.randint(1, 5000),
            "inbound_qty": self._rng.randint(20, 300),
            "season_tag": self._rng.choice(["旺季", "淡季", "平季"]),
            "temperature_level": self._rng.choice(["常温", "低温"]),
            "preferred_location": self._rng.choice(self.locations).code if self.locations else None,
        }

    def _encode(self, request: Dict, location: Location, rank: int) -> np.ndarray:
        inbound_qty = request["inbound_qty"]
        available = location.capacity - location.current_load
        available_ratio = available / max(1, location.capacity)
        load_ratio = location.current_load / max(1, location.capacity)
        post_load_ratio = (location.current_load + inbound_qty) / max(1, location.capacity)

        zone_level = 1.0 if location.zone.startswith("A") else 0.7 if location.zone.startswith("B") else 0.45
        temp_match = 1.0 if location.temperature_level == request["temperature_level"] else 0.0
        preferred_match = 1.0 if location.code == request["preferred_location"] else 0.0

        vec = np.array(
            [
                min(1.0, inbound_qty / 1000.0),
                min(1.0, location.capacity / 2000.0),
                min(1.0, location.current_load / 2000.0),
                available_ratio,
                load_ratio,
                post_load_ratio,
                zone_level,
                location.priority / 10.0,
                temp_match,
                preferred_match,
                1.0 if request["season_tag"] == "旺季" else 0.0,
                1.0 if request["season_tag"] == "淡季" else 0.0,
                (request["material_id"] % 1024) / 1024.0,
                min(1.0, len(self.locations) / 200.0),
                min(1.0, rank / 100.0),
                1.0 if available >= inbound_qty else 0.0,
            ],
            dtype=np.float32,
        )
        return vec

    def _reward(self, request: Dict, selected: Location) -> Tuple[float, Dict]:
        inbound = request["inbound_qty"]
        available = selected.capacity - selected.current_load
        post_ratio = (selected.current_load + inbound) / max(1, selected.capacity)

        capacity_balance = max(0.0, 1.0 - abs(0.65 - post_ratio))
        pick_cost_negative = 1.0 if selected.zone.startswith("A") else 0.75 if selected.zone.startswith("B") else 0.55
        temp_match = 1.0 if selected.temperature_level == request["temperature_level"] else 0.0
        stability = 1.0 if selected.code == request["preferred_location"] else 0.0
        overflow_penalty = max(0.0, inbound - available) / max(1, inbound)

        reward = (
            0.35 * capacity_balance
            + 0.25 * pick_cost_negative
            + 0.2 * temp_match
            + 0.1 * stability
            - 0.1 * overflow_penalty
        )

        return float(reward), {
            "capacity_balance": capacity_balance,
            "pick_cost_negative": pick_cost_negative,
            "temperature_match": temp_match,
            "stability": stability,
            "overflow_penalty": overflow_penalty,
        }
