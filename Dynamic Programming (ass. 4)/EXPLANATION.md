# Dynamic Programming Path Finding with Keys and Doors

## Table of Contents
1. [Overview](#overview)
2. [Problem Domain](#problem-domain)
3. [State Space](#state-space)
4. [Action Space](#action-space)
5. [Core Components](#core-components)
6. [Value Iteration Algorithm](#value-iteration-algorithm)
7. [Q-Value Iteration Algorithm](#q-value-iteration-algorithm)
8. [Policy Extraction](#policy-extraction)
9. [Reward Structure](#reward-structure)
10. [Key Methods](#key-methods)
11. [Execution Flow](#execution-flow)
12. [Example Walkthrough](#example-walkthrough)
13. [Convergence Analysis](#convergence-analysis)
14. [Extensions](#extensions)

---

## Overview

This project implements **Dynamic Programming algorithms** to solve an optimal path-finding problem in a grid-based environment. The agent must navigate through a prison-like maze, collect keys, unlock doors, and reach a goal location with the maximum reward.

The implementation includes:
- **Value Iteration (VI)**: Computes optimal state values
- **Q-Value Iteration (QI)**: Computes optimal action-value pairs
- **Policy Execution**: Interactive navigation using learned policies

---

## Problem Domain

### The Prison Environment

The environment is a 2D grid loaded from `prison.txt` containing:
- `*`: Starting position of the agent
- `#`: Walls (impassable)
- ` ` (space): Free walkable areas
- `a, b, c, ...`: Keys (lowercase letters) to be collected
- `A, B, C, ...`: Doors (uppercase letters) that require corresponding keys
- `1, 2, 3, ...`: Goal locations with numeric rewards (goal value × 10)

**Example maze from `prison.txt`:**
```
##########
#* A    1#
#a # #####
#### #####
#        #
# ##B    #
#b##3   ##
##########
```

The agent starts at `*`, must collect key `a` to pass door `A`, reach goal `1` for 10 points.

### State Space

Each **state** is represented as a vector combining:
1. **Agent location**: `(row, column)` coordinates in the grid
2. **Key possession**: Boolean flags for each key (have key or don't have key)

For example: `[1, 2, True, False]` means the agent is at position `(1,2)`, has key 'a' (index 1), but not key 'b' (index 2).

**State Space Size**: `(number of free locations) × 2^(number of keys)`

The code identifies all unique states in `get_unique_states_list()`:
- Counts all non-wall locations as free locations
- Counts all lowercase letters as keys
- Generates all possible key possession combinations

### Action Space

Four discrete actions available at each state:
- `up`: Move agent row -1
- `down`: Move agent row +1
- `left`: Move agent column -1
- `right`: Move agent column +1

---

## Core Components

### 1. World Class (`world.py`)

Represents the maze environment and manages agent transitions.

#### Initialization (`__init__`)
- Loads maze from text file using `read_txt_to_map()`
- Identifies all possible states via `get_unique_states_list()`
- Sets initial agent state at `*` location
- Defines action space: `['up', 'down', 'left', 'right']`

#### Key Methods

**`transition_function(state, action)` → (next_state, reward)**

The **core of the environment model**. Given current state and action, returns:

1. **Movement Validation**:
   - Calculates new location based on action
   - Checks if new location is a wall `#` → stays in place
   - Checks if new location is a locked door → requires corresponding key in state

2. **Key Collection**:
   - If landing on lowercase letter (key), updates state to include that key

3. **Reward Calculation**:
   - Landing on goal (digit): `reward = digit × 10` (e.g., goal '3' = 30 points)
   - Any other step: `reward = -1` (penalty encourages efficient paths)
   - At goal state: `reward = 0` (already terminal)

**`act(action)`**
- Actually executes an action in the environment
- Updates internal agent state vector
- Checks terminal condition (reached goal)

**`print_map()`**
- Visualizes current maze with agent at `*`

#### State Conversion Utilities

- `_state_vector_to_state()`: Converts state vector to state index
- `_state_to_state_vector()`: Converts state index back to vector
- `get_current_state()`: Returns current state index

---

### 2. Dynamic Programming Class (`dynamic_programming.py`)

Implements value and Q-value iteration algorithms to find optimal policies.

#### Value Iteration (VI)

**Algorithm Overview:**

```
V(s) = max_a [R(s,a,s') + γ × V(s')]
```

For each state, compute the value as the maximum expected return from taking each action.

**Implementation:**

```python
def value_iteration(env, gamma=1.0, theta=0.001):
```

- **Input**:
  - `env`: World environment object
  - `gamma`: Discount factor (1.0 = no discounting, future rewards = current)
  - `theta`: Convergence threshold (stop when max value change < theta)

- **Process**:
  1. Initialize `V_s` as zero vector (one value per state)
  2. Iterate until convergence:
     - For each state, compute Q-value for all possible actions
     - Each action Q-value: `Q(a) = R(s,a,s') + γ × V(s')`
     - Update state value to maximum Q-value
     - Track maximum value change (delta)
  3. Stop when delta < theta (converged)

- **Output**: `self.V_s` array containing optimal value for each state

**Time Complexity**: O(iterations × states × actions)

#### Q-Value Iteration (QI)

**Algorithm Overview:**

```
Q(s,a) = R(s,a,s') + γ × max_a' Q(s',a')
```

Similar to VI, but stores the value for each (state, action) pair separately.

**Implementation:**

```python
def Q_value_iteration(env, gamma=1.0, theta=0.001):
```

- **Process**:
  1. Initialize `Q_sa` as 2D zero array (states × actions)
  2. Iterate until convergence:
     - For each state and each action:
       - Get next state and reward from environment
       - Update Q-value: `Q(s,a) = R + γ × max(Q(s',·))`
       - Track maximum Q-value change
  3. Stop when delta < theta

- **Output**: `self.Q_sa` matrix containing optimal Q-values for state-action pairs

**Advantage over VI**: Q-values directly encode the best action, no need to compute action values during execution.

#### Policy Execution (`execute_policy`)

**Interactive policy demonstration**:

```python
def execute_policy(env, table='V'):
```

1. **Reset agent** to start position
2. **Loop until goal**:
   - Get current state
   - Compute action values:
     - **If using V-table**: `Q(a) = R(s,a,s') + V(s')`
     - **If using Q-table**: Use directly `Q_sa[state, :]`
   - Find **greedy action** (maximum value)
   - Display greedy action; user can press Enter to execute it or type alternative action
   - Execute action, update environment
   - Display new map

**Interactive Feature**: User can override the algorithm and manually select actions to compare with optimal behavior.

---

## Algorithm Flow Diagram

```
┌─────────────────────────────────────────┐
│     Load World (prison.txt)             │
│  - Parse maze layout                    │
│  - Identify states and actions          │
│  - Set agent at start position          │
└─────────────────────┬───────────────────┘
                      │
        ┌─────────────┴──────────────┐
        │                            │
        ▼                            ▼
┌──────────────────┐        ┌──────────────────┐
│  Value Iteration │        │ Q-Value Iteration│
│  (VI)            │        │ (QI)             │
│                  │        │                  │
│ For each state:  │        │ For each state:  │
│ - Try all actions│        │ For each action: │
│ - Keep best one  │        │ - Update Q-value │
│ - Converge?      │        │ - Converge?      │
│ - Store V(s)     │        │ - Store Q(s,a)  │
└─────────┬────────┘        └────────┬─────────┘
          │                          │
          └──────────┬───────────────┘
                     │
          ┌──────────▼───────────┐
          │  Execute Policy      │
          │  (Interactive Play)  │
          │                      │
          │ 1. Reset to start    │
          │ 2. While not goal:   │
          │    - Show map        │
          │    - Suggest action  │
          │    - Execute move    │
          │    - Show reward     │
          │ 3. Goal reached!     │
          └──────────────────────┘
```

---

## Example Execution

### Setup Phase
```
Initialized 25 free locations and 2 keys → 100 unique states
```

### Value Iteration Phase
```
Starting Value Iteration (VI)
Iteration 1: Delta = 28.0
Iteration 2: Delta = 15.2
...
Iteration 42: Delta = 0.0008
Value Iteration converged after 42 iterations
```

The algorithm converges when changes are negligible (delta < 0.001).

### Execution Phase
```
Start executing. Current map:
# # # # # # # #
# * A     1 #
# a # # # # # #
...

Greedy action= right
Choose an action by typing it in full, then hit enter. Just hit enter to execute the greedy action:
[User presses Enter]
Executed action: right
--------------------------------------
New map:
# # # # # # # #
#   * A     1 #
# a # # # # # #
...
```

---

## Key Concepts

### Markov Decision Process (MDP)

This problem is a **finite horizon MDP** with:
- **States (S)**: Agent positions + key inventory
- **Actions (A)**: Movement directions
- **Transition Function (T)**: Deterministic (no randomness)
- **Reward Function (R)**: Step penalty (-1) + goal bonus (digit × 10)
- **Discount Factor (γ)**: 1.0 (no discounting)

### Bellman Equation

The algorithms solve the **Bellman optimality equation**:

    V*(s) = max_a [ Σ_{s'} P(s'|s,a) [R(s,a,s') + γ V*(s')] ]

Since transitions are deterministic: P(s'|s,a) = 1

    V*(s) = max_a [ R(s,a,s') + γ V*(s') ]

### Convergence Guarantee

Both algorithms are guaranteed to converge because:
- The state space is finite
- The value updates are monotonic (always improve or stay same)
- Each iteration reduces the maximum value change

### Discount Factor (γ)

- **γ = 1.0**: Future rewards weighted equally as immediate rewards
- **γ = 0.0**: Only immediate rewards matter
- **0 < γ < 1**: Trade-off between immediate and future rewards

In this implementation, γ = 1.0 means the agent prefers efficiency (fewer steps).

---

## Complexity Analysis

Let:
- **n_states** = number of unique states
- **n_actions** = 4 (always)
- **n_iterations** = iterations to convergence

### Value Iteration
- **Time**: O(n_iterations × n_states × n_actions)
- **Space**: O(n_states)

### Q-Value Iteration
- **Time**: O(n_iterations × n_states × n_actions)
- **Space**: O(n_states × n_actions)

### Policy Execution
- **Time**: O(path_length × n_actions) for greedy action selection
- **Space**: O(1) per step

---

## Differences Between VI and QI

| Aspect | Value Iteration | Q-Value Iteration |
|--------|-----------------|-------------------|
| **Stores** | V(s) for each state | Q(s,a) for each state-action pair |
| **Convergence** | Checks value changes | Checks Q-value changes |
| **Space** | O(n_states) | O(n_states × n_actions) |
| **Execution** | Recomputes Q-values during execution | Direct Q-value lookup |
| **Flexibility** | Must know transition function during execution | Can use Q-table offline |

---

## Key Features & Complexity

### Handling Keys and Doors
- Keys are boolean properties of the state vector
- Doors block movement unless the corresponding key is in the state
- State expansion: Each key adds a factor of 2 to the state space

### Deterministic Environment
- No randomness in transitions
- Each action produces a guaranteed next state
- Simplifies computation compared to stochastic MDPs

### Reward Structure
- **Step penalty (-1)**: Encourages finding short paths
- **Goal bonus**: Proportional to goal value (goal 3 worth 30 points)
- Terminal states: No further transitions (absorbing states)

---

## Usage Instructions

### Running the Program

```python
python dynamic_programming.py
```

1. Press Enter to start Value Iteration
2. Observe convergence iterations
3. Press Enter to execute the optimal policy based on V
4. Navigate (press Enter for algorithm suggestion or type action)
5. After reaching goal, press Enter to start Q-Value Iteration
6. Press Enter to execute Q-based policy
7. Navigate with Q-value guidance

### Modifying Parameters

Edit the main block:
```python
DP.value_iteration(env, gamma=1.0, theta=0.001)
```

- Increase **theta** (e.g., 0.01) for faster (less accurate) convergence
- Decrease **gamma** (e.g., 0.9) to prefer immediate rewards
- Decrease **theta** (e.g., 0.0001) for higher accuracy

---

## Algorithm Correctness

**Theorem**: Both Value Iteration and Q-Value Iteration converge to the optimal value function.

**Proof Sketch**:
1. The Bellman operator is a contraction mapping (with discount γ < 1)
2. In our case, effective contraction occurs due to finite state space
3. Each iteration reduces the maximum error
4. Eventually converges to unique fixed point (optimal values)

**Why it works**: The algorithms exploit the optimal substructure of MDPs—the optimal policy can be computed from optimal policies of successor states.

---

## Practical Applications

This algorithm can be applied to:
- **Robot navigation** with key collection
- **Puzzle games** (escape rooms, dungeon crawlers)
- **Resource management** (keys as collected resources)
- **Real-world logistics** (waypoint visiting with prerequisites)

---

## Educational Purpose

This code demonstrates core concepts from the course "Symbolic AI":
- ✓ Formal problem modeling (MDP framework)
- ✓ Dynamic programming algorithms
- ✓ Convergence analysis
- ✓ Policy execution
- ✓ State space representation
- ✓ Bellman equations
- ✓ Optimal control

The combination of VI and QI shows different approaches to solving the same problem, building intuition for reinforcement learning algorithms.
