package control.AI.MNA;

import model.Board;
import model.Card;
import model.Position;
import model.jaxb.BoardType;
import model.jaxb.CardType;
import model.jaxb.PositionType;

public class MNA_Move implements Comparable<Object>{
	private Card shiftCard;
	private Position shiftPosition;
	private Position movePosition;
	private Board afterMove;
	private Position treasurePosition;
	
	private int value;
	
	public MNA_Move() {
		
	}
	
	public MNA_Move(MNA_Move move) {
		if (move.shiftCard != null) {
			this.shiftCard = new Card(move.shiftCard);
		}
		else {
			this.shiftCard = null;
		}
		
		if (move.shiftPosition != null) {
			this.shiftPosition = new Position(move.shiftPosition);
		}
		else {
			this.shiftPosition = null;
		}
		
		if (move.movePosition != null) {
			this.movePosition = new Position(move.movePosition);
		}
		else {
			this.movePosition = null;
		}
		
		if (move.afterMove != null) {
			this.afterMove = new Board(move.afterMove);
		}
		else {
			this.afterMove = null;
		}
		
		if (move.treasurePosition != null) {
			this.treasurePosition = new Position(move.treasurePosition);
		}
		else {
			this.treasurePosition = null;
		}
		
		this.value = move.value;
	}

	public Card getShiftCard() {
		return shiftCard;
	}

	public void setShiftCard(CardType shiftCard) {
		this.shiftCard = new Card(shiftCard);
	}

	public Position getShiftPosition() {
		return shiftPosition;
	}

	public void setShiftPosition(PositionType shiftPosition) {
		this.shiftPosition = new Position(shiftPosition);
	}

	public Position getMovePosition() {
		return movePosition;
	}

	public void setMovePosition(PositionType movePosition) {
		this.movePosition = new Position(movePosition);
	}

	public Board getAfterMove() {
		return afterMove;
	}

	public void setAfterMove(BoardType afterMove) {
		this.afterMove = new Board(afterMove);
	}

	public Position getTreasurePosition() {
		return treasurePosition;
	}

	public void setTreasurePosition(PositionType treasurePosition) {
		this.treasurePosition = new Position(treasurePosition);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public void addValue(int value) {
		this.value = this.value + value;
	}
	
	@Override
	public int compareTo(Object o) {
		if (this.value > ((MNA_Move) o).value) {
			return 1;
		}
		else if (this.value == ((MNA_Move) o).value) {
			return 0;
		}
		else {
			return -1;
		}
	}

	public String toString() {
		return "VAL: " + value + " SC: (" + shiftCard.getShape() + "" + shiftCard.getOrientation()
				+ ") SP: (" + shiftPosition.getRow() + "," + shiftPosition.getCol() + ") MP: ("
				+ movePosition.getRow() + "," + movePosition.getCol() + ")";
	}
}
