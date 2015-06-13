package control.AI.LAMB;

import model.Card;
import model.Position;
import model.jaxb.PositionType;

public class Move implements Comparable<Object> {
	private Position shiftPosition;
	private Position movePosition;
	private Card shiftCard;
	private int value;
	
	public Move() {
		super();
	}
	
	public Move(Card shiftCard, Position shift, Position move, int value) {
		this.shiftPosition = shift;
		this.movePosition = move;
		this.shiftCard = shiftCard;
		this.value = value;
	}

	public void setShiftPosition(Position shiftPosition) {
		this.shiftPosition = new Position(shiftPosition);
	}

	public void setMovePosition(Position movePosition) {
		this.movePosition = movePosition;
	}

	public void setShiftCard(Card shiftCard) {
		this.shiftCard = new Card(shiftCard);
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
	
	public String toString() {
		return "VAL: " + value + " SC: (" + shiftCard.getShape() + "" + shiftCard.getOrientation() +
				") SP: (" + shiftPosition.getRow() +"," + shiftPosition.getCol() + 
				") MP: (" + movePosition.getRow() +"," + movePosition.getCol() + ")";
	}
}
