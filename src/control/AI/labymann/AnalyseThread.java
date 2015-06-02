package control.AI.labymann;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import model.Board;
import model.Card;
import model.jaxb.PositionType;

public class AnalyseThread extends Thread {
	public static class Parameter {
		private Board board;
		private int playerID;
		private int playerCount;
		private ArrayList<Move> moves;
		private ReentrantLock lock;
		private Side side;
		
		public Board getBoard() {
			return board;
		}
		public void setBoard(Board board) {
			this.board = board;
		}
		public int getPlayerID() {
			return playerID;
		}
		public void setPlayerID(int playerID) {
			this.playerID = playerID;
		}
		public int getPlayerCount() {
			return playerCount;
		}
		public void setPlayerCount(int playerCount) {
			this.playerCount = playerCount;
		}
		public ArrayList<Move> getMoves() {
			return moves;
		}
		public void setMoves(ArrayList<Move> moves) {
			this.moves = moves;
		}
		public ReentrantLock getLock() {
			return lock;
		}
		public void setLock(ReentrantLock lock) {
			this.lock = lock;
		}
		public Side getSide() {
			return side;
		}
		public void setSide(Side side) {
			this.side = side;
		}
	}
	
	public enum Side {UP, RIGHT, DOWN, LEFT}
	private enum Points {
		OWN_START_REACHABLE(Short.MAX_VALUE),
		OWN_TREASURE_REACHABLE((short) 100),
		OTHER_START_BLOCKED((short) 50),
		OTHER_TREASURE_REACHABLE((short) -3);
		
		private final short value;
		
		private Points(short v) {
			this.value = v;
		}
		
		private short getValue() {
			return value;
		}
	}
	
	private Parameter p;
	
	public AnalyseThread(Parameter p, Side side) {
		this.p = p;
	}
	
	private Move analyseBoard(Board board) {
		Move move = new Move();
		return move;
	}
	
	public void run() {
		Board temp_board;
		Card shiftCard = new Card(p.getBoard().getShiftCard());
		Move bestMove = new Move();
		Move tempMove;
		bestMove.setValue(Integer.MIN_VALUE);
		PositionType oldPinPos = p.getBoard().findPlayer(p.getPlayerID());
		for (int i = 1; i < 6; i += 2) {
			temp_board = (Board) p.getBoard().clone();
			tempMove = analyseBoard(temp_board);
			if (tempMove.compareTo(bestMove) == 1) {
				bestMove = tempMove;
			}
		}
		p.getLock().lock();
		p.getMoves().add(bestMove);
		p.getLock().unlock();
	}
}
