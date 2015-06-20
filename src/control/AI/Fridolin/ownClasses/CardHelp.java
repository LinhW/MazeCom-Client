package control.AI.Fridolin.ownClasses;


public class CardHelp {
	private Card c;
	private Position p;

	public CardHelp(Card c, Position p) {
		this.c = c;
		this.p = p;
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

}
