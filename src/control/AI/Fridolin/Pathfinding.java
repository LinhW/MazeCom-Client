package control.AI.Fridolin;

import java.util.ArrayList;
import java.util.List;

import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;
import control.AI.Fridolin.ownClasses.Board;
import control.AI.Fridolin.ownClasses.Card;
import control.AI.Fridolin.ownClasses.Position;

public class Pathfinding {
	private final int x = 7;
	private final int y = 7;
	private Board betterBoard;
	private int id;
	private List<TreasuresToGoType> list_treToGo;
	private List<TreasureType> list_foundTreasures;
	private Integer[] nextPlayer;

	public Pathfinding(int id) {
		this.id = id;
	}

	public void setBoard(Board b) {
		this.betterBoard = b;
	}

	public void setTreToGo(List<TreasuresToGoType> list) {
		this.list_treToGo = list;

		int i = id + 1;
		nextPlayer = new Integer[list.size() - 1];
		for (int j = 0; j < nextPlayer.length; j++) {
			if (i > list.size()) {
				i = 1;
			}
			nextPlayer[j] = i;
			i++;
		}
	}

	public void setFoundTreasures(List<TreasureType> foundTreasures) {
		this.list_foundTreasures = foundTreasures;
	}

	/**
	 * start calculating the best turn
	 * 
	 * @return PinPosHelp with PinPos, ShiftPos and ShiftCard
	 */
	public PinPosHelp start() {
		// TODO
		return null;
	}

	/**
	 * take another move
	 * 
	 * @return the second best move
	 */
	public PinPosHelp getNewMove() {
		// TODO
		return null;
	}
}
