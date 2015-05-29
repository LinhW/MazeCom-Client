package control.AI;

import java.util.List;

import model.Board;
import model.Card;
import model.Position;
import model.Card.CardShape;
import model.Card.Orientation;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;

public class Util {

	public static Board getBoard(AwaitMoveMessageType message) {
		return new Board(message.getBoard());
	}

	/**
	 * create Position
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public static Position createPosition(int row, int col) {
		return new Position(row, col);
	}

	/**
	 * Returns Pin Position of the specified Player
	 * 
	 * @param b
	 * @param playerID
	 * @return
	 */
	public static Position getPinPos(Board b, int playerID) {
		return new Position(b.findPlayer(playerID));
	}

	/**
	 * returns the current shiftCard
	 * 
	 * @param b
	 * @return
	 */
	public static Card getShiftCard(Board b) {
		return new Card(b.getShiftCard());
	}

	/**
	 * Create a Card with the parameter cardShape (-> enum CardShape),
	 * orientation (-> enum Orientation) and Treasure
	 * 
	 * @param cardShape
	 * @param orientation
	 * @param treasureName
	 * @return
	 */
	public static Card createCard(String cardShape, int orientation, String treasureName) {
		return new Card(CardShape.valueOf(cardShape), Orientation.fromValue(orientation), TreasureType.fromValue(treasureName));
	}

	/**
	 * Returns the Position of the specified treasure
	 * 
	 * @param Board
	 * @param treasureName
	 * @return Position
	 */
	public static Position getTreasurePos(Board b, String treasureName) {
		return new Position(b.findTreasure(TreasureType.valueOf(treasureName)));
	}

	/**
	 * Returns the Position of the specified treasure
	 * 
	 * @param Board
	 * @param TreasureType
	 * @return Position
	 */
	public static Position getTreasurePos(Board b, TreasureType treasure) {
		return new Position(b.findTreasure(treasure));
	}

	/**
	 * Returns the number of treasures to find for the player with the specified
	 * ID
	 * 
	 * @param message
	 * @param playerID
	 * @return
	 * @throws Exception
	 */
	public static int getAmount(AwaitMoveMessageType message, int playerID) throws Exception {
		List<TreasuresToGoType> list = message.getTreasuresToGo();
		for (TreasuresToGoType tt : list) {
			if (tt.getPlayer() == playerID) {
				return tt.getTreasures();
			}
		}
		throw new Exception("Player with " + playerID + " ID is not available");
	}

	/**
	 * returns a list of TreasureTypes with found Treasures
	 * 
	 * @param message
	 * @return
	 */
	public static List<TreasureType> getFoundTreasures(AwaitMoveMessageType message) {
		return message.getFoundTreasures();
	}

	/**
	 * Returns true or false whether the specified treasure is already found.
	 * 
	 * @param message
	 * @param treasure
	 * @return
	 */
	public static boolean iaAlreadyFound(AwaitMoveMessageType message, TreasureType treasure) {
		return message.getFoundTreasures().contains(treasure);
	}

	/**
	 * Returns true or false whether it is allowed to shift the card at the
	 * given position
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public static boolean isGlued(int row, int col) {
		return (row == 1 || row == 3 || row == 5) && (col == 1 || col == 3 || col == 3);
	}

	/**
	 * Returns true or false whether it is allowed to shift the card at the
	 * given position
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public static boolean isGlued(Position p) {
		return isGlued(p.getRow(), p.getCol());
	}

	/**
	 * returns true or false whether the player has found his treasure or not
	 * 
	 * @param b
	 * @param treasure
	 * @param PlayerId
	 * @return
	 */
	public static boolean foundMyTreasure(Board b, TreasureType treasure, int PlayerId) {
		return equals(b.findTreasure(treasure), b.findPlayer(PlayerId));
	}

	public static boolean equals(Position a, Position b) {
		return a.getCol() == b.getCol() && a.getRow() == b.getRow();
	}

	public static boolean equals(PositionType a, PositionType b) {
		return a.getCol() == b.getCol() && a.getRow() == b.getRow();
	}

	public static Card getCard(Board b, Position p) {
		return new Card(b.getCard(p.getRow(), p.getCol()));
	}

	public static Card getCard(Board b, int row, int col) {
		return new Card(b.getCard(row, col));
	}

	public static Position containsInList(PositionType pt, List<PositionType> l) {
		for (PositionType p : l) {
			if (new Position(p).equals(pt)) {
				return new Position(pt);
			}
		}

		return null;
	}

	/**
	 * clockwise, just 90, 180, 270 as values
	 * 
	 * @param celsius
	 * @return
	 */
	public static Card rotateCard(Card c, int celsius) {
		return new Card(c.getShape(), Orientation.fromValue(((c.getOrientation().value() + celsius) % 360)), c.getTreasure());
	}

}
