package ai;

import gui.data.Board;
import gui.data.Card;
import gui.data.Card.CardShape;
import gui.data.Card.Orientation;
import gui.data.Position;

import java.util.List;

import jaxb.AwaitMoveMessageType;
import jaxb.TreasureType;
import jaxb.TreasuresToGoType;

public class Util {

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

}
