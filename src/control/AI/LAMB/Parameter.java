package control.AI.LAMB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import model.Board;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;

public class Parameter {
	private Board board;
	private int playerID;
	private int playerCount;
	private TreasureType treasure;
	private ArrayList<Move> moves;
	private List<TreasuresToGoType> treasuresToGo;
	private List<TreasureType> treasuresFound;
	private ReentrantLock lock;
	
	public Parameter() {
		
	}
	
	@SuppressWarnings("unchecked")
	public Parameter(Parameter p) {
		this.board = (Board) p.board.clone();
		this.playerID = p.playerID;
		this.playerCount = p.playerCount;
		this.treasure = p.treasure;
		this.moves = (ArrayList<Move>) p.moves.clone();
		this.treasuresToGo = new ArrayList<TreasuresToGoType>();
		this.treasuresToGo.addAll(p.treasuresToGo);
		this.treasuresFound = new ArrayList<TreasureType>();
		this.treasuresFound.addAll(p.treasuresFound);
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
	public int getPlayerCount() {
		return playerCount;
	}
	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}
	public TreasureType getTreasure() {
		return treasure;
	}
	public void setTreasure(TreasureType treasure) {
		this.treasure = treasure;
	}
	public ArrayList<Move> getMoves() {
		return moves;
	}
	public void setMoves(ArrayList<Move> moves) {
		this.moves = moves;
	}
	public List<TreasuresToGoType> getTreasuresToGo() {
		return treasuresToGo;
	}
	public void setTreasuresToGo(List<TreasuresToGoType> treasuresToGo) {
		this.treasuresToGo = treasuresToGo;
	}
	public List<TreasureType> getTreasuresFound() {
		return treasuresFound;
	}
	public void setTreasuresFound(List<TreasureType> treasuresFound) {
		this.treasuresFound = treasuresFound;
	}
	public ReentrantLock getLock() {
		return lock;
	}
	public void setLock(ReentrantLock lock) {
		this.lock = lock;
	}
}