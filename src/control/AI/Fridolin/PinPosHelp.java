package control.AI.Fridolin;

import java.util.ArrayList;
import java.util.List;

import control.AI.Fridolin.ownClasses.Card;
import control.AI.Fridolin.ownClasses.Position;

public class PinPosHelp {
	private Position pinPos;
	private int diff = Integer.MAX_VALUE;
	private CardHelp ch;
	private Position trePos;

	public PinPosHelp(Position trePos, Position PinPos, CardHelp ch) {
		this(trePos, PinPos, ch, Integer.MAX_VALUE);
	}

	public PinPosHelp(Position trePos, Position pinPos, CardHelp ch, int diff) {
		this.trePos = trePos;
		this.pinPos = pinPos;
		this.ch = ch;
		this.diff = diff;
	}

	public Position getPinPos() {
		return pinPos;
	}

	public void setPinPos(Position p) {
		this.pinPos = p;
	}

	public int getDiff() {
		return diff;
	}

	public Position getTrePos() {
		return this.trePos;
	}

	public void setTrePos(Position trePos) {
		this.trePos = trePos;
	}

	public static List<PinPosHelp> getSmallestDiff(List<PinPosHelp> list) {
		List<PinPosHelp> res = new ArrayList<>();
		int min = Integer.MAX_VALUE;
		int diff;
		for (PinPosHelp pph : list) {
			diff = pph.getDiff();
			if (diff == min) {
				res.add(pph);
			}
			if (diff < min) {
				min = diff;
				res.clear();
				res.add(pph);
			}
		}
		return res;
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
			return "TrePos: " + trePos + " PinPos: " + pinPos + " " + ch.debug() + "\t" + diff;
		} else {
			return "PinPos: " + pinPos + " " + " null\t" + diff;
		}
	}

	public boolean equals(PinPosHelp pph) {
		return pph.ch.equals(this.getCardHelp()) && this.pinPos.equals(pph.getPinPos());
	}

}
