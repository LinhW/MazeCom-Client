package control.AI.Fridolin.ownClasses;

import java.util.List;


public class PinPosHelp {
	private Position pinPos;
	private CardHelp ch;
	private Position trePos;
	private double rating = 0;

	public PinPosHelp(Position trePos, Position pinPos, CardHelp ch) {
		this.trePos = trePos;
		this.pinPos = pinPos;
		this.ch = ch;
	}
	
	public PinPosHelp(Position trePos, Position pinPos, CardHelp ch, double Rating) {
		this.trePos = trePos;
		this.pinPos = pinPos;
		this.ch = ch;
		this.rating = Rating;
	}


	public Position getPinPos() {
		return pinPos;
	}

	public void setPinPos(Position p) {
		this.pinPos = p;
	}

	public Position getTrePos() {
		return this.trePos;
	}

	public void setTrePos(Position trePos) {
		this.trePos = trePos;
	}

	public CardHelp getCardHelp() {
		return ch;
	}
	
	public Card getShiftCard(){
		return ch.getCard();
	}
	
	public Position getShiftPos(){
		return ch.getPos();
	}

	public void setCardHelp(CardHelp ch) {
		this.ch = ch;
	}

	public String toString() {
		return "PinPos: " + pinPos + " " + ch;
	}

	public String debug() {
		if (ch != null) {
			return "TrePos: " + trePos + " PinPos: " + pinPos + " " + ch.debug();
		} else {
			return "PinPos: " + pinPos + " " + " null";
		}
	}

	public boolean equals(PinPosHelp pph) {
		return pph.ch.equals(this.getCardHelp()) && this.pinPos.equals(pph.getPinPos());
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating += rating;
	}
	
	public static PinPosHelp getLowestRating(List<PinPosHelp> list){
		double min = Double.MAX_VALUE;
		PinPosHelp p = null;
		for (PinPosHelp pph: list){
			if (pph.getRating() < min){
				min = pph.getRating();
				p = pph;
			}
		}
		return p;
	}

}
