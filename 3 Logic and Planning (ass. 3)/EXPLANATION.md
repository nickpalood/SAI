# Logic and Planning Assignment 3 - Comprehensive Explanation

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Core Components](#core-components)
4. [The Agent Cycle](#the-agent-cycle)
5. [Knowledge Representation](#knowledge-representation)
6. [Forward Chaining Algorithm](#forward-chaining-algorithm)
7. [Planning Algorithm](#planning-algorithm)
8. [The Environment (Maze World)](#the-environment-maze-world)
9. [Example Execution Flow](#example-execution-flow)
10. [Knowledge Base Files](#knowledge-base-files)

---

## Overview

This assignment implements a **BDI (Beliefs, Desires, Intentions) Agent** that uses **logical inference** and **automated planning** to navigate a maze-like environment. The agent:

- **Senses** the environment to gather percepts
- **Thinks** using logical rules to derive beliefs, goals, and possible actions
- **Acts** by executing actions and updating its knowledge
- **Plans** using iterative deepening search to achieve goals

The core of the system is:
1. **Forward Chaining**: A logical inference algorithm that derives new facts from rules and existing facts
2. **BDI Architecture**: Maintaining separate knowledge bases for beliefs, desires, and intentions
3. **Planning**: Using depth-first search with iterative deepening to find action sequences that achieve goals

---

## System Architecture

### High-Level Component Diagram

```
┌─────────────────────────────────────────────────────┐
│                  Agent (BDI Agent)                   │
├─────────────────────────────────────────────────────┤
│                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │   Beliefs    │  │   Desires    │  │ Intentions │ │
│  │  (KB class)  │  │  (KB class)  │  │ (KB class) │ │
│  └──────────────┘  └──────────────┘  └────────────┘ │
│                                                       │
│  ┌──────────────────────────────────────────────────┐│
│  │        Rule Databases                           ││
│  │  • Percept Rules (percepts.txt)                 ││
│  │  • Program Rules (program.txt)                  ││
│  │  • Action Rules (actions.txt)                   ││
│  └──────────────────────────────────────────────────┘│
│                                                       │
│  ┌──────────────────────────────────────────────────┐│
│  │    Agent Methods                                ││
│  │  • sense() - Gather percepts from world        ││
│  │  • think() - Apply program rules               ││
│  │  • decide() - Choose action or plan           ││
│  │  • act() - Execute action & apply postconds   ││
│  │  • cycle() - Run full sense-think-act loop    ││
│  └──────────────────────────────────────────────────┘│
│                                                       │
│  ┌──────────────────────────────────────────────────┐│
│  │    Inference & Planning                         ││
│  │  • forwardChain() - Logical inference          ││
│  │  • idSearch() - Iterative deepening search    ││
│  │  • depthFirst() - Recursive depth-first search ││
│  │  • unifiesWith() - Unification algorithm       ││
│  └──────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────┘
                          ↓
        ┌─────────────────────────────────────┐
        │    Maze Environment                 │
        │  • Locations with keys/locks        │
        │  • generatePercepts()               │
        │  • executeAction()                  │
        └─────────────────────────────────────┘
```

---

## Core Components

### 1. **RunMe.java** - Entry Point
The main program that orchestrates everything:

```java
public static void main(String[] args) {
    // 1. Load the maze world from file
    Maze w = new Maze(new File("data/prison.txt"));
    
    // 2. Create an agent
    Agent a = new MyAgent();
    
    // 3. Configure agent behavior
    a.HUMAN_DECISION = false;  // Automatic decision-making
    a.PLAN = true;              // Enable planning
    a.VERBOSE = true;           // Show detailed output
    
    // 4. Load three knowledge bases with logical rules
    a.loadKnowledgeBase("percepts", new File("data/percepts.txt"));
    a.loadKnowledgeBase("program", new File("data/program.txt"));
    a.loadKnowledgeBase("actions", new File("data/actions.txt"));
    
    // 5. Run the sense-think-act cycle repeatedly
    while (true) {
        a.cycle(w);  // One iteration of the agent cycle
        System.out.println("Press <enter> to continue next cycle");
        io.nextLine();
    }
}
```

**Key Concept**: The agent runs in a **discrete loop**, executing one sense-think-act cycle per iteration.

---

### 2. **Agent.java** - Core Agent Logic

This abstract class defines the BDI architecture and the main agent cycle:

#### Agent State (Knowledge Bases)
```java
KB believes;       // What the agent believes to be true
KB desires;        // What goals the agent wants to achieve
KB intentions;     // What actions the agent can currently perform

KB perceptRules;   // Rules for processing sensory data
KB programRules;   // Rules for reasoning and planning
KB actionRules;    // Rules for action postconditions
```

#### The Sense-Think-Act Cycle

```java
public void cycle(Maze w) {
    intentions = new KB();  // Clear intentions each cycle
    sense(w);               // Phase 1: Gather and process percepts
    think(believes, desires, intentions);  // Phase 2: Reason about situation
    act(w, decide(HUMAN_DECISION), believes, desires);  // Phase 3: Execute action
}
```

#### Phase 1: SENSE

```java
public void sense(Maze w) {
    // Get raw percepts from the world
    KB percepts = w.generatePercepts();
    
    // Apply inference: Percept Rules ∪ Percepts ∪ Beliefs
    KB result = forwardChain(perceptRules.union(percepts).union(believes));
    
    // Process derived facts into appropriate KBs
    processFacts(result, believes, desires, intentions);
}
```

**What happens**:
1. World generates facts about current sensory observations (e.g., `at(room1)`, `atKey(room1,k1)`)
2. Forward chaining applies **percept rules** to derive new beliefs (e.g., `+connected(room1,room2)`)
3. Results are processed to update beliefs, desires, and intentions

#### Phase 2: THINK

```java
public void think(KB b, KB d, KB i) {
    // Apply program rules to beliefs
    KB facts = forwardChain(programRules.union(b));
    
    // Process derived facts and actions
    processFacts(facts, b, d, i);
}
```

**What happens**:
1. Forward chaining applies **program rules** to beliefs
2. This generates new beliefs, goals (`*predicate`), and potential actions (`_action`)
3. Actions are added to **intentions** for the decide phase

#### Phase 3: DECIDE

```java
public Predicate decide(boolean humanActor) {
    if (PLAN) {
        // Planning mode: find action sequences to achieve goals
        Vector<Plan> plans = new Vector<>();
        for (Sentence d : desires.rules()) {
            Plan plan = idSearch(7, believes, new Predicate(d));
            if (plan != null && plan.size() > 0) {
                plans.add(plan);
            }
        }
        if (plans.size() > 0) {
            return plans.get(0).get(0);  // Return first action of first plan
        }
    }
    
    // Fallback: pick a random intention
    if (intentions.rules().size() > 0) {
        return new Predicate(intentions.get(0));
    }
    return null;
}
```

**Decision Logic**:
1. If planning is enabled and there are unsatisfied goals, **find plans** to achieve them
2. Otherwise, pick a **random action from intentions**
3. If no actions available, return `null` (do nothing)

#### Phase 4: ACT

```java
public void act(Maze w, Predicate action, KB b, KB d) {
    if (action != null && w.executeAction(action)) {
        // Action succeeded in the world
        KB actionKB = new KB();
        actionKB.add(new Sentence(action.toString()));
        
        // Apply action rules to determine postconditions
        KB facts = forwardChain(actionRules.union(actionKB).union(b));
        
        // Update beliefs with postconditions
        processFacts(facts, b, d, null);
    } else {
        System.out.println("Warning: action execution failed");
    }
}
```

**What happens**:
1. Try to execute the action in the real world
2. If successful, apply **action rules** to determine postconditions
3. Update beliefs based on what changed (e.g., agent moved, key picked up)

---

### 3. **MyAgent.java** - Algorithm Implementations

This concrete class implements the abstract methods for inference and planning:

#### Forward Chaining Algorithm

```java
public KB forwardChain(KB kb) {
    KB facts = new KB();  // New facts derived from rules
    HashMap<String, Predicate> factMap = new HashMap<>();
    
    boolean changed = true;
    while (changed) {  // Repeat until no new facts can be derived
        changed = false;
        
        // For each rule in the knowledge base
        for (Sentence rule : kb.rules()) {
            // Find ALL possible substitutions that satisfy the rule's conditions
            Collection<HashMap<String, String>> allSubsts = new Vector<>();
            findAllSubstitutions(allSubsts, new HashMap<>(), rule.conditions, factMap);
            
            // For each valid substitution
            for (HashMap<String, String> subst : allSubsts) {
                // Apply substitution to each conclusion
                for (Predicate conclusion : rule.conclusions) {
                    Predicate boundConclusion = substitute(conclusion, subst);
                    
                    // Add if fully bound (no variables) and new
                    if (boundConclusion.bound()) {
                        String key = boundConclusion.toString();
                        if (!factMap.containsKey(key)) {
                            facts.add(new Sentence(key));
                            factMap.put(key, boundConclusion);
                            changed = true;
                        }
                    }
                }
            }
        }
    }
    return facts;
}
```

**Algorithm Steps**:
1. Initialize empty fact set
2. **Repeat** while new facts are being derived:
   - For each rule in KB:
     - Find ALL substitutions that satisfy the rule's conditions
     - Apply each substitution to the rule's conclusions
     - Add fully-bound conclusions as new facts
3. Return all derived facts

**Example**:
```
Rule: parent(X,Y) & male(Y) > father(X,Y)
Facts: parent(john,bob), male(bob)

Iteration 1:
  - Find substitution: X=john, Y=bob
  - Apply to conclusion: father(john,bob)
  - Add father(john,bob) to facts
  
Iteration 2:
  - No new rules fire
  - Algorithm terminates
```

#### Unification Algorithm

```java
public HashMap<String, String> unifiesWith(Predicate p, Predicate f) {
    // Assumes f (fact) is fully bound, p (pattern) may have variables
    
    // Must have same name and arity
    if (!p.getName().equals(f.getName())) return null;
    if (p.getTerms().size() != f.getTerms().size()) return null;
    
    HashMap<String, String> subst = new HashMap<>();
    
    // Try to unify each term
    for (int i = 0; i < p.getTerms().size(); i++) {
        Term pTerm = p.getTerm(i);
        Term fTerm = f.getTerm(i);
        
        if (pTerm.isVariable()) {
            // Variable in pattern: must be consistent
            if (subst.containsKey(pTerm)) {
                if (!subst.get(pTerm).equals(fTerm)) return null;
            } else {
                subst.put(pTerm, fTerm);
            }
        } else {
            // Constant in pattern: must match exactly
            if (!pTerm.equals(fTerm)) return null;
        }
    }
    
    return subst;
}
```

**Example**:
```
Pattern: at(X)
Fact: at(room1)

Unification succeeds: X → room1

Pattern: parent(X,Y)
Fact: parent(john,bob)

Unification succeeds: X → john, Y → bob

Pattern: parent(john,bob)
Fact: parent(X,Y)

Unification fails (because fact is not bound)
```

#### Iterative Deepening Search (Planning)

```java
public Plan idSearch(int maxDepth, KB kb, Predicate goal) {
    // Try depths 1, 2, 3, ..., maxDepth
    for (int depth = 1; depth <= maxDepth; depth++) {
        KB stateCopy = new KB();
        for (Sentence s : kb.rules()) {
            stateCopy.add(s);
        }
        
        Plan plan = depthFirst(depth, 0, stateCopy, goal, new Plan());
        if (plan != null) {
            return plan;  // Found a plan at this depth
        }
    }
    
    return null;  // No plan found at any depth up to maxDepth
}
```

**Key Feature**: Explores plans of increasing length, ensuring the shortest plan is found first.

#### Depth-First Search (Planning)

```java
public Plan depthFirst(int maxDepth, int depth, KB state, Predicate goal, Plan partialPlan) {
    // Base case: reached maximum depth
    if (depth > maxDepth) return null;
    
    // Check if goal is satisfied in current state
    KB inferredFacts = forwardChain(programRules.union(state));
    
    // Strip operators from goal (e.g., *at(X) → at(X))
    Predicate goalWithoutOperator = stripOperator(goal);
    
    // See if goal unifies with any derived fact
    for (Sentence s : inferredFacts.rules()) {
        Predicate fact = new Predicate(s);
        HashMap<String, String> unifier = unifiesWith(goalWithoutOperator, fact);
        if (unifier != null) {
            return partialPlan;  // Goal achieved!
        }
    }
    
    // Goal not satisfied: try each available action
    KB tempDesires = new KB();
    KB intentions = new KB();
    think(state, tempDesires, intentions);  // Get available actions
    
    if (intentions.isEmpty()) return null;
    
    // For each action
    for (Sentence intentionSentence : intentions.rules()) {
        Predicate action = new Predicate(intentionSentence);
        
        // Create new plan with this action
        Plan newPlan = new Plan(partialPlan);
        newPlan.add(action);
        
        // Simulate action: copy state and apply postconditions
        KB newState = new KB();
        for (Sentence s : state.rules()) {
            newState.add(s);
        }
        act(null, action, newState, new KB());  // null world = simulation
        
        // Recursively search from new state
        Plan result = depthFirst(maxDepth, depth + 1, newState, goal, newPlan);
        if (result != null) {
            return result;  // Found a plan!
        }
    }
    
    return null;  // No plan found from this state
}
```

**Algorithm Logic**:
1. Check if goal is already satisfied → **return current plan**
2. Get available actions from current state → **find all intentions**
3. For each action:
   - Create new plan with this action
   - Simulate the action's effects on state
   - **Recursively search from new state**
4. Return first plan that reaches the goal

---

## Knowledge Representation

### Four Types of Special Predicates (Operators)

The system uses prefix operators to distinguish different types of predicates:

| Operator | Name | Meaning | Example |
|----------|------|---------|---------|
| `+` | Addition | Add fact to beliefs | `+visited(X)` |
| `-` | Deletion | Remove fact from beliefs | `-at(X)` |
| `*` | Adoption | Add goal to desires | `*at(room2)` |
| `~` | Dropping | Remove goal from desires | `~at(room2)` |
| `_` | Action | Add to intentions (actions to perform) | `_grab(X,K)` |
| `!` | Negation | Logical negation (NOT) | `!hasKey(K)` |

### Rule Syntax

Rules use **simplified first-order logic** with the format:

```
condition1 & condition2 & ... > conclusion1 & conclusion2 & ...
```

**Elements**:
- **Conditions**: Predicates that must be true (left of `>`)
- **Conclusions**: Facts or actions that become true if conditions hold (right of `>`)
- **Variables**: Uppercase letters (e.g., `X`, `Y`, `K`)
- **Constants**: Lowercase identifiers (e.g., `room1`, `key1`)

**Example Rule**:
```
at(X) & atKey(X,K) & !hasKey(K) > _grab(X,K)
```

**Meaning**: "If the agent is at location X, and a key K is at location X, and the agent doesn't have the key, then the agent should grab the key."

---

## Forward Chaining Algorithm

### Complete Execution Example

**Given**:
```
Rules:
  R1: parent(X,Y) & male(Y) > father(X,Y)
  R2: father(X,Y) & father(Y,Z) > grandfather(X,Z)

Facts:
  F1: parent(john,bob)
  F2: male(bob)
  F3: parent(bob,alice)
  F4: male(alice)
```

**Execution**:

**Iteration 1**:
```
Processing R1: parent(X,Y) & male(Y) > father(X,Y)
  Condition 1: parent(X,Y)
    - Unifies with F1: parent(john,bob) → X=john, Y=bob
    - Check remaining conditions with this substitution
  Condition 2: male(Y) = male(bob)
    - Unifies with F2: male(bob) ✓
    - All conditions satisfied!
  Apply substitution to conclusion: father(john,bob)
  → Add father(john,bob) to facts [NEW]
  
Processing R2: father(X,Y) & father(Y,Z) > grandfather(X,Z)
  Condition 1: father(X,Y)
    - No father/2 facts yet, no match
    - Cannot process this rule
  
Changed = true (new fact added)
```

**Iteration 2**:
```
Processing R1: parent(X,Y) & male(Y) > father(X,Y)
  Condition 1: parent(X,Y)
    - Unifies with F3: parent(bob,alice) → X=bob, Y=alice
  Condition 2: male(Y) = male(alice)
    - Unifies with F4: male(alice) ✓
    - All conditions satisfied!
  Apply substitution to conclusion: father(bob,alice)
  → Add father(bob,alice) to facts [NEW]

Processing R2: father(X,Y) & father(Y,Z) > grandfather(X,Z)
  Condition 1: father(X,Y)
    - Unifies with father(john,bob) → X=john, Y=bob
    - Check remaining conditions
  Condition 2: father(Y,Z) = father(bob,Z)
    - Unifies with father(bob,alice) → Z=alice
    - All conditions satisfied!
  Apply substitution to conclusion: grandfather(john,alice)
  → Add grandfather(john,alice) to facts [NEW]
  
Changed = true (new facts added)
```

**Iteration 3**:
```
No new facts can be derived
Changed = false → Algorithm terminates
```

**Final Facts**:
```
parent(john,bob)
male(bob)
parent(bob,alice)
male(alice)
father(john,bob)      ← Derived
father(bob,alice)     ← Derived
grandfather(john,alice) ← Derived
```

---

## Planning Algorithm

### Planning Flow

```
User Goal: at(exit)
         ↓
    idSearch(7, beliefs, at(exit))
         ↓
    Try depth 1:
      depthFirst(1, 0, state, at(exit), [])
         → Is at(exit) in state? No
         → Get available actions
         → Try each action, recurse
         → No solution at depth 1
         ↓
    Try depth 2:
      depthFirst(2, 0, state, at(exit), [])
         → Is at(exit) in state? No
         → Get available actions
         → For action "goto(room1,room2)":
           - Create new state with postconditions applied
           - depthFirst(2, 1, newState, at(exit), [goto(room1,room2)])
             → Is at(exit) in newState? No
             → Get actions from newState
             → For action "open(room2,key1)":
               - Apply postconditions
               - depthFirst(2, 2, newState2, at(exit), [goto(...), open(...)])
                 → Goal achieved!
                 → Return [goto(...), open(...)]
         ↓
    Found plan at depth 2!
    Return [goto(room1,room2), open(room2,key1)]
```

### Planning Correctness

**Why does planning work?**

1. **Soundness**: Every action in the returned plan is executable and moves closer to the goal
2. **Completeness**: If a plan exists within `maxDepth`, it will be found
3. **Optimality**: Iterative deepening finds the shortest plan

---

## The Environment (Maze World)

### Maze Structure

The maze is loaded from a text file like `prison.txt`:

```
10 10          (width height)
S.....K.E.    (S=start, E=exit, .=empty, K=key, D=door, numbers=locked doors)
..........
..........
K........D
```

### Location Types

| Symbol | Meaning |
|--------|---------|
| `S` | Start position |
| `E` | Exit (goal) |
| `.` | Empty passage |
| `K` | Key location |
| `1`,`2`,`3`... | Locked door with matching key |

### Agent Percepts

Each cycle, the environment generates percepts based on the agent's current state:

```java
KB percepts = w.generatePercepts();
// Returns facts like:
//   at(room1)           - Agent's current location
//   passage(room2)      - Adjacent passage
//   key(key1)           - Key at current location
//   locked(key1)        - Door is locked with key1
//   exit                - Exit is here
```

---

## Example Execution Flow

### Complete Scenario

**Initial State**:
```
Agent at location: start
Inventory: empty
Goal: Reach exit

Beliefs: at(start)
Desires: at(exit)
Intentions: (empty)
```

**Cycle 1: SENSE**

```
World generates percepts:
  at(start)
  passage(room1)
  atKey(start,key1)

Apply percept rules:
  at(start) > +at(start)        ✓ (belief already exists)
  passage(room1) & at(start) > +connected(start,room1)  
                                ✓ (add connection)

Result: beliefs now include at(start), connected(start,room1)
```

**Cycle 1: THINK**

```
Apply program rules:
  at(start) & !visited(start) > +visited(start)
                                ✓ (add visited)
  
  at(start) & atKey(start,key1) & !hasKey(key1) > _grab(start,key1)
                                ✓ (add intention to grab)
  
  connected(start,room1) & at(start) & !visited(room1) & !atLocked(room1,K)
                                ✓ (add intention to move)

Result: 
  Beliefs: at(start), visited(start), connected(start,room1)
  Desires: at(exit)
  Intentions: grab(start,key1), at(room1)
```

**Cycle 1: DECIDE**

```
Planning enabled, goal = at(exit)

idSearch(7, beliefs, at(exit)) begins...
  depth = 1: Can't reach exit in 1 action
  depth = 2: Can't reach exit in 2 actions
  ...
  depth = 4: 
    goto(start,room1) > move
    goto(room1,room2) > move
    goto(room2,exit) > move
    → Plan: [goto(start,room1), goto(room1,room2), goto(room2,exit)]
    
Return first action: goto(start,room1)
```

**Cycle 1: ACT**

```
Execute action: goto(start,room1)
  World accepts action ✓

Apply action rules:
  goto(start,room1) > -at(start) & +at(room1)
                    ✓ (remove start, add room1)

Result:
  Beliefs updated: at(room1) [instead of at(start)]
```

**Cycle 2-4**: Process continues...
```
Cycle 2: Execute goto(room1,room2)
Cycle 3: Execute goto(room2,exit)
Cycle 4: 
  SENSE: at(exit), exit
  THINK: Goal at(exit) is satisfied → remove from desires
  DECIDE: No more goals
  Result: Agent has reached the exit!
```

---

## Knowledge Base Files

### 1. percepts.txt - Sensory Processing Rules

```plaintext
# This file defines how to interpret raw sensory data from the world

# Current location
at(X) > +at(X)

# Adjacent locations
passage(Y) & at(X) > +connected(X,Y)

# Keys at current location
key(K) & at(X) > +atKey(X,K)

# Locked doors at current location
locked(K) & at(X) > +atLocked(X,K)

# Exit location
exit & at(X) > +atExit(X)
```

**Purpose**: Transform raw percepts into usable beliefs. Adds `+` operator to make beliefs persistent.

---

### 2. program.txt - Agent Reasoning Rules

```plaintext
# This file defines the agent's reasoning about goals and actions

# Track visited locations
at(X) & !visited(X) > +visited(X)

# Goal: Go to exit if visible
atExit(Y) & !at(Y) > *at(Y)

# Goal: Unlock door if blocking path
atLocked(Y,K) & connected(X,Y) & at(X) & hasKey(K) > *notLocked(Y,K)

# Goal: Get key if needed to unlock door
atLocked(Y,K) & !hasKey(K) & atKey(Z,K) & !at(Z) > *at(Z)

# Goal: Explore connected unvisited locations
connected(X,Y) & at(X) & !visited(Y) & !atLocked(Y,K) > *at(Y)

# Action: Grab key at current location
at(X) & atKey(X,K) & !hasKey(K) > _grab(X,K)
```

**Purpose**: 
- `+` rules: Derive new beliefs (e.g., mark visited)
- `*` rules: Generate goals (things to achieve)
- `_` rules: Generate intentions (actions to take)

---

### 3. actions.txt - Action Postconditions

```plaintext
# This file defines what happens after an action succeeds

# Open/unlock a door
open(X,K) > -atLocked(X,K) & -hasKey(K) & +notLocked(X,K)

# Grab a key from location
grab(X,K) > +hasKey(K) & -atKey(X,K)

# Move to new location
goto(X,Y) > -at(X) & +at(Y)
```

**Purpose**: Specify the effects of actions on beliefs. Applied when an action succeeds in the world.

---

## Key Data Structures

### KB (Knowledge Base)
```java
class KB {
    Vector<Sentence> rules;           // All sentences (facts and rules)
    HashMap<String,Sentence> hash;    // Fast lookup
    
    void add(Sentence s);             // Add a sentence
    void del(Sentence s);             // Remove a sentence
    KB union(KB kb1);                 // Combine two KBs
    Vector<Sentence> rules();         // Get all sentences
}
```

### Sentence
```java
class Sentence {
    Vector<Predicate> conditions;     // Conditions (left of >)
    Vector<Predicate> conclusions;    // Conclusions (right of >)
    
    // Example: at(X) & !visited(X) > +visited(X)
    //          conditions = [at(X), !visited(X)]
    //          conclusions = [+visited(X)]
}
```

### Predicate
```java
class Predicate {
    String name;                      // Name of predicate
    Vector<Term> terms;               // Arguments
    boolean add, del, act, adopt, drop, neg;  // Operators
    
    // Example: +visited(room1)
    //          name = "visited"
    //          terms = ["room1"]
    //          add = true
}
```

### Term
```java
class Term {
    String term;                      // The value (variable or constant)
    boolean var;                      // Is this a variable?
    
    // Example: "X" → var=true
    //          "room1" → var=false
}
```

### Plan
```java
class Plan extends Vector<Predicate> {
    // A plan is just a sequence of predicates (actions)
    // Example: [goto(room1,room2), open(room2,key1), goto(room2,exit)]
}
```

---

## Summary of Algorithm Flow

```
┌─────────────────────────────────────────────────────────┐
│         START: Agent.cycle(world)                       │
├─────────────────────────────────────────────────────────┤
│                                                          │
│ 1. SENSE PHASE                                          │
│    ├─ world.generatePercepts()                         │
│    ├─ forwardChain(perceptRules ∪ percepts ∪ beliefs) │
│    └─ processFacts() → update beliefs, desires         │
│                                                          │
│ 2. THINK PHASE                                          │
│    ├─ forwardChain(programRules ∪ beliefs)            │
│    ├─ processFacts() → derive new beliefs & goals      │
│    └─ Generate intentions (possible actions)            │
│                                                          │
│ 3. DECIDE PHASE                                         │
│    ├─ If PLAN mode:                                    │
│    │  ├─ For each unsatisfied goal:                    │
│    │  │  └─ idSearch(maxDepth, beliefs, goal)         │
│    │  │     ├─ Try depth 1, 2, 3, ... until found    │
│    │  │     └─ depthFirst() searches each depth       │
│    │  └─ Select first action from first plan          │
│    │                                                    │
│    └─ Else if intentions available:                    │
│       └─ Select random action from intentions          │
│                                                          │
│ 4. ACT PHASE                                            │
│    ├─ world.executeAction(action)                      │
│    ├─ If successful:                                   │
│    │  ├─ forwardChain(actionRules ∪ action ∪ beliefs) │
│    │  └─ processFacts() → apply postconditions         │
│    └─ Otherwise: do nothing                             │
│                                                          │
└─────────────────────────────────────────────────────────┘
        Returns → Main loop continues next cycle
```

---

## Advanced Concepts

### Unification and Substitution

**Unification** finds the variable bindings needed to match two predicates:

```
Pattern: human(X)
Fact: human(socrates)
Result: {X → socrates}

Pattern: parent(X,Y)
Fact: parent(john,bob)
Result: {X → john, Y → bob}

Pattern: equal(X,X)
Fact: equal(mary,john)
Result: null (X can't be both mary and john)
```

**Substitution** applies these bindings:

```
Original: parent(X,Y) & married(X,Y)
Substitution: {X → john, Y → mary}
Result: parent(john,mary) & married(john,mary)
```

### Why Iterative Deepening for Planning?

Instead of exploring all possible plans, we explore plans of increasing depth:

```
Depth 1: Try all 1-action plans (fast, but unlikely to work)
Depth 2: Try all 2-action plans
Depth 3: Try all 3-action plans (may find solution here)
...
```

**Advantages**:
- ✓ Finds shortest plan
- ✓ Memory-efficient (restart search at each depth)
- ✓ Complete (will find solution if it exists within maxDepth)

**Trade-off**: Re-explores some states, but typically faster than depth-first alone.

### Negation in Conditions

`!predicate` means "predicate is NOT true in the current beliefs":

```
Rule: at(X) & !visited(X) > +visited(X)
Beliefs: at(room1)

The condition !visited(room1) is true because visited(room1) 
is not in beliefs → Rule fires
```

---

## Potential Extensions

The system could be extended with:

1. **Negation as failure**: Allow negated conclusions
2. **Disjunctive goals**: Multiple alternative goal paths
3. **Resource constraints**: Limit action budget or planning time
4. **Learning**: Remember which plans work well
5. **Continuous domains**: Real-valued state variables
6. **Temporal rules**: Actions with durations
7. **Goal priorities**: Weighted or ordered goals

---

## Debugging Tips

1. **Enable VERBOSE mode** to see detailed reasoning
2. **Check forward chaining output**: Are expected facts being derived?
3. **Verify unification**: Do pattern matching logs show correct substitutions?
4. **Trace planning**: Check if idSearch finds plans at expected depths
5. **Monitor state changes**: Use processFacts debug output
6. **Check rule syntax**: Ensure proper use of `>`, `&`, and operators

---

## Conclusion

This Logic and Planning system demonstrates:
- **Symbolic AI**: Using logical rules to represent knowledge
- **Inference**: Forward chaining to derive new facts
- **Planning**: Automated search for action sequences
- **BDI Architecture**: Separating beliefs, desires, and intentions
- **Iterative Deepening**: Finding optimal plans efficiently

The agent successfully navigates complex environments by reasoning about its beliefs, adopting goals, and planning action sequences to achieve them.
