package leidenuniv.symbolicai;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import leidenuniv.symbolicai.logic.KB;
import leidenuniv.symbolicai.logic.Predicate;
import leidenuniv.symbolicai.logic.Sentence;
import leidenuniv.symbolicai.logic.Term;

public class MyAgent extends Agent {

	@Override
	public KB forwardChain(KB kb) {
		KB facts = new KB();
		HashMap<String, Predicate> factMap = new HashMap<>();
		boolean changed = true;
		while (changed) {
			changed = false;

			for (Sentence rule : kb.rules()) {
				Collection<HashMap<String, String>> allSubsts = new Vector<>();

				if (rule.conditions.isEmpty()) {
					allSubsts.add(new HashMap<>());
				} else {
					findAllSubstitutions(allSubsts, new HashMap<>(), rule.conditions, factMap);
				}

				for (HashMap<String, String> subst : allSubsts) {
					for (Predicate conclusion : rule.conclusions) {
						Predicate boundConclusion = substitute(conclusion, subst);

						if (boundConclusion.bound()) {
							String key = boundConclusion.toString();
							if (!factMap.containsKey(key)) {
								Sentence newFact = new Sentence(boundConclusion.toString());
								facts.add(newFact);
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

	@Override
	public boolean findAllSubstitutions(Collection<HashMap<String, String>> allSubstitutions,
			HashMap<String, String> substitution, Vector<Predicate> conditions, HashMap<String, Predicate> facts) {

		if (conditions.isEmpty()) {
			allSubstitutions.add(new HashMap<>(substitution));
			return true;
		}

		Predicate condition = conditions.get(0);
		Predicate substCondition = substitute(condition, substitution);
		Vector<Predicate> remainingConditions = new Vector<>(conditions.subList(1, conditions.size()));

		if (substCondition.not()) {
			if (substCondition.bound()) {
				if (!substCondition.getTerm(0).term.equals(substCondition.getTerm(1).term)) {
					return findAllSubstitutions(allSubstitutions, substitution, remainingConditions, facts);
				}
			}
			return false;
		}

		if (substCondition.eql()) {
			if (substCondition.bound()) {
				if (substCondition.getTerm(0).term.equals(substCondition.getTerm(1).term)) {
					return findAllSubstitutions(allSubstitutions, substitution, remainingConditions, facts);
				}
			}
			return false;
		}

		if (substCondition.neg) {
			boolean foundMatch = false;
			for (Predicate fact : facts.values()) {
				HashMap<String, String> unifier = unifiesWith(substCondition, fact);
				if (unifier != null) {
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch) {
				return findAllSubstitutions(allSubstitutions, substitution, remainingConditions, facts);
			}
			return false;
		}

		boolean foundAny = false;
		for (Predicate fact : facts.values()) {
			HashMap<String, String> unifier = unifiesWith(substCondition, fact);
			if (unifier != null) {
				HashMap<String, String> newSubst = new HashMap<>(substitution);
				newSubst.putAll(unifier);
				if (findAllSubstitutions(allSubstitutions, newSubst, remainingConditions, facts)) {
					foundAny = true;
				}
			}
		}

		return foundAny;
	}

	@Override
	public HashMap<String, String> unifiesWith(Predicate p, Predicate f) {
		// Returns the valid substitution for which p predicate unifies with f
		// You may assume that Predicate f is fully bound (i.e., it has no variables
		// anymore)
		// The result can be an empty substitution, if no subst is needed to unify p
		// with f (e.g., if p an f contain the same constants or do not have any terms)
		// Please note because f is bound and p potentially contains the variables,
		// unifiesWith is NOT symmetrical
		// So: unifiesWith("human(X)","human(joost)") returns X=joost, while
		// unifiesWith("human(joost)","human(X)") returns null
		// If no subst is found it returns null
		if (p.neg) {
			p = new Predicate(p.toString().substring(1));
		}

		if (!p.getName().equals(f.getName())) {
			return null;
		}

		if (p.getTerms().size() != f.getTerms().size()) {
			return null;
		}

		HashMap<String, String> subst = new HashMap<>();

		for (int i = 0; i < p.getTerms().size(); i++) {
			Term pTerm = p.getTerm(i);
			Term fTerm = f.getTerm(i);

			if (pTerm.var) {
				if (subst.containsKey(pTerm.term)) {
					if (!subst.get(pTerm.term).equals(fTerm.term)) {
						return null;
					}
				} else {
					subst.put(pTerm.term, fTerm.term);
				}
			} else {
				if (!pTerm.term.equals(fTerm.term)) {
					return null;
				}
			}
		}

		return subst;
	}

	@Override
	public Predicate substitute(Predicate old, HashMap<String, String> s) {
		// Substitutes all variable terms in predicate <old> for values in substitution
		// <s>
		// (only if a key is present in s matching the variable name of course)
		// Use Term.substitute(s)
		Predicate result = new Predicate(old.toString());
		for (Term t : result.getTerms()) {
			t.substitute(s);
		}
		return result;
	}

	@Override
	public Plan idSearch(int maxDepth, KB kb, Predicate goal) {
		System.out.println("=== IDSEARCH DEBUG ===");
		System.out.println("Goal: " + goal);
		System.out.println("Max depth: " + maxDepth);
		System.out.println("Initial KB size: " + kb.rules().size());

		for (int depth = 1; depth <= maxDepth; depth++) {
			System.out.println("DEBUG: Trying depth " + depth);
			KB stateCopy = new KB();
			for (Sentence s : kb.rules()) {
				stateCopy.add(s);
			}
			Plan plan = depthFirst(depth, 0, stateCopy, goal, new Plan());
			if (plan != null) {
				System.out.println("DEBUG: Found plan at depth " + depth + ": " + plan);
				System.out.println("=== END IDSEARCH (returning plan) ===");
				return plan;
			}
		}
		System.out.println("DEBUG: No plan found at any depth up to " + maxDepth);
		System.out.println("=== END IDSEARCH (returning null) ===");
		return null;
	}

	@Override
	public Plan depthFirst(int maxDepth, int depth, KB state, Predicate goal, Plan partialPlan) {
		System.out.println("  DEPTHFIRST: depth=" + depth + "/" + maxDepth + ", goal=" + goal);
		System.out.println("  DEPTHFIRST: partialPlan size=" + partialPlan.size());

		if (depth > maxDepth) {
			System.out.println("  DEPTHFIRST: Max depth reached, returning null");
			return null;
		}

		// Check if goal is satisfied in current state
		// Run forward chaining with program rules to derive all facts
		KB inferredFacts = forwardChain(programRules.union(state));

		// IMPORTANT: Strip operators from goal before checking
		// Goals come in as *at(X) but we check against facts like at(X)
		Predicate goalWithoutOperator = goal;
		if (goal.adopt || goal.drop || goal.add || goal.del || goal.act) {
			// Create version without operator
			goalWithoutOperator = new Predicate(goal.toString().substring(1));
		}

		// Try to unify goal with any inferred fact (ignoring action operators)
		for (Sentence s : inferredFacts.rules()) {
			Predicate fact = new Predicate(s);

			// Skip facts that are actions/operators - we only check regular facts
			if (fact.isAction()) {
				continue;
			}

			HashMap<String, String> unifier = unifiesWith(goalWithoutOperator, fact);
			if (unifier != null) {
				System.out.println("  DEPTHFIRST: Goal " + goal + " satisfied by fact " + fact);
				System.out.println("  DEPTHFIRST: Returning plan: " + partialPlan);
				return partialPlan;
			}
		}

		// Goal not yet satisfied - get available actions from current state
		KB tempDesires = new KB();
		KB intentions = new KB();

		// Think uses program rules on state to generate intentions
		think(state, tempDesires, intentions);

		System.out.println("  DEPTHFIRST: Generated " + intentions.rules().size() + " intentions");

		if (intentions.rules().isEmpty()) {
			System.out.println("  DEPTHFIRST: No actions available, returning null");
			return null;
		}

		// Try each available action
		for (Sentence intentionSentence : intentions.rules()) {
			Predicate action = new Predicate(intentionSentence);
			System.out.println("  DEPTHFIRST: Trying action: " + action);

			// Create new plan with this action
			Plan newPlan = new Plan(partialPlan);
			newPlan.add(action);

			// Create a complete copy of current state for simulation
			KB newState = new KB();
			for (Sentence s : state.rules()) {
				newState.add(s);
			}

			// Simulate action by applying action rules
			// This updates newState with postconditions (e.g., +at(Y), -at(X), +hasKey(K),
			// -atKey(X,K))
			KB tempDesires2 = new KB();
			act(null, action, newState, tempDesires2);

			// Recursively search from new state
			Plan result = depthFirst(maxDepth, depth + 1, newState, goal, newPlan);
			if (result != null) {
				System.out.println("  DEPTHFIRST: Found successful plan: " + result);
				return result;
			}

			System.out.println("  DEPTHFIRST: Action " + action + " didn't lead to goal, trying next");
		}

		System.out.println("  DEPTHFIRST: No plan found from this state");
		return null;
	}
}
