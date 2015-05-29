package model;

import model.jaxb.TreasureType;

public class PersData {

	private int id;
	private int move;
	private String name;

	private TreasureType currentTreasure;

	public PersData(String name) {
		setName(name);
	}

	public int getID() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getMove() {
		return move;
	}

	public void setCurrentTreasure(TreasureType currentTreasure) {
		this.currentTreasure = currentTreasure;
	}

	public void setMove(int move) throws Exception {
		if (move < this.move) {
			throw new Exception("Falsche Zugzuweisung");
		}
		this.move = move;
	}

	public TreasureType treasuresToGo() {
		// TODO Auto-generated method stub
		return null;
	}

	public TreasureType getCurrentTreasure() {
		return currentTreasure;
	}

}
