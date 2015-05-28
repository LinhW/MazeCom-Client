package ai;

import jaxb.TreasureType;
import gui.data.Board;
import gui.data.Card;
import gui.data.Position;
import gui.data.Card.CardShape;
import gui.data.Card.Orientation;

public class Util {

	public static Position createPosition(int row, int col) {
		return new Position(row, col);
	}

	public static Position getPinPos(Board b, int playerID) {
		return new Position(b.findPlayer(playerID));
	}

	public static Card getShiftCard(Board b) {
		return new Card(b.getShiftCard());
	}

	public static Card createCard(String cardType, int orientation, String treasureName) {
		return new Card(CardShape.valueOf(cardType), Orientation.fromValue(orientation), TreasureType.fromValue(treasureName));
	}

	public static Position getTreasurePosition(Board b, String treasureName) {
		return new Position(b.findTreasure(TreasureType.valueOf(treasureName)));
	}

	public static Position getTreasurePosition(Board b, TreasureType treasure) {
		return new Position(b.findTreasure(treasure));
	}
}
