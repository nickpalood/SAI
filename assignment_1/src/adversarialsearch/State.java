package adversarialsearch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class State {

    ////////////////////////////////
    ///// GAME STATE VARIABLES /////
    ////////////////////////////////

    char[][] board;     // the board as a 2D character array [height][width]
    int[] agentX;       // the x-coordinates of the agents, agentX[0] = x_agent0
    int[] agentY;       // the y-coordinates of the agents, agentY[0] = y_agent0
    int[] score;        // the amount of food eaten by each agent
    int turn;           // who's turn it is, agent 0 or agent 1
    int food;           // the total amount of food still available
    Vector<String> moves; // list of moves executed so far

    /////////////////////////////////////////
    ///// CONSTRUCTOR - INITIALIZE GAME /////
    /////////////////////////////////////////

    public State() {
        agentX = new int[2];      // room for both players' x positions
        agentY = new int[2];      // room for both players' y positions
        score = new int[2];       // room for both players' scores
        turn = 0;                 // agent 0 goes first
        food = 0;                 // no food counted yet
        moves = new Vector<String>(); // empty move history
    }

    /////////////////////////////////////
    ///// READ BOARD FROM TEXT FILE /////
    /////////////////////////////////////

    public void read(String file) {
        try {
            // open file and get board dimensions from first line
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            String[] dimensions = line.split(" ");
            int w = Integer.parseInt(dimensions[0]);  // board width
            int h = Integer.parseInt(dimensions[1]);  // board height

            // create board with proper size and reset food counter
            board = new char[h][w];
            food = 0;

            // read each row of the maze
            for (int row = 0; row < h; row++) {
                line = reader.readLine();
                // check each character in this row
                for (int col = 0; col < w; col++) {
                    char ch = line.charAt(col);
                    if (ch == 'A') {
                        // found player A's starting position
                        agentX[0] = col;
                        agentY[0] = row;
                        board[row][col] = ' '; // player moves around, so put empty space
                    } else if (ch == 'B') {
                        // found player B's starting position
                        agentX[1] = col;
                        agentY[1] = row;
                        board[row][col] = ' '; // player moves around, so put empty space
                    } else if (ch == '*') {
                        // found food - keep it and count it
                        board[row][col] = ch;
                        food++;
                    } else {
                        // walls (#) or empty spaces ( ) - just copy them
                        board[row][col] = ch;
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace(); // something went wrong reading the file
        }
    }

    ////////////////////////////////////////
    ///// CONVERT GAME STATE TO STRING /////
    ////////////////////////////////////////

    public String toString() {
        StringBuilder result = new StringBuilder();

        // go through each row and column of the board
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                // check if both players are standing on the same spot
                if (agentX[0] == col && agentY[0] == row && agentX[1] == col && agentY[1] == row) {
                    result.append("AB"); // show both players
                } else if (agentX[0] == col && agentY[0] == row) {
                    result.append("A");  // show player A
                } else if (agentX[1] == col && agentY[1] == row) {
                    result.append("B");  // show player B
                } else {
                    // no player here, so show whatever is on the board (food, wall, or empty)
                    if (board[row][col] == '*') {
                        result.append("*"); // show food
                    } else {
                        result.append(board[row][col]); // show wall or empty space
                    }
                }
            }
            result.append("\n"); // end this row and start a new line
        }
        return result.toString();
    }

    //////////////////////////////////////////
    ///// CREATE DEEP COPY OF GAME STATE /////
    //////////////////////////////////////////

    public State copy() {
        State copy = new State();

        // copy the entire board piece by piece
        copy.board = new char[board.length][board[0].length];
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                copy.board[row][col] = board[row][col];
            }
        }

        // copy both players' positions and scores
        for (int player = 0; player < 2; player++) {
            copy.agentX[player] = agentX[player];
            copy.agentY[player] = agentY[player];
            copy.score[player] = score[player];
        }

        // copy game state info
        copy.turn = turn;
        copy.food = food;

        // copy the history of moves made so far
        for (String move : moves) {
            copy.moves.add(move);
        }

        return copy;
    }

    /////////////////////////////////////////
    ///// FIND VALID MOVES FOR A PLAYER /////
    /////////////////////////////////////////

    public Vector<String> legalMoves(int agent) {
        Vector<String> validMoves = new Vector<String>();
        int currentX = agentX[agent];  // where is this player right now?
        int currentY = agentY[agent];

        // check if player can move in each direction (can't walk through walls)
        if (currentY > 0 && board[currentY-1][currentX] != '#') validMoves.add("up");
        if (currentX < board[0].length-1 && board[currentY][currentX+1] != '#') validMoves.add("right");
        if (currentY < board.length-1 && board[currentY+1][currentX] != '#') validMoves.add("down");
        if (currentX > 0 && board[currentY][currentX-1] != '#') validMoves.add("left");

        // check if player can eat (only if standing on food)
        if (board[currentY][currentX] == '*') validMoves.add("eat");

        // check if player can place a wall (only if standing on empty space)
        if (board[currentY][currentX] == ' ') validMoves.add("block");

        return validMoves;
    }

    ////////////////////////////////////////
    ///// GET MOVES FOR CURRENT PLAYER /////
    ////////////////////////////////////////

    public Vector<String> legalMoves() {
        return legalMoves(turn); // get moves for whoever's turn it is
    }

    ///////////////////////////////////
    ///// EXECUTE A PLAYER'S MOVE /////
    ///////////////////////////////////

    public void execute(String action) {
        int currentPlayer = turn;          // who is making this move?
        int playerX = agentX[currentPlayer]; // where are they now?
        int playerY = agentY[currentPlayer];

        // do whatever action the player chose
        switch (action) {
            case "up":
                agentY[currentPlayer]--; // move player up (y gets smaller)
                break;
            case "right":
                agentX[currentPlayer]++; // move player right (x gets bigger)
                break;
            case "down":
                agentY[currentPlayer]++; // move player down (y gets bigger)
                break;
            case "left":
                agentX[currentPlayer]--; // move player left (x gets smaller)
                break;
            case "eat":
                score[currentPlayer]++;   // player gets a point for eating food
                board[playerY][playerX] = ' '; // remove food from board
                food--;                   // one less food piece in the game
                break;
            case "block":
                board[playerY][playerX] = '#'; // place a wall where player is standing
                break;
        }

        // remember this move and switch to the other player
        moves.add(action);
        turn = 1 - turn; // flip between 0 and 1 (if 0 becomes 1, if 1 becomes 0)
    }

    /////////////////////////////////////
    ///// CHECK IF GAME IS FINISHED /////
    /////////////////////////////////////

    public boolean isLeaf() {
        // game ends if all food has been eaten
        if (food == 0) return true;

        // game ends if current player can't make any moves
        if (legalMoves(turn).isEmpty()) return true;

        // game is still going
        return false;
    }

    ////////////////////////////////////////////
    ///// CALCULATE GAME RESULT FOR PLAYER /////
    ////////////////////////////////////////////

    public double value(int agent) {
        // only calculate winner if game is actually over
        if (!isLeaf()) return 0;

        // if all food eaten, whoever ate more wins
        if (food == 0) {
            if (score[agent] > score[1-agent]) return 1;   // this player won
            if (score[agent] < score[1-agent]) return -1;  // this player lost
            return 0; // tie game - both ate same amount
        }

        // if current player has no moves, they lose immediately
        if (legalMoves(turn).isEmpty()) {
            if (turn == agent) return -1; // this player is stuck, so they lose
            else return 1; // other player is stuck, so this player wins
        }

        return 0; // shouldn't reach here, but just in case
    }
}
