package control.AI.LAMB;

import model.Card;
import model.jaxb.PositionType;

public class Move implements Comparable<Object> {
	private PositionType shiftPosition;
	private PositionType movePosition;
	private Card shiftCard;
	private int value;
	
	public Move() {
		super();
	}
	
	public Move(Card shiftCard, PositionType shift, PositionType move, int value) {
		this.shiftPosition = shift;
		this.movePosition = move;
		this.shiftCard = shiftCard;
		this.value = value;
	}

	public void setShiftPosition(PositionType shiftPosition) {
		this.shiftPosition = shiftPosition;
	}

	public void setMovePosition(PositionType movePosition) {
		this.movePosition = movePosition;
	}

	public void setShiftCard(Card shiftCard) {
		this.shiftCard = shiftCard;
	}

	public PositionType getShiftPosition() {
		return shiftPosition;
	}

	public PositionType getMovePosition() {
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
		if (this.value > ((Move) o).value) {
			return 1;
		}
		else if (this.value == ((Move) o).value) {
			return 0;
		}
		else {
			return -1;
		}
	}
}
