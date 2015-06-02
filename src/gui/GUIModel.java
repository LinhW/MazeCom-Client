package gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import model.Card;
import model.Card.CardShape;
import model.Card.Orientation;
import model.Position;
import model.jaxb.TreasureType;

public class GUIModel {

	private Card shiftCard;
	private TreasureType cardTreasure;
	private Map<String, Integer> map;
	private int row;
	private int col;
	private Position pinPos;
	private int id;

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public GUIModel() {
		map = new HashMap<>();
	}

	public Orientation getCardOrientation() {
		return shiftCard.getOrientation();
	}

	public int getOrientation() {
		return getCardOrientation().value();
	}

	public void setCardOrientation(int orientation) {
		this.shiftCard = new Card(getCardShape(), Orientation.fromValue(orientation), shiftCard.getTreasure());
	}

	public void setCardOrientation(Orientation orientation) {
		this.shiftCard = new Card(getCardShape(), orientation, shiftCard.getTreasure());
	}

	public CardShape getCardShape() {
		return this.shiftCard.getShape();
	}

	public String getShape() {
		return this.shiftCard.getShape().name();
	}

	public void setCardShape(CardShape c) {
		this.shiftCard = new Card(c, getCardOrientation(), shiftCard.getTreasure());
	}

	public void setCardShape(String c) {
		this.shiftCard = new Card(CardShape.valueOf(c), getCardOrientation(), shiftCard.getTreasure());
	}

	public TreasureType getCardTreasure() {
		return cardTreasure;
	}

	public String getTreasure() {
		return cardTreasure.name();
	}

	public void setCardTreasure(TreasureType treasureType) {
		this.cardTreasure = treasureType;
	}

	public void setCardTreasure(String treasure) {
		this.cardTreasure = TreasureType.valueOf(treasure);
	}

	public void setKeyEventMap(Map<String, Integer> map) {
		this.map = map;
	}

	public void setKeyEvent(String key, int event) {
		map.put(key, event);
	}

	public int getKeyEvent(String key) {
		return map.get(key);
	}

	public Collection<Integer> getKeyEvents() {
		return map.values();
	}

	public Set<String> getKeys() {
		return map.keySet();
	}

	public Card getShiftCard() {
		return shiftCard;
	}

	public void setShiftCard(Card shiftCard) {
		this.shiftCard = shiftCard;
	}

	public Position getPinPos() {
		return pinPos;
	}

	public void setPinPos(Position pinPos) {
		this.pinPos = pinPos;
	}

	public void setPinPos(int row, int col) {
		this.pinPos = new Position(row, col);
	}

	public void setPlayerID(int id) {
		this.id = id;
	}

	public int getPlayerID() {
		return id;
	}

}
