# Wumpus World Assignment 2 - Comprehensive Explanation

## Table of Contents
1. [Overview](#overview)
2. [Prolog Basics](#prolog-basics)
3. [Knowledge Representation in Prolog](#knowledge-representation-in-prolog)
4. [Graph-Based Navigation](#graph-based-navigation)
5. [Pathfinding Algorithm](#pathfinding-algorithm)
6. [Robot Movement System](#robot-movement-system)
7. [Dynamic Facts and Predicates](#dynamic-facts-and-predicates)
8. [Family Relationships Example](#family-relationships-example)
9. [Program Execution Flow](#program-execution-flow)
10. [Advanced Prolog Concepts](#advanced-prolog-concepts)

---

## Overview

Assignment 2 uses **Prolog** (a logic programming language) to implement intelligent agents in the Wumpus World domain. The assignment demonstrates:

1. **Knowledge Representation**: Storing facts about the world in Prolog's fact database
2. **Logical Reasoning**: Using rules to derive new facts from existing ones
3. **Pathfinding**: Finding routes through a graph of connected locations
4. **Dynamic State**: Modifying facts at runtime (agent movement)
5. **Declarative Programming**: Specifying *what* to solve, not *how* to solve it

The system navigates an agent from a starting location to a goal through a network of connected locations (represented as a graph), using logical inference and backtracking.

---

## Prolog Basics

### What is Prolog?

Prolog is a **declarative logic programming language** based on formal logic. Instead of imperative commands ("do this, then do that"), you declare facts and rules, then ask queries.

### Core Prolog Concepts

#### Facts
A **fact** is a statement that is unconditionally true:

```prolog
% Simple facts
male(joost).
male(sacha).
parent(joost, sacha).
parent(joost, leon).

% These are stored in the database
```

#### Rules
A **rule** has conditions and conclusions:

```prolog
% If X is male AND X has parent P AND P has child Y, then X is brother of Y
brother(X, Y) :-
    male(X),
    parent(P, X),
    parent(P, Y),
    X \= Y.

% Structure: head(args) :- condition1, condition2, condition3.
%            ^^^^^^^^^          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
%            conclusion          conditions (separated by commas = AND)
```

#### Queries
A **query** asks whether a fact is true or finds values that make it true:

```prolog
?- male(joost).          % Is joost male? → yes
?- brother(X, sacha).    % Who is sacha's brother? → X = joost
?- parent(joost, X).     % Who are joost's children? → X = sacha; X = leon
```

### Unification and Pattern Matching

Prolog matches patterns by finding substitutions for variables:

```prolog
% Rule
parent(joost, X) :- X is child of joost

% Query
?- parent(joost, sacha).

% Unification: X = sacha (makes both sides identical)
% Result: true
```

### Backtracking

When a rule has multiple solutions, Prolog backtracks to find them all:

```prolog
parent(joost, sacha).
parent(joost, leon).

?- parent(joost, X).

% First solution: X = sacha
% On backtrack: X = leon
% On backtrack again: no more solutions
```

---

## Knowledge Representation in Prolog

### Wumpus World Facts

The Wumpus World (in `wumpusfinal.txt`) represents a navigation problem using:

```prolog
% Dynamic facts (can change during execution)
:- dynamic robot/1.           % Robot can move
robot(1).                     % Robot starts at location 1

% Static facts (goal never changes)
goal(5).                      % Goal is at location 5

% Connectivity (the graph structure)
link(1, 2).                   % Location 1 connects to location 2
link(2, 3).
link(3, 4).
link(3, 6).
link(6, 5).
link(6, 7).

% Visualized as a graph:
%
%     1 --- 2 --- 3 --- 4
%                 |
%                 6 --- 5
%                 |
%                 7
```

### Declarative Knowledge

Instead of specifying HOW to solve the problem, we describe WHAT we know:

```prolog
% WHAT: "A location is adjacent if there's a link and we're there"
adjacent(L) :-
    robot(X),        % Get robot's current position
    link(X, L).      % Check if there's a link from X to L

% HOW: Prolog figures out how to satisfy this
```

### Reachability

The system can express complex relationships:

```prolog
% "There exists a path P from goal to current position"
path(P) :-
    robot(X),         % Current position
    goal(G),          % Goal position
    find_path(X, G, P). % Find path

% "The path from X to X is empty (we're already there)"
find_path(X, X, []).

% "Path from X to Goal includes step Y, then rest of path"
find_path(X, Goal, [Y|Rest]) :-
    link(X, Y),              % Direct link exists
    find_path(Y, Goal, Rest). % Continue from Y
```

---

## Graph-Based Navigation

### The Navigation Graph

```prolog
link(1, 2).    link(2, 3).    link(3, 4).
link(3, 6).    link(6, 5).    link(6, 7).
```

Creates this structure:

```
Nodes: 1, 2, 3, 4, 5, 6, 7
Edges: 1—2, 2—3, 3—4, 3—6, 6—5, 6—7

Graph layout:
        [1]
         |
        [2]
         |
        [3]---[4]
         |
        [6]
        / \
      [5] [7]
```

### Node Properties

**Node Types**:
- **Start**: 1 (where robot begins)
- **Goal**: 5 (where robot wants to reach)
- **Intermediate**: 2, 3, 4, 6, 7 (helper nodes)

### Path Examples

**Path from 1 to 5**:
```
1 → 2 → 3 → 6 → 5
(4 steps)

Path representation: [2, 3, 6, 5]
(First step is 2, which is adjacent to 1)
```

**Alternative path from 1 to 5**:
```
1 → 2 → 3 → 4 (dead end, must backtrack)
(doesn't reach goal)
```

---

## Pathfinding Algorithm

### Depth-First Search (DFS)

The `find_path` predicate implements **depth-first search**:

```prolog
% Base case: reached the goal
find_path(X, X, []).
    % If current position X = Goal, path is empty (we're there)

% Recursive case: explore from X
find_path(X, Goal, [Y|Rest]) :-
    link(X, Y),              % Find a neighbor Y
    find_path(Y, Goal, Rest). % Recursively find path from Y to Goal
```

### Execution Trace

**Finding path from 1 to 5**:

```
find_path(1, 5, Path) ?
├─ Check base case: 1 ≠ 5, continue
├─ Find neighbor Y of 1:
│  ├─ link(1, 2) → Y = 2
│  ├─ Recurse: find_path(2, 5, Rest)
│  │  ├─ Check base case: 2 ≠ 5, continue
│  │  ├─ Find neighbor Y of 2:
│  │  │  ├─ link(2, 3) → Y = 3
│  │  │  ├─ Recurse: find_path(3, 5, Rest2)
│  │  │  │  ├─ Check base case: 3 ≠ 5, continue
│  │  │  │  ├─ Find neighbor Y of 3:
│  │  │  │  │  ├─ link(3, 4) → Y = 4
│  │  │  │  │  │  ├─ Recurse: find_path(4, 5, Rest3)
│  │  │  │  │  │  │  └─ 4 has no link to 5 (BACKTRACK)
│  │  │  │  │  │
│  │  │  │  │  ├─ link(3, 6) → Y = 6 (second option)
│  │  │  │  │  └─ Recurse: find_path(6, 5, Rest3)
│  │  │  │  │     ├─ Check base case: 6 ≠ 5, continue
│  │  │  │  │     ├─ Find neighbor Y of 6:
│  │  │  │  │     │  ├─ link(6, 5) → Y = 5
│  │  │  │  │     │  └─ Recurse: find_path(5, 5, Rest4)
│  │  │  │  │     │     ├─ BASE CASE: 5 = 5, Rest4 = []
│  │  │  │  │     │     └─ Return Rest3 = [5]
│  │  │  │  │     │
│  │  │  │  │     └─ Return Rest2 = [6, 5]
│  │  │  │  │
│  │  │  │  └─ Return Rest = [3, 6, 5]
│  │  │  │
│  │  │  └─ Return Rest = [2, 3, 6, 5]
│  │  │
│  │  └─ Return Path = [2, 3, 6, 5]
```

**Final Result**: 
```prolog
Path = [2, 3, 6, 5]
```

### Why DFS Works

1. **Complete**: Finds a solution if one exists (assuming finite graph)
2. **Sound**: Solution found is a valid path (each step has a link)
3. **No Infinite Loops**: Once we reach goal, we stop (base case)
4. **Backtracking**: If a path is blocked, try another option

**Caveat**: DFS can loop in graphs with cycles! Solution: track visited nodes (not in this simple implementation).

---

## Robot Movement System

### Dynamic Facts

The robot's position is stored as a **dynamic fact** that changes:

```prolog
% Declare that robot/1 can be asserted/retracted
:- dynamic robot/1.

% Initial state
robot(1).

% At runtime, the fact changes:
robot(1) → robot(2) → robot(3) → robot(6) → robot(5)
```

### Movement Rules

#### Rule 1: Suggested Movement (Using Pathfinding)

```prolog
move(L) :-
    suggest(L),           % L is suggested by pathfinding
    retract(robot(_)),    % Remove old position
    assertz(robot(L)).    % Add new position
```

**Logic**:
1. Compute optimal next step using pathfinding
2. Remove the old `robot(X)` fact
3. Add new `robot(L)` fact
4. If this succeeds, the move was made

#### Rule 2: Adjacent Movement (Fallback)

```prolog
move(L) :-
    adjacent(L),          % L is adjacent to current position
    retract(robot(_)),    % Remove old position
    assertz(robot(L)).    % Add new position
```

**Logic**: If pathfinding isn't available, move to any adjacent location.

### Retract and Assert

**`retract(Fact)`**: Remove a fact from the database
```prolog
retract(robot(1)).     % Remove: robot(1)
```

**`assertz(Fact)`**: Add a fact to the end of the database
```prolog
assertz(robot(2)).     % Add: robot(2)
```

These are the only ways to modify Prolog's database at runtime.

### Movement Sequence

```prolog
% Initial state
robot(1).

% Step 1
?- move(2).
  suggest(2) succeeds (pathfinding computes 1 → 2 → 3 → 6 → 5)
  retract(robot(1))    % Now: robot(X) fails
  assertz(robot(2))    % Now: robot(2) is true
  
% State after step 1
robot(2).

% Step 2
?- move(3).
  suggest(3) succeeds (pathfinding computes 2 → 3 → 6 → 5)
  retract(robot(2))    % Remove robot(2)
  assertz(robot(3))    % Add robot(3)
  
% State after step 2
robot(3).
```

---

## Dynamic Facts and Predicates

### Why Dynamic?

In Prolog, facts are normally static (fixed). For an agent, the robot's position must **change** at runtime.

### Declaration

```prolog
% Tells Prolog that robot/1 will be modified
:- dynamic robot/1.
```

This directive must appear **before** the first use of `robot/1`.

### Modification Operations

**Assert (Add)**:
```prolog
assertz(robot(2)).      % Add at end
asserta(robot(2)).      % Add at beginning (less common)
```

**Retract (Remove)**:
```prolog
retract(robot(X)).      % Remove first matching fact
retractall(robot(_)).   % Remove all matching facts
```

### State Changes

```prolog
% Initial database state
robot(1).

% After: retract(robot(X)), assertz(robot(2))
robot(2).

% After: retract(robot(X)), assertz(robot(3))
robot(3).

% Query succeeds or fails based on current state:
?- robot(3).    % true  (current state)
?- robot(1).    % false (was removed)
```

### Side Effects in Prolog

Normal Prolog predicates are "pure" (no side effects). But:
- `assertz`: **side effect** - modifies database
- `retract`: **side effect** - modifies database
- `write`: **side effect** - produces output

This makes the program **stateful** (depends on execution history).

---

## Family Relationships Example

The included `family (3).txt` demonstrates logical inference:

### Facts

```prolog
% Male and female facts
male(joost).
male(sacha).
male(leon).
male(merlijn).
male(peter).

female(sofie).
female(sandrine).
female(fien).

% Parent relationships
parent(joost, sacha).
parent(joost, leon).
parent(sandrine, sacha).
parent(sandrine, leon).
parent(fien, sofie).
parent(fien, merlijn).
parent(peter, fien).
parent(peter, joost).
```

### Derived Relationships

#### Sister Rule

```prolog
sister(X, Y) :-
    female(X),           % X is female
    parent(P, X),        % X has a parent P
    parent(P, Y),        % Y has the same parent P
    X \= Y.              % X and Y are different
```

**Meaning**: "X is sister of Y if X is female, they share a parent, and they're different people."

**Example Query**:
```prolog
?- sister(fien, joost).

Execute:
1. female(fien) → true
2. parent(P, fien) → P = peter
3. parent(peter, joost) → true
4. fien \= joost → true

Result: true (fien is joost's sister)
```

#### Ancestor Rule (Recursive)

```prolog
ancestor(X, Y) :-
    parent(X, Y).        % Direct parent-child
ancestor(X, Y) :-
    parent(X, Z),        % X has child Z
    ancestor(Z, Y).      % Z is ancestor of Y (recursion)
```

**Meaning**: "X is ancestor of Y if X is parent of Y, OR X is parent of someone who is ancestor of Y."

**Example Query**:
```prolog
?- ancestor(peter, sacha).

Execute:
1. Try: parent(peter, sacha) → false (not direct parent)
2. Try: parent(peter, Z), ancestor(Z, sacha)
   └─ parent(peter, fien) → Z = fien
      └─ ancestor(fien, sacha)
         └─ Try: parent(fien, sacha) → false
         └─ Try: parent(fien, Z2), ancestor(Z2, sacha)
            └─ parent(fien, joost) → Z2 = joost
               └─ ancestor(joost, sacha)
                  └─ parent(joost, sacha) → true ✓

Result: true (peter is sacha's great-grandfather)

Path: peter → fien → joost → sacha
```

#### Cousin Rule

```prolog
cousin(X, Y) :-
    male(X),                           % X is male
    parent(P, X),                      % X has parent P
    (brother(P, Z); sister(P, Z)),     % P has sibling Z
    parent(Z, Y),                      % Z is parent of Y
    X \= Y.                            % X and Y are different
```

**Meaning**: "X and Y are cousins if X's parent has a sibling who is Y's parent."

**Complex Queries**:
```prolog
% Find all siblings
?- sister(X, Y).
X = fien, Y = joost;
X = fien, Y = leon;
...

% Find all ancestors of sacha
?- ancestor(X, sacha).
X = joost;
X = sandrine;
X = peter;
X = fien;
...
```

---

## Program Execution Flow

### Complete Robot Navigation Sequence

**Initial Setup**:
```prolog
robot(1).              % Robot at location 1
goal(5).               % Goal is location 5

link(1, 2).
link(2, 3).
link(3, 4).
link(3, 6).
link(6, 5).
link(6, 7).
```

### Execution Steps

**Step 1: Query Robot's Path**

```prolog
?- path(P).

Execute:
1. robot(X) → X = 1 (current position)
2. goal(G) → G = 5 (goal position)
3. find_path(1, 5, P)
   └─ Finds P = [2, 3, 6, 5]

Result: P = [2, 3, 6, 5]
```

**Step 2: Suggest Next Move**

```prolog
?- suggest(L).

Execute:
1. path([L|_]) 
   └─ path([L|_]) uses find_path to get P = [2, 3, 6, 5]
   └─ First element L = 2

Result: L = 2 (next move is to location 2)
```

**Step 3: Execute Move**

```prolog
?- move(2).

Execute:
1. suggest(2) → true
2. retract(robot(1))  → Remove robot(1) from database
3. assertz(robot(2))  → Add robot(2) to database

Result: true
State changed: robot(1) → robot(2)
```

**Step 4: Repeat**

```prolog
?- suggest(L).   % Now L = 3 (next in path)
?- move(3).      % Move to location 3, etc.

State progression: 1 → 2 → 3 → 6 → 5
```

### Summary of Execution

```
Initial: robot(1), goal(5)
         Request: move robot to goal

Step 1:  Compute path = [2, 3, 6, 5]
Step 2:  Extract first step = 2
Step 3:  Execute move to 2
         robot(1) → robot(2)

Step 4:  Compute path from 2 = [3, 6, 5]
Step 5:  Extract first step = 3
Step 6:  Execute move to 3
         robot(2) → robot(3)

...continue until...

Step N:  robot(6) → robot(5)
Step N+1: At goal! Stop.

Total moves: 4 (1→2→3→6→5)
```

---

## Advanced Prolog Concepts

### List Syntax

Prolog uses lists extensively:

```prolog
% List notation
[].              % Empty list
[1].             % Single element
[1, 2, 3].       % Multiple elements

% Head|Tail notation
[H|T] where:
  H = first element (head)
  T = remaining elements (tail)

% Examples
[1, 2, 3] = [1 | [2, 3]]
            = [1, 2 | [3]]
            = [1, 2, 3 | []]

% Pattern matching
[X] matches lists with 1 element (bind X to that element)
[X, Y] matches lists with 2 elements
[X, Y | Rest] matches lists with 2+ elements
```

### Append Predicate

The `append` rule concatenates lists:

```prolog
append([], X, X).
    % Base case: append([], L, L) - adding nothing to L gives L

append([X | Y], Z, [X | W]) :-
    append(Y, Z, W).
    % Recursive case: 
    % If first list is [X|Y] and second is Z,
    % result is [X|W] where W = append(Y, Z)
```

**Example**:
```prolog
?- append([1, 2], [3, 4], R).
R = [1, 2, 3, 4]

Execution:
append([1, 2], [3, 4], R)
├─ [1, 2] matches [X|Y] where X=1, Y=[2]
├─ append([2], [3, 4], W)
│  ├─ [2] matches [X|Y] where X=2, Y=[]
│  ├─ append([], [3, 4], W2)
│  │  └─ Base case: W2 = [3, 4]
│  └─ W = [2 | [3, 4]] = [2, 3, 4]
└─ R = [1 | [2, 3, 4]] = [1, 2, 3, 4]
```

### Comparison Operators

```prolog
X = Y      % Unification (can bind variables)
X \= Y     % Not unifiable
X < Y      % Arithmetic less than
X > Y      % Arithmetic greater than
X =< Y     % Arithmetic less or equal
X >= Y     % Arithmetic greater or equal
X == Y     % Exactly equal (no unification)
X \== Y    % Exactly not equal
```

### Cut Operator (!)

The cut operator `!` prunes choice points:

```prolog
max(X, Y, X) :- X >= Y, !.  % If X >= Y, don't look for other solutions
max(X, Y, Y).                % Otherwise Y is max

?- max(5, 3, M).
M = 5 (and no other solutions due to cut)
```

Without cut, Prolog would try both rules even after finding a solution.

### Negation

```prolog
\+ Condition     % Logical negation (not provable)

% Example
human_and_not_male(X) :-
    human(X),
    \+ male(X).

% This uses "negation as failure": if male(X) cannot be proven, 
% \+ male(X) succeeds
```

---

## Key Differences: Prolog vs Traditional Programming

| Aspect | Prolog | Traditional (Java/Python) |
|--------|--------|--------------------------|
| **Paradigm** | Declarative (what to solve) | Imperative (how to solve) |
| **Control Flow** | Backtracking search | Explicit statements |
| **State** | Facts in database | Variables in memory |
| **Matching** | Pattern unification | Equality comparison |
| **Solutions** | Can have multiple | Usually one |
| **Data Structure** | Lists, terms | Arrays, objects |
| **Loops** | Recursion + backtracking | For/while loops |

---

## Advantages of Logic Programming

1. **Declarative**: Describe the problem, not the solution
2. **Expressive**: Powerful for symbolic reasoning
3. **Automatic Search**: Backtracking explores alternatives
4. **Flexible**: Can use rules in multiple directions

```prolog
% Definition: parent(A, B) means A is parent of B
parent(john, mary).

% Can query multiple ways:
?- parent(john, X).        % Who are john's children?
?- parent(X, mary).        % Who are mary's parents?
?- parent(john, mary).     % Is john mary's parent?
```

---

## Limitations of Prolog

1. **Depth-First Search**: May find suboptimal solutions or infinite loops
2. **No Negation as Failure**: Can't prove something is false (only unprovable)
3. **Performance**: Not optimized for large datasets
4. **Debugging**: Backtracking can make execution hard to trace
5. **No Floating Point**: Limited numeric computation

---

## Prolog for AI Applications

### Well-Suited For:

1. **Knowledge Bases**: Storing facts and rules about domains
2. **Expert Systems**: Reasoning with rules to answer queries
3. **Logic Puzzles**: Constraint satisfaction problems
4. **Natural Language**: Parsing and semantic analysis
5. **Pathfinding**: Graph traversal and shortest paths (like this assignment)

### Example: Wumpus World Agent

In real Wumpus World:
- Agent doesn't know environment in advance
- Must use sensors to learn about threats
- Uses logic to infer wumpus location
- Plans safe paths

Prolog excels at:
```prolog
% "If I smell a stench and I moved from (1,1), then wumpus is at (1,2) or (2,1)"
infer_wumpus_location(X, Y) :-
    stench_percept,
    came_from(Xp, Yp),
    adjacent(X, Y, Xp, Yp).
```

---

## Conclusion

Assignment 2 demonstrates:

1. **Logic Programming**: Fundamentally different from imperative programming
2. **Declarative Specification**: Define what the agent knows, not how it acts
3. **Inference**: Derive new facts from existing facts and rules
4. **Pathfinding**: Use logical reasoning to find paths through graphs
5. **Dynamic State**: Modify facts to represent agent movement
6. **Backtracking Search**: Automatic exploration of alternatives

Prolog is particularly powerful for AI applications involving:
- Symbolic reasoning
- Knowledge representation
- Logical inference
- Constraint solving
- Natural language processing

The Wumpus World is a classic AI testbed that demonstrates an agent navigating an unknown environment, perceiving threats, reasoning about locations, and planning safe paths—all expressible elegantly in Prolog.
