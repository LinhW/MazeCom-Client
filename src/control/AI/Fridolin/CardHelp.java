package control.AI.Fridolin;

import control.AI.Fridolin.ownClasses.Card;
import control.AI.Fridolin.ownClasses.Position;

public class CardHelp {
	private Card c;
	private Position p;
	private int diff;

	public CardHelp(Card c, Position p) {
		this.c = c;
		this.p = p;
	}

	public CardHelp(Card c, Position p, int diff) {
		this.c = c;
		this.p = p;
		this.diff = diff;
	}

	public Card getCard() {
		return c;
	}

	public Position getPos() {
		return p;
	}

	public String toString() {
		return "CardPos: " + p + "\n" + c;
	}

	public String debug() {
		return "CardPos: " + p + " Card: " + c.getShape() + c.getOrientation().value();
	}

	public boolean equals(CardHelp ch) {
		return this.c.equals(ch.getCard()) && this.p.equals(ch.getPos());
	}

	public int getDiff() {
		return diff;
	}

}
