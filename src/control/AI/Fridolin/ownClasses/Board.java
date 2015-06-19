package control.AI.Fridolin.ownClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import model.jaxb.BoardType;
import model.jaxb.CardType;
import model.jaxb.CardType.Pin;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import control.Settings;
import control.AI.Fridolin.ownClasses.Card.CardShape;
import control.AI.Fridolin.ownClasses.Card.Orientation;

public class Board extends BoardType {

	private TreasureType currentTreasure;

	public Board(BoardType boardType) {
		super();
		PositionType forbiddenPositionType = boardType.getForbidden();
		forbidden = (forbiddenPositionType != null) ? new Position(forbiddenPositionType) : null;
		shiftCard = new Card(boardType.getShiftCard());
		this.getRow();
		for (int i = 0; i < 7; i++) {
			this.getRow().add(i, new Row());
			this.getRow().get(i).getCol();
			for (int j = 0; j < 7; j++) {
				this.getRow().get(i).getCol().add(j, new Card(boardType.getRow().get(i).getCol().get(j)));
			}
		}
	}

	public Board() {
		super();
		forbidden = null;
		this.getRow();
		// Erst werden alle Karten mit einer Standardkarte belegt
		for (int i = 0; i < 7; i++) {
			this.getRow().add(i, new Row());
			this.getRow().get(i).getCol();
			for (int j = 0; j < 7; j++) {
				this.getRow().get(i).getCol().add(j, new Card(CardShape.I, Orientation.D0, null));
			}

		}
		// Dann wird das Spielfeld regelkonform aufgebaut
		generateInitialBoard();
	}

	private void generateInitialBoard() {
		// fixedCards:
		// Die festen, unveraenderbaren Karten auf dem Spielbrett
		setCard(0, 0, new Card(CardShape.L, Orientation.D90, null));
		setCard(0, 2, new Card(CardShape.T, Orientation.D0, TreasureType.SYM_13));
		setCard(0, 4, new Card(CardShape.T, Orientation.D0, TreasureType.SYM_14));
		setCard(0, 6, new Card(CardShape.L, Orientation.D180, null));
		setCard(2, 0, new Card(CardShape.T, Orientation.D270, TreasureType.SYM_15));
		setCard(2, 2, new Card(CardShape.T, Orientation.D270, TreasureType.SYM_16));
		setCard(2, 4, new Card(CardShape.T, Orientation.D0, TreasureType.SYM_17));
		setCard(2, 6, new Card(CardShape.T, Orientation.D90, TreasureType.SYM_18));
		setCard(4, 0, new Card(CardShape.T, Orientation.D270, TreasureType.SYM_19));
		setCard(4, 2, new Card(CardShape.T, Orientation.D180, TreasureType.SYM_20));
		setCard(4, 4, new Card(CardShape.T, Orientation.D90, TreasureType.SYM_21));
		setCard(4, 6, new Card(CardShape.T, Orientation.D90, TreasureType.SYM_22));
		setCard(6, 0, new Card(CardShape.L, Orientation.D0, null));
		setCard(6, 2, new Card(CardShape.T, Orientation.D180, TreasureType.SYM_23));
		setCard(6, 4, new Card(CardShape.T, Orientation.D180, TreasureType.SYM_24));
		setCard(6, 6, new Card(CardShape.L, Orientation.D270, null));

		// die freien verschiebbaren Teile auf dem Spielbrett
		ArrayList<Card> freeCards = new ArrayList<Card>();
		Random random = new Random();
		if (Settings.TESTBOARD) {
			random.setSeed(Settings.TESTBOARD_SEED);
		}
		// 15 mal L-shape (6 (sym) + 9 (ohne))
		freeCards.add(new Card(CardShape.L, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_01));
		freeCards.add(new Card(CardShape.L, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_02));
		freeCards.add(new Card(CardShape.L, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_03));
		freeCards.add(new Card(CardShape.L, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_04));
		freeCards.add(new Card(CardShape.L, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_05));
		freeCards.add(new Card(CardShape.L, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_06));

		for (int i = 0; i < 9; i++) {
			freeCards.add(new Card(CardShape.L, Orientation.fromValue(random.nextInt(4) * 90), null));
		}

		// 13 mal I-shape
		for (int i = 0; i < 13; i++) {
			freeCards.add(new Card(CardShape.I, Orientation.fromValue(random.nextInt(4) * 90), null));
		}

		// 6 mal T-shape
		freeCards.add(new Card(CardShape.T, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_07));
		freeCards.add(new Card(CardShape.T, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_08));
		freeCards.add(new Card(CardShape.T, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_09));
		freeCards.add(new Card(CardShape.T, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_10));
		freeCards.add(new Card(CardShape.T, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_11));
		freeCards.add(new Card(CardShape.T, Orientation.fromValue(random.nextInt(4) * 90), TreasureType.SYM_12));

		if (!Settings.TESTBOARD)
			Collections.shuffle(freeCards);

		int k = 0;
		for (int i = 1; i < 7; i += 2) {
			for (int j = 0; j < 7; j += 1) {
				setCard(i, j, freeCards.get(k++));
			}
		}
		for (int i = 1; i < 7; i += 2) {
			for (int j = 0; j < 7; j += 2) {
				setCard(j, i, freeCards.get(k++));
			}
		}
		this.setShiftCard(freeCards.get(k));
		getCard(0, 0).getPin().getPlayerID().add(1);
		getCard(0, 6).getPin().getPlayerID().add(2);
		getCard(6, 0).getPin().getPlayerID().add(3);
		getCard(6, 6).getPin().getPlayerID().add(4);

		// Start als Schatz hinterlegen
		getCard(0, 0).setTreasure(TreasureType.START_01);
		getCard(0, 6).setTreasure(TreasureType.START_02);
		getCard(6, 0).setTreasure(TreasureType.START_03);
		getCard(6, 6).setTreasure(TreasureType.START_04);

	}

	// Ausgabe des Spielbretts als AsciiArt
	@Override
	public String toString() {
		String player = " [";
		for (int i = 1; i < 5; i++) {
			player += "Player" + i + ":" + getPinPos(i) + ", ";
		}
		player = player.substring(0, player.length() - 2);
		player += "]";
		StringBuilder sb = new StringBuilder();
		sb.append("Board [currentTreasure=" + currentTreasure + "]" + player + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("Spielbrett: \n"); //$NON-NLS-1$
		sb.append(" ------ ------ ------ ------ ------ ------ ------ \n"); //$NON-NLS-1$
		for (int i = 0; i < getRow().size(); i++) {
			StringBuilder line1 = new StringBuilder("|"); //$NON-NLS-1$
			StringBuilder line2 = new StringBuilder("|"); //$NON-NLS-1$
			StringBuilder line3 = new StringBuilder("|"); //$NON-NLS-1$
			StringBuilder line4 = new StringBuilder("|"); //$NON-NLS-1$
			StringBuilder line5 = new StringBuilder("|"); //$NON-NLS-1$
			StringBuilder line6 = new StringBuilder("|"); //$NON-NLS-1$
			for (int j = 0; j < getRow().get(i).getCol().size(); j++) {
				Card c = new Card(getCard(i, j));
				if (c.getOpenings().isTop()) {
					line1.append("##  ##|"); //$NON-NLS-1$
					line2.append("##  ##|"); //$NON-NLS-1$
				} else {
					line1.append("######|"); //$NON-NLS-1$
					line2.append("######|"); //$NON-NLS-1$
				}
				if (c.getOpenings().isLeft()) {
					line3.append("  "); //$NON-NLS-1$
					line4.append("  "); //$NON-NLS-1$
				} else {
					line3.append("##"); //$NON-NLS-1$
					line4.append("##"); //$NON-NLS-1$
				}
				if (c.getPin().getPlayerID().size() != 0) {
					line3.append("S"); //$NON-NLS-1$
				} else {
					line3.append(" "); //$NON-NLS-1$
				}
				if (c.getTreasure() != null) {
					String name = c.getTreasure().name();
					switch (name.charAt(1)) {
					case 'Y':
						// Symbol
						line3.append("T"); //$NON-NLS-1$
						break;
					case 'T':
						// Startpunkt
						line3.append("S"); //$NON-NLS-1$
						break;
					}
					line4.append(name.substring(name.length() - 2));
				} else {
					line3.append(" "); //$NON-NLS-1$
					line4.append("  "); //$NON-NLS-1$
				}
				if (c.getOpenings().isRight()) {
					line3.append("  |"); //$NON-NLS-1$
					line4.append("  |"); //$NON-NLS-1$
				} else {
					line3.append("##|"); //$NON-NLS-1$
					line4.append("##|"); //$NON-NLS-1$
				}
				if (c.getOpenings().isBottom()) {
					line5.append("##  ##|"); //$NON-NLS-1$
					line6.append("##  ##|"); //$NON-NLS-1$
				} else {
					line5.append("######|"); //$NON-NLS-1$
					line6.append("######|"); //$NON-NLS-1$
				}
			}
			sb.append(line1.toString() + "\n"); //$NON-NLS-1$
			sb.append(line2.toString() + "\n"); //$NON-NLS-1$
			sb.append(line3.toString() + "\n"); //$NON-NLS-1$
			sb.append(line4.toString() + "\n"); //$NON-NLS-1$
			sb.append(line5.toString() + "\n"); //$NON-NLS-1$
			sb.append(line6.toString() + "\n"); //$NON-NLS-1$
			sb.append(" ------ ------ ------ ------ ------ ------ ------ \n"); //$NON-NLS-1$
		}

		return sb.toString();
	}

	public void setCard(int row, int col, Card c) {
		// Muss ueberschrieben werden, daher zuerst entfernen und dann...
		this.getRow().get(row).getCol().remove(col);
		// ...hinzufuegen
		this.getRow().get(row).getCol().add(col, c);
	}

	public CardType getCard(int row, int col) {
		return this.getRow().get(row).getCol().get(col);
	}
	
	public Card getCard(Position p){
		return new Card(getCard(p.getRow(), p.getCol()));
	}

	public Card getShiftCard() {
		return new Card(super.getShiftCard());
	}

	public Position getPinPos(int PlayerID) {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				Pin pinsOnCard = getCard(i, j).getPin();
				for (Integer pin : pinsOnCard.getPlayerID()) {
					if (pin == PlayerID) {
						return new Position(i, j);
					}
				}
			}
		}
		return null;
	}

	// Fuehrt nur das Hereinschieben der Karte aus!!!
	public void proceedShift(Position shiftPos, Card shiftCard) {
		if (shiftPos.getCol() % 6 == 0) { // Col=6 oder 0
			if (shiftPos.getRow() % 2 == 1) {
				// horizontal schieben
				int row = shiftPos.getRow();
				int start = (shiftPos.getCol() + 6) % 12; // Karte die rausgenommen
				// wird
				setShiftCard(getCard(row, start));

				if (start == 6) {
					for (int i = 6; i > 0; --i) {
						setCard(row, i, new Card(getCard(row, i - 1)));
					}
				} else {// Start==0
					for (int i = 0; i < 6; ++i) {
						setCard(row, i, new Card(getCard(row, i + 1)));
					}
				}
			}
		} else if (shiftPos.getRow() % 6 == 0) {
			if (shiftPos.getCol() % 2 == 1) {
				// vertikal schieben
				int col = shiftPos.getCol();
				int start = (shiftPos.getRow() + 6) % 12; // Karte die rausgenommen
				// wird
				setShiftCard(getCard(start, col));
				if (start == 6) {
					for (int i = 6; i > 0; --i) {
						setCard(i, col, new Card(getCard(i - 1, col)));
					}
				} else {// Start==0
					for (int i = 0; i < 6; ++i) {
						setCard(i, col, new Card(getCard(i + 1, col)));
					}
				}

			}
		}
		forbidden = shiftPos.getOpposite();
		// muss dieser wieder aufs Brett gesetzt werden
		// Dazu wird Sie auf die gerade hereingeschoben
		// Karte gesetzt
		if (!this.shiftCard.getPin().getPlayerID().isEmpty()) {
			// Figur zwischenspeichern
			Pin temp = this.shiftCard.getPin();
			// Figur auf SchiebeKarte l??schen
			this.shiftCard.setPin(new Pin());
			// Zwischengespeicherte Figur auf
			// neuer Karte plazieren
			shiftCard.setPin(temp);
		}
		setCard(shiftPos.getRow(), shiftPos.getCol(), shiftCard);
	}

	@Override
	public Object clone() {
		Board clone = new Board();
		if (forbidden == null) {
			clone.forbidden = null;
		} else {
			clone.forbidden = new Position(this.forbidden);
		}
		clone.shiftCard = new Card(this.shiftCard);
		clone.currentTreasure = this.currentTreasure;
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				clone.setCard(i, j, new Card(this.getCard(i, j)));
			}
		}
		return clone;
	}

	public Position findTreasure(TreasureType treasureType) {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				TreasureType treasure = getCard(i, j).getTreasure();
				if (treasure == treasureType) {
					return new Position(i, j);
				}
			}
		}
		return null;
	}

	public void setTreasure(TreasureType t) {
		currentTreasure = t;
	}

	public TreasureType getTreasure() {
		return currentTreasure;
	}

	public void setPinPos(int PlayerID, int row, int col) {
		Position p = getPinPos(PlayerID);
		Pin pin = this.getCard(p.getRow(), p.getCol()).getPin();
		this.getCard(p.getRow(), p.getCol()).setPin(new Pin());
		this.getCard(row, col).setPin(pin);
	}
}
