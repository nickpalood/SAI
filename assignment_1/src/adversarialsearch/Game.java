package adversarialsearch;

import java.util.Vector;

public class Game {

    State b;           // our main game board state
    int nodesVisited;  // count how many game states we look at during search

    public Game() {
        b = new State();                // create a fresh game state
        b.read("data/board.txt");       // load the starting board from file
    }

    ////////////////////////
    ///// BASIC MINIMAX ////
    ////////////////////////

    // it imagines all possible moves
    // picks the one that leads to the best outcome, assuming both players play perfectly
    public State minimax(State s, int forAgent, int maxDepth, int depth) {
        nodesVisited++; // every time we examine a game state, count it

        // stop searching if we've looked far enough ahead OR the game is over
        // (no point looking further if someone already won)
        if (depth >= maxDepth || s.isLeaf()) {
            return s; // this is as far as we can/need to look
        }

        State bestState = null; // remember the best move we find here

        // figure out what the current player wants to accomplish
        if (s.turn == forAgent) {
            // it's our agent's turn, we want the HIGHEST possible score
            double bestValue = Double.NEGATIVE_INFINITY; // start with the worst possible value

            // try every single move our agent could make right now
            for (String move : s.legalMoves()) {
                State nextState = s.copy(); // make a copy so to not mess up the original game
                nextState.execute(move);    // try making this move

                // now look ahead to see what happens after this move
                // (the opponent will respond, then we'll respond to that, etc.)
                State resultState = minimax(nextState, forAgent, maxDepth, depth + 1);
                double value = resultState.value(forAgent); // how good is the final outcome?

                // if this move leads to a better result than what we've seen so far, remember it
                if (value > bestValue) {
                    bestValue = value;      // this is our new best score
                    bestState = nextState;  // this is the move that gets us there
                }
            }
        } else {
            // it's the OPPONENT's turn - they want to make OUR score as LOW as possible
            double bestValue = Double.POSITIVE_INFINITY; // start with the best possible value for us

            // try every move the opponent could make
            for (String move : s.legalMoves()) {
                State nextState = s.copy(); // copy the game state
                nextState.execute(move);    // execute the opponent's move

                // see what happens after the opponent makes this move
                State resultState = minimax(nextState, forAgent, maxDepth, depth + 1);
                double value = resultState.value(forAgent); // how good is this for us?

                // the opponent will pick whichever move is WORST for us
                if (value < bestValue) {
                    bestValue = value;      // this is worse for us (better for opponent)
                    bestState = nextState;  // opponent will probably choose this
                }
            }
        }

        return bestState; // return the best state we found after looking ahead
    }

    //////////////////////////////
    ///// ALPHA-BETA PRUNING /////
    //////////////////////////////

    public State alfabeta(State s, int forAgent, int maxDepth, int depth, double alfa, double beta) {
        nodesVisited++; // count this node visit

        // stop searching if we've looked far enough ahead OR the game is over
        // (no point looking further if someone already won)
        // (same stopping conditions as regular minimax)
        if (depth >= maxDepth || s.isLeaf()) {
            return s;
        }

        State bestState = null;

        if (s.turn == forAgent) {
            // our turn - we want to maximize our score
            double bestValue = Double.NEGATIVE_INFINITY;

            for (String move : s.legalMoves()) {
                State nextState = s.copy();
                nextState.execute(move);

                // keep searching deeper with our alpha-beta bounds
                State resultState = alfabeta(nextState, forAgent, maxDepth, depth + 1, alfa, beta);
                double value = resultState.value(forAgent);

                if (value > bestValue) {
                    bestValue = value;
                    bestState = nextState;
                }

                // here's the alpha-beta magic: update our "guaranteed minimum" score
                alfa = Math.max(alfa, bestValue);

                // if the opponent already has a better option somewhere else,
                // they'll never let us get to this branch anyway so cancel looking
                if (beta <= alfa) {
                    break; // this is the "pruning" - we skip the remaining moves
                }
            }
        } else {
            // opponent's turn - they want to minimize our score
            double bestValue = Double.POSITIVE_INFINITY;

            for (String move : s.legalMoves()) {
                State nextState = s.copy();
                nextState.execute(move);

                State resultState = alfabeta(nextState, forAgent, maxDepth, depth + 1, alfa, beta);
                double value = resultState.value(forAgent);

                if (value < bestValue) {
                    bestValue = value;
                    bestState = nextState;
                }

                // update the opponent's "guaranteed maximum" (minimum for us)
                beta = Math.min(beta, bestValue);

                // if we already have a better option elsewhere, stop looking here!
                if (beta <= alfa) {
                    break; // prune this branch too
                }
            }
        }

        return bestState;
    }

    ///////////////////////////////////////////////////////
    ///// TEST BOTH ALGORITHMS AND SEE THE DIFFERENCE /////
    ///////////////////////////////////////////////////////

    public void test() {
        System.out.println("Testing Minimax vs Alpha-Beta Pruning");
        System.out.println("======================================");
        System.out.println();

        // test different search depths and see how they compare
        for (int depth = 3; depth <= 13; depth++) {
            System.out.println("Testing at depth " + depth + ":");

            // first try regular minimax and see how many nodes it visits
            nodesVisited = 0;
            long startTime = System.currentTimeMillis();
            State minimaxResult = minimax(b, b.turn, depth, 0);
            long minimaxTime = System.currentTimeMillis() - startTime;
            int minimaxNodes = nodesVisited;

            System.out.println("  Minimax visited " + minimaxNodes + " nodes");
            System.out.println("  Minimax took " + minimaxTime + " milliseconds");

            // now try alpha-beta and see how much faster it is
            nodesVisited = 0;
            startTime = System.currentTimeMillis();
            State alfabetaResult = alfabeta(b, b.turn, depth, 0,
                    Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            long alfabetaTime = System.currentTimeMillis() - startTime;
            int alfabetaNodes = nodesVisited;

            System.out.println("  Alpha-beta visited " + alfabetaNodes + " nodes");
            System.out.println("  Alpha-beta took " + alfabetaTime + " milliseconds");

            // calculate how much better alpha-beta performed
            if (alfabetaNodes > 0) {
                double reduction = (double) minimaxNodes / alfabetaNodes;
                System.out.println("  Alpha-beta visited " + String.format("%.2f", reduction) + "x fewer nodes!");

                if (alfabetaTime > 0) {
                    double speedup = (double) minimaxTime / alfabetaTime;
                    System.out.println("  Alpha-beta was " + String.format("%.2f", speedup) + "x faster!");
                }
            }

            System.out.println();
        }

        System.out.println("=========");
        System.out.println();

        // show what the best move looks like at a reasonable depth
        System.out.println("Example: Best move found at depth 7");
        System.out.println("===================================");
        nodesVisited = 0;
        State bestMove = alfabeta(b, b.turn, 7, 0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        System.out.println("Alpha-beta visited " + nodesVisited + " nodes to find this move:");
        System.out.println();
        System.out.println(bestMove.toString());
        System.out.println("Agent " + b.turn + "'s scores: Agent 0 = " + bestMove.score[0] +
                ", Agent 1 = " + bestMove.score[1]);
        System.out.println("Food remaining: " + bestMove.food);
        System.out.println("Moves made: " + bestMove.moves);
    }
}