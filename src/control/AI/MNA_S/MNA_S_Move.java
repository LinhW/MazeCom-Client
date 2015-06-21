package control.AI.MNA_S;

import model.Card;
import model.Position;
import model.jaxb.CardType;
import model.jaxb.PositionType;

public class MNA_S_Move implements Comparable<Object> {	
	private Position shiftPosition;
	private Position movePosition;
	private Card shiftCard;
	private int value;

	public MNA_S_Move() {
		super();
	}

	public MNA_S_Move(MNA_S_Move move) {
		this.shiftPosition = new Position(move.shiftPosition);
		if (move.movePosition != null) {
			this.movePosition = new Position(move.movePosition);
		}
		this.shiftCard = new Card(move.shiftCard);
		this.value = move.value;
	}

	public void setShiftPosition(PositionType shiftPosition) {
		this.shiftPosition = new Position(shiftPosition);
	}

	public void setMovePosition(PositionType movePosition) {
		this.movePosition = new Position(movePosition);
	}

	public void setShiftCard(CardType shiftCard) {
		this.shiftCard = new Card(shiftCard);
	}

	public Position getShiftPosition() {
		return shiftPosition;
	}

	public Position getMovePosition() {
		return movePosition;
	}

	public Card getShiftCard() {
		return shiftCard;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public int compareTo(Object o) {
		if (this.value > ((MNA_S_Move) o).value) {
			return 1;
		}
		else if (this.value == ((MNA_S_Move) o).value) {
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
