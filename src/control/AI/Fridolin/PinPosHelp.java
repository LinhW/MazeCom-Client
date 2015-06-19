package control.AI.Fridolin;

import control.AI.Fridolin.ownClasses.Card;
import control.AI.Fridolin.ownClasses.Position;

public class PinPosHelp {
	private Position pinPos;
	private CardHelp ch;
	private Position trePos;

	public PinPosHelp(Position trePos, Position pinPos, CardHelp ch) {
		this.trePos = trePos;
		this.pinPos = pinPos;
		this.ch = ch;
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

}
