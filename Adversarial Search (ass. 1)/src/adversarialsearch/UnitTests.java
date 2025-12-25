package adversarialsearch;
public class UnitTests {
	public static State s=new State(), copy;

	public static void main(String[] args) {
		System.out.println("Unit testing");

		try {
			testRead();
			testCopy();
			testLegalMoves();
			testLeafValue();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	public static void testRead() throws Exception{
		//Check board read
		System.out.println("Testing read()");
		s.read("data/board.txt");
		if (s.board[1][2]!='*' | s.board[2][3]!='*') {
			throw new Exception("Unit test error: reading board: Food not found at right location in board array, perhaps flipped dimensions?") ;
		} else if (s.food!=2) {
			throw new Exception("Unit test error: reading board: Food amount not 2") ;
		} else if (s.agentX[0]!=2 | s.agentY[0]!=2) {
			throw new Exception("Unit test error: reading board: Agent A not at right location");
		} else if (s.agentX[1]!=2 | s.agentY[1]!=3) {
			throw new Exception("Unit test error: reading board: Agent B not at right location");
		} else if (s.board[2][2]=='A' | s.board[3][2]=='B') {
			throw new Exception("Unit test error: reading board: You should remove A and B from s.board after reading, and only keep track of the agents with the coordinates");
		}

	}
	public static void testCopy() throws Exception {
		System.out.println("Testing copy()");
		copy=s.copy();
		if (!copy.toString().equals(s.toString())){
			throw new Exception("Unit test error: copy: the toString of copy must be equal to the toString of original.") ;
		} else if (s.turn!=copy.turn | s.food!=copy.food | s.score[0]!=copy.score[0] | s.score[1]!=copy.score[1] |
					s.agentX[0]!=copy.agentX[0] | s.agentX[1]!=copy.agentX[1] | s.agentY[0]!=copy.agentY[0] | s.agentY[1]!=copy.agentY[1]){
			throw new Exception("Unit test error: copy: agentX, agentY, score, food or turn properties not the same for copy.") ;
		} else
		{
			s.board[0][0]='D';
			if (copy.board[0][0]=='D') {
				throw new Exception("Unit test error: copy: board not a deep copy. Check if you properly clone arrays.") ;
			}
		}
	}
	public static void testLegalMoves() throws Exception {
		System.out.println("Testing legalMoves() for agent A");
		s.read("data/board.txt");
		s.turn=0;
		if (s.legalMoves().size()!=4) {
			throw new Exception("Unit test error: legalMoves: should have 4 legal moves at start positions for A.") ;
		}
		s.agentY[0]=1;
		if (s.legalMoves().size()!=3) {
			throw new Exception("Unit test error: legalMoves: should have 3 legal moves at upper food location after moving agent A there.") ;
		}
		System.out.println("Testing legalMoves() for agent B");
		s.turn=1;
		if (s.legalMoves().size()!=3) {
			throw new Exception("Unit test error: legalMoves: should have 3 legal moves at start positions for B.") ;
		}
		s.agentX[1]=3;
		if (s.legalMoves().size()!=2) {
			throw new Exception("Unit test error: legalMoves: should have 2 legal moves at wall location right from start positions for B.") ;
		}
	}

	public static void testLeafValue() throws Exception {
		System.out.println("Testing leaf() and value() for agent A");
		s.read("data/board.txt");
		if (s.isLeaf()) {
			throw new Exception("Unit test error: leaf: start board should not be a leaf") ;
		}
		if (s.value(0)!=0 | s.value(1)!=0) {
			throw new Exception("Unit test error: leaf: start board should have value 0 for both agents") ;
		}
		s.score[0]=1;
		if (s.value(0)!=0 | s.value(1)!=0) {
			throw new Exception("Unit test error: value: if state is not a leaf, value must be 0 even if one of the agents has a higher score") ;
		}
		s.score[0]=0;
		s.board[3][1]='#';
		s.board[3][2]='#';
		s.board[2][2]='#';

		if (!s.isLeaf()| s.value(0)!=1 | s.value(1)!=-1) {
			throw new Exception("Unit test error: leaf: when completely blocking agent B, it has no more moves, the state must be a leaf and the value must be 1 for A and -1 for B.") ;
		}
		s.food=0;
		if (!s.isLeaf()) {
			throw new Exception("Unit test error: leaf: when all food is eaten, the state must be a leaf.") ;
		}
	}
}
