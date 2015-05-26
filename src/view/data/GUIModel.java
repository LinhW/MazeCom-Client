package view.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GUIModel {

	private int card_orientation;
	private String cardType;
	private Map<String, Integer> map;

	public GUIModel() {
		map = new HashMap<>();
	}

	public int getCardOrientation() {
		return card_orientation;
	}

	public void setCardOrientation(int orientation) {
		this.card_orientation = orientation;
	}

	public void setCardType(String c) {
		this.cardType = c;
	}

	public String getCardType() {
		return cardType;
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

}
