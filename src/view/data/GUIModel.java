package view.data;


public class GUIModel {

	private int card_orientation;
	private String cardType;

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

}
