package control.AI.LAMB;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import model.Board;

public class Parameter {
	private Board board;
	private int playerID;
	private ArrayList<Move> moves;
	private ReentrantLock lock;
	
	public Parameter() {
		
	}
	
	@SuppressWarnings("unchecked")
	public Parameter(Parameter p) {
		this.board = (Board) p.board.clone();
		this.playerID = p.playerID;
		this.moves = (ArrayList<Move>) p.moves.clone();
		this.lock = new ReentrantLock();
	}
	
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
}