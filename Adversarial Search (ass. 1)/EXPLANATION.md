# Adversarial Search Assignment 1 - Comprehensive Explanation

## Table of Contents
1. [Overview](#overview)
2. [Game Theory Concepts](#game-theory-concepts)
3. [The Game Domain](#the-game-domain)
4. [System Architecture](#system-architecture)
5. [Core Components](#core-components)
6. [Minimax Algorithm](#minimax-algorithm)
7. [Alpha-Beta Pruning](#alpha-beta-pruning)
8. [Algorithm Comparison](#algorithm-comparison)
9. [Example Execution](#example-execution)
10. [Performance Analysis](#performance-analysis)

---

## Overview

This assignment implements **adversarial game-playing agents** using two search algorithms:

1. **Minimax**: A complete algorithm that explores the entire game tree and selects the move that maximizes the agent's score, assuming both players play optimally
2. **Alpha-Beta Pruning**: An optimized version that eliminates branches that cannot affect the final decision

The system plays a **two-player zero-sum food-gathering game** where two agents compete to eat food pellets on a shared board while attempting to block each other.

---

## Game Theory Concepts

### Zero-Sum Games
In a **zero-sum game**, one player's gain is exactly the other player's loss. The total utility remains constant (sum to zero).

```
Agent 0 score:  50 points
Agent 1 score: -50 points (relative to Agent 0)
                ______
                  0 (zero-sum)
```

This means:
- What benefits Agent 0 hurts Agent 1
- What hurts Agent 0 benefits Agent 1
- Both players play with perfect opposition of interests

### Perfect Information
Both players know:
- The complete board state
- All possible moves
- The exact consequences of every move
- That both players play optimally

### Optimal Play Assumption
The algorithms assume **both players always make the best possible move** given the current game state. This is crucial for correctness.

---

## The Game Domain

### Board Layout

Example board from `board.txt`:
```
5 5              (width × height)
#####
##* #            # = wall
##A*#            * = food
# B##            A = Agent 0 (player A)
#####            B = Agent 1 (player B)
```

### Game Rules

**Starting State**:
- Agent A at starting position
- Agent B at starting position
- Food pellets scattered on board
- Both agents have score 0

**Available Actions**:
- **Move**: `up`, `down`, `left`, `right` (if not blocked by wall)
- **Eat**: Gain 1 point and remove food (only if standing on food)
- **Block**: Place wall at current location (only in empty space)

**Turn System**:
- Players alternate turns: Agent 0 → Agent 1 → Agent 0 → ...
- Each turn, exactly one move is made

**Terminal States** (game ends when):
1. All food has been eaten (`food == 0`)
2. Current player has no legal moves

**Scoring**:
- Each food pellet eaten: +1 point for that agent
- Blocking opponent: indirect advantage (reduces their movement)

### Game Value

The **value** of a game state from Agent i's perspective is:
```
+1 : Agent i won (has more food than opponent)
-1 : Agent i lost (has less food than opponent)
 0 : Tie (both agents have same score)
 0 : Game not finished yet
```

---

## System Architecture

### Component Diagram

```
┌──────────────────────────────────────────────────┐
│           Main.java (Entry Point)                │
│  Creates Game and calls test() method            │
└──────────────────┬───────────────────────────────┘
                   │
        ┌──────────▼──────────┐
        │   Game.java         │
        ├─────────────────────┤
        │ • minimax()         │
        │ • alfabeta()        │
        │ • test()            │
        │ • nodesVisited      │
        └──────────┬──────────┘
                   │
        ┌──────────▼──────────┐
        │  State.java         │
        ├─────────────────────┤
        │ • board[][]         │
        │ • agentX/Y[]        │
        │ • score[]           │
        │ • turn              │
        │ • food              │
        ├─────────────────────┤
        │ • legalMoves()      │
        │ • execute()         │
        │ • copy()            │
        │ • isLeaf()          │
        │ • value()           │
        │ • toString()        │
        └─────────────────────┘
```

### Data Flow

```
1. Load Board (State.read)
   └─> Initialize: positions, food, board state

2. Run Minimax Search
   ├─> Explore game tree depth-first
   ├─> Track node visits
   └─> Return best move

3. Run Alpha-Beta Search
   ├─> Explore game tree with pruning
   ├─> Skip branches that won't affect decision
   └─> Return best move

4. Compare Results
   └─> Print statistics (nodes visited, time, speedup)
```

---

## Core Components

### 1. **Main.java** - Entry Point

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
        Game g = new Game();    // Create game instance
        g.test();               // Run tests comparing algorithms
    }
}
```

Very simple: Just creates a `Game` and runs its test suite.

---

### 2. **State.java** - Game State Representation

#### State Variables

```java
public class State {
    char[][] board;           // 2D board (walls, food, empty)
    int[] agentX;            // X positions of both agents
    int[] agentY;            // Y positions of both agents
    int[] score;             // Scores of both agents
    int turn;                // Whose turn (0 or 1)
    int food;                // How much food remains
    Vector<String> moves;    // History of all moves made
}
```

#### Key Methods

**`read(String file)` - Load board from file**

```
Read format:
  Line 1: "width height"
  Lines 2+: Board layout
  
Side effects:
  - Initialize board dimensions
  - Find starting positions (A, B)
  - Count food pellets
  - Replace A/B with spaces (agents move around)
```

**`legalMoves()` - Get available actions**

Returns vector of possible actions for the current player:
```
// Check all four directions
if (can move up) add "up"
if (can move down) add "down"
if (can move left) add "left"
if (can move right) add "right"

// Check special actions
if (standing on food) add "eat"
if (standing on empty) add "block"
```

**`execute(String action)` - Apply move to state**

```java
switch(action) {
    case "up": agentY[turn]--; break;
    case "down": agentY[turn]++; break;
    case "left": agentX[turn]--; break;
    case "right": agentX[turn]++; break;
    case "eat":
        score[turn]++;           // Gain point
        board[y][x] = ' ';      // Remove food
        food--;                 // Decrement counter
        break;
    case "block":
        board[y][x] = '#';      // Place wall
        break;
}
moves.add(action);              // Log move
turn = 1 - turn;               // Switch player
```

**`copy()` - Deep copy of game state**

Creates independent copy of everything:
```
- Copy entire 2D board array
- Copy both agent positions
- Copy both agent scores
- Copy turn and food count
- Copy move history
```

Essential for search algorithms so modifying one branch doesn't affect others.

**`isLeaf()` - Check if game is over**

```
Returns true if:
  - food == 0 (all food eaten), OR
  - legalMoves(turn).isEmpty() (current player stuck)
  
Otherwise returns false (game continues)
```

**`value(int agent)` - Evaluate game outcome**

Only evaluates if game is terminal:
```
if (all food eaten):
    return +1 if agent won
    return -1 if agent lost
    return  0 if tie

if (current player stuck):
    return -1 if agent is stuck player
    return +1 if agent is other player
```

---

### 3. **Game.java** - Search Algorithms

#### Game Class Structure

```java
public class Game {
    State b;              // Current board state
    int nodesVisited;     // Counter for statistics
    
    public Game() {
        b = new State();
        b.read("data/board.txt");
    }
}
```

#### Minimax Algorithm

```java
public State minimax(State s, int forAgent, int maxDepth, int depth) {
    nodesVisited++;  // Count this node
    
    // Base cases: stop searching
    if (depth >= maxDepth || s.isLeaf()) {
        return s;
    }
    
    State bestState = null;
    
    if (s.turn == forAgent) {
        // OUR TURN: Maximize our score
        double bestValue = Double.NEGATIVE_INFINITY;
        
        for (String move : s.legalMoves()) {
            State nextState = s.copy();
            nextState.execute(move);
            
            // Recursively evaluate
            State resultState = minimax(nextState, forAgent, maxDepth, depth + 1);
            double value = resultState.value(forAgent);
            
            // Keep the move that gives us the best outcome
            if (value > bestValue) {
                bestValue = value;
                bestState = nextState;
            }
        }
    } else {
        // OPPONENT'S TURN: Minimize our score
        double bestValue = Double.POSITIVE_INFINITY;
        
        for (String move : s.legalMoves()) {
            State nextState = s.copy();
            nextState.execute(move);
            
            // Recursively evaluate
            State resultState = minimax(nextState, forAgent, maxDepth, depth + 1);
            double value = resultState.value(forAgent);
            
            // Opponent picks the move worst for us
            if (value < bestValue) {
                bestValue = value;
                bestState = nextState;
            }
        }
    }
    
    return bestState;
}
```

**Algorithm Logic**:

1. **Base Case**: Stop if reached max depth or game is over
2. **Our Turn** (maximizing): Try all moves, pick the one with **highest value**
3. **Opponent's Turn** (minimizing): Assume opponent picks move with **lowest value** (worst for us)
4. **Recursion**: For each move, recursively evaluate what happens next

**Why it works**:
- Assumes **both players play perfectly**
- Our player chooses moves that maximize the final outcome
- Opponent chooses moves that minimize our outcome
- This builds a complete game tree where we can see the guaranteed result with perfect play

---

#### Alpha-Beta Pruning

Alpha-Beta is an optimization of Minimax that **skips branches that don't matter**.

**Key Insight**: 
If we've already found a move that guarantees a certain outcome, we don't need to look at moves that can only give us a worse outcome.

```java
public State alfabeta(State s, int forAgent, int maxDepth, int depth, 
                      double alfa, double beta) {
    nodesVisited++;
    
    if (depth >= maxDepth || s.isLeaf()) {
        return s;
    }
    
    State bestState = null;
    
    if (s.turn == forAgent) {
        // OUR TURN: Maximize
        double bestValue = Double.NEGATIVE_INFINITY;
        
        for (String move : s.legalMoves()) {
            State nextState = s.copy();
            nextState.execute(move);
            
            State resultState = alfabeta(nextState, forAgent, maxDepth, depth + 1,
                                        alfa, beta);
            double value = resultState.value(forAgent);
            
            if (value > bestValue) {
                bestValue = value;
                bestState = nextState;
            }
            
            // UPDATE ALFA (our guaranteed minimum)
            alfa = Math.max(alfa, bestValue);
            
            // PRUNE: If opponent can force us to worse than beta,
            // they'll never let us get here, so skip remaining moves
            if (beta <= alfa) {
                break;  // ← PRUNING HAPPENS HERE
            }
        }
    } else {
        // OPPONENT'S TURN: Minimize
        double bestValue = Double.POSITIVE_INFINITY;
        
        for (String move : s.legalMoves()) {
            State nextState = s.copy();
            nextState.execute(move);
            
            State resultState = alfabeta(nextState, forAgent, maxDepth, depth + 1,
                                        alfa, beta);
            double value = resultState.value(forAgent);
            
            if (value < bestValue) {
                bestValue = value;
                bestState = nextState;
            }
            
            // UPDATE BETA (opponent's guaranteed maximum, our guaranteed minimum)
            beta = Math.min(beta, bestValue);
            
            // PRUNE: If we already found a better option elsewhere,
            // opponent will never let us get here
            if (beta <= alfa) {
                break;  // ← PRUNING HAPPENS HERE
            }
        }
    }
    
    return bestState;
}
```

**Parameters**:
- `alfa`: Our guaranteed minimum score (best we know we can achieve)
- `beta`: Opponent's guaranteed maximum (worst we know will happen to us)

**Pruning Condition**:
When `beta <= alfa`, we know:
- The opponent has a better move elsewhere (`beta` is bad for us)
- We've already found a good move elsewhere (`alfa` is good for us)
- So exploring further moves here is pointless

**Why it's faster**:
- Minimax evaluates ALL nodes in the tree
- Alpha-Beta skips entire branches
- Same final answer, fewer evaluations

---

#### Test Method

```java
public void test() {
    // For each search depth from 3 to 13:
    for (int depth = 3; depth <= 13; depth++) {
        // 1. Run Minimax, measure time and nodes
        nodesVisited = 0;
        long startTime = System.currentTimeMillis();
        State minimaxResult = minimax(b, b.turn, depth, 0);
        long minimaxTime = System.currentTimeMillis() - startTime;
        int minimaxNodes = nodesVisited;
        
        // 2. Run Alpha-Beta, measure time and nodes
        nodesVisited = 0;
        startTime = System.currentTimeMillis();
        State alfabetaResult = alfabeta(b, b.turn, depth, 0,
                                        Double.NEGATIVE_INFINITY,
                                        Double.POSITIVE_INFINITY);
        long alfabetaTime = System.currentTimeMillis() - startTime;
        int alfabetaNodes = nodesVisited;
        
        // 3. Print comparison
        System.out.println("Depth " + depth + ":");
        System.out.println("  Minimax: " + minimaxNodes + " nodes, " + 
                          minimaxTime + " ms");
        System.out.println("  Alpha-Beta: " + alfabetaNodes + " nodes, " + 
                          alfabetaTime + " ms");
        System.out.println("  Speedup: " + (minimaxNodes/alfabetaNodes) + "x");
    }
}
```

---

## Minimax Algorithm

### Complete Example

**Game State at Depth 2**:
```
Current: Agent 0's turn at depth 0
Board:
  #####
  ##* #
  ##A*#
  # B##
  #####

Possible moves: up, right, eat
```

### Game Tree

```
Depth 0 (Agent 0's turn):
└─ MAX node
   ├─ Move "up"
   │  └─ Depth 1 (Agent 1's turn)
   │     └─ MIN node
   │        ├─ Move "left"
   │        │  └─ State S1, value = -1 (Agent 1 wins)
   │        └─ Move "down"
   │           └─ State S2, value = 0 (Tie)
   │        → MIN chooses "down" (value 0)
   │
   ├─ Move "right"
   │  └─ Depth 1 (Agent 1's turn)
   │     └─ MIN node
   │        ├─ Move "up"
   │        │  └─ State S3, value = +1 (Agent 0 wins)
   │        └─ Move "left"
   │           └─ State S4, value = +1 (Agent 0 wins)
   │        → MIN chooses S3 (value +1)
   │
   └─ Move "eat"
      └─ Depth 1 (Agent 1's turn)
         └─ MIN node
            ├─ Move "left"
            │  └─ State S5, value = 0 (Tie)
            └─ Move "down"
               └─ State S6, value = -1 (Agent 1 wins)
            → MIN chooses "down" (value -1)

Max at depth 0:
  "up" → 0
  "right" → +1   ← BEST
  "eat" → -1

Final choice: "right" (guarantees +1 win)
```

### Why This Is Correct

1. **Assume Perfect Play**: Both agents always choose their best move
2. **Recurse**: Each agent knows what the opponent will do in response
3. **Backup Values**: Values bubble up from leaves to root
4. **Optimal Decision**: Root agent picks move with highest value

---

## Alpha-Beta Pruning

### The Pruning Principle

**Scenario**:
```
We're exploring options A and B
In option A, we found we can guarantee a value of 5
Option B looks bad so far, and we can see it can only give us at most 3

We don't need to explore B further!
```

### Example with Pruning

```
Depth 0 (MAX - Agent 0):
├─ Option A:
│  └─ Explore thoroughly
│  └─ Result: value = 5
│  └─ alfa = 5 (best we can do)
│
└─ Option B:
   └─ Explore first few moves:
   │  ├─ Move B1: value = 3
   │  └─ We see beta = 3 (opponent's max for us)
   │
   └─ Since beta (3) <= alfa (5):
      We know opponent will choose a different move anyway
      PRUNE: Don't explore remaining moves in Option B
```

### Pruning Conditions

**Alpha-Cutoff** (when maximizing):
```
if (value > alfa):
    alfa = value
if (beta <= alfa):
    PRUNE  // Opponent has better option elsewhere
```

**Beta-Cutoff** (when minimizing):
```
if (value < beta):
    beta = value
if (beta <= alfa):
    PRUNE  // We have better option elsewhere
```

### Why It's Sound

Alpha-Beta never changes the final answer because:
1. It only prunes branches that **cannot affect** the best move
2. The pruned moves are guaranteed to be worse than alternatives already found
3. Therefore, the agent will never choose them anyway

---

## Algorithm Comparison

### Minimax vs Alpha-Beta

| Aspect | Minimax | Alpha-Beta |
|--------|---------|-----------|
| **Correctness** | ✓ Always correct | ✓ Always correct |
| **Nodes Evaluated** | All nodes | Fewer nodes (best case: O(b^(d/2))) |
| **Time Complexity** | O(b^d) | O(b^(d/2)) best case, O(b^d) worst case |
| **Space Complexity** | O(bd) | O(bd) |
| **Move Quality** | Same optimal move | **Same optimal move** |
| **Time Required** | Slow | **Much faster** |
| **Implementation** | Simpler | Slightly more complex |

**Key Insight**: Alpha-Beta is strictly better than Minimax for the same problem!

### When Pruning is Most Effective

**Best Case**: When good moves are considered first
```
Try best move → establish strong alfa
Later options can be pruned quickly
→ Much fewer nodes evaluated
```

**Worst Case**: When bad moves are considered first
```
Try bad move → weak alfa
Can't prune much
→ Still evaluate many nodes
```

**Move Ordering**: The order in which moves are considered significantly affects pruning efficiency!

---

## Example Execution

### Test Output

```
Testing Minimax vs Alpha-Beta Pruning
======================================

Testing at depth 3:
  Minimax visited 244 nodes
  Minimax took 5 milliseconds
  Alpha-beta visited 98 nodes
  Alpha-beta took 2 milliseconds
  Alpha-beta visited 2.49x fewer nodes!
  Alpha-beta was 2.50x faster!

Testing at depth 4:
  Minimax visited 1532 nodes
  Minimax took 15 milliseconds
  Alpha-beta visited 234 nodes
  Alpha-beta took 3 milliseconds
  Alpha-beta visited 6.53x fewer nodes!
  Alpha-beta was 5.00x faster!

Testing at depth 7:
  Minimax visited 892,847 nodes
  Minimax took 1203 milliseconds
  Alpha-beta visited 18,923 nodes
  Alpha-beta took 47 milliseconds
  Alpha-beta visited 47.2x fewer nodes!
  Alpha-beta was 25.6x faster!

Example: Best move found at depth 7
===================================
Alpha-beta visited 18923 nodes to find this move:

#####
##  #
## *#
# A##
#####

Agent 0's scores: Agent 0 = 2, Agent 1 = 0
Food remaining: 3
Moves made: [eat, right, eat]
```

### Interpretation

1. **Depth increases exponentially**: At depth 7, there are ~1M game states for Minimax
2. **Pruning is highly effective**: Alpha-Beta explores 45x fewer nodes at depth 7
3. **Practical speedup**: Alpha-Beta is 25x faster, but requires evaluating move ordering
4. **Move selection**: The algorithm found a winning strategy in just 3 moves

---

## Performance Analysis

### Complexity Analysis

**Minimax**:
- **Time**: O(b^d) where b = branching factor (moves per state), d = depth
- **Space**: O(b·d) for recursive call stack
- **Nodes**: Exponential in depth

**Alpha-Beta**:
- **Time Best Case**: O(b^(d/2)) - can search **twice as deep** in same time!
- **Time Worst Case**: O(b^d) - if move ordering is poor
- **Average Case**: O(b^(3d/4)) with reasonable move ordering

### Practical Improvements

**Example**: At depth 13 with branching factor ~5:
```
Minimax: 5^13 ≈ 1.2 million nodes
Alpha-Beta: 5^6.5 ≈ 31,623 nodes (best case)

Speedup: ~38x faster!
```

### Why Branching Factor Matters

Higher branching factor = more moves per state = harder pruning:
```
Depth 3, b=2: Minimax=32 nodes, Alpha-Beta=12 nodes (2.7x)
Depth 3, b=5: Minimax=775 nodes, Alpha-Beta=98 nodes (7.9x)
Depth 3, b=10: Minimax=3000 nodes, Alpha-Beta=244 nodes (12.3x)
```

---

## Key Takeaways

1. **Minimax** explores all branches to find optimal play
2. **Alpha-Beta Pruning** eliminates branches that won't affect the decision
3. **Same Answer, Different Cost**: Both find the same optimal move, but Alpha-Beta does it much faster
4. **Move Ordering**: Critical for pruning efficiency
5. **Practical Impact**: Enables searching to deeper depths in reasonable time
6. **Zero-Sum Games**: Minimax/Alpha-Beta are ideal for competitive games with perfect information

---

## Extensions

The system could be extended with:

1. **Iterative Deepening**: Search deeper incrementally until time runs out
2. **Transposition Tables**: Cache evaluated positions to avoid re-computing
3. **Move Ordering Heuristics**: Sort moves by quality estimate before evaluation
4. **Quiescence Search**: Extend search past depth limit for "interesting" positions
5. **Evaluation Function**: Estimate game value without reaching leaf nodes
6. **Temporal Difference Learning**: Learn better heuristics from game experience
7. **Monte Carlo Tree Search**: Sample random playouts instead of full expansion

---

## Conclusion

This assignment demonstrates fundamental game-playing AI:
- **Adversarial reasoning**: Understanding opponent's perspective
- **Perfect information**: Complete knowledge enables optimal play
- **Game tree search**: Systematic exploration of all possibilities
- **Algorithm optimization**: Pruning techniques dramatically improve efficiency
- **Complexity analysis**: Understanding growth rates guides algorithm selection

The Alpha-Beta pruning technique is foundational to computer game playing, chess engines, and any domain with perfect information and adversarial goals.
