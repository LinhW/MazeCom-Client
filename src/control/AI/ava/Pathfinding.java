package control.AI.ava;

import java.util.ArrayList;
import java.util.List;

import model.jaxb.CardType.Pin;
import control.AI.ava.ownClasses.Board;
import control.AI.ava.ownClasses.Card;
import control.AI.ava.ownClasses.Position;

public class Pathfinding {

	private int x;
	private int y;
	private Board betterBoard;
	private final int MARKER1 = 1;
	private final int MARKER2 = 2;
	private int PlayerID;
	private int count;
	private static WriteIntoFile wif;
	private static WriteIntoFile possPos;
	private static WriteIntoFile wif_v2;
	private static final String FILEPATH = "src/control/AI/ava/possPos.txt";

	/**
	 * PinPosHelp with diff from new PinPos to TreasurePos
	 */
	private List<PinPosHelp> list_PinPosHelp_v1;
	/**
	 * PinPosHelp with diff from new PinPos to the next Position with connection to the Treasure
	 */
	private List<PinPosHelp> list_PinPosHelp_v2;

	public Pathfinding(Board b, int PlayerID) {
		this.betterBoard = b;
		this.PlayerID = PlayerID;
		x = b.getRow().size();
		y = b.getRow().get(0).getCol().size();
		list_PinPosHelp_v1 = new ArrayList<>();
		list_PinPosHelp_v2 = new ArrayList<>();
		wif = new WriteIntoFile(WriteIntoFile.FILEPATH + ".txt");
		wif_v2 = new WriteIntoFile(WriteIntoFile.FILEPATH + "_v2.txt");
		possPos = new WriteIntoFile(FILEPATH);
	}

	public PinPosHelp ava(Position start, Position trePos) {
		list_PinPosHelp_v1.clear();
		list_PinPosHelp_v2.clear();
		wif.write("PinPos: " + start + " TreasureToFindPos: " + trePos);
		CardHelp ch = simpleSolution(trePos);
		PinPosHelp pph = new PinPosHelp(trePos, ch);
		System.out.println("CardHelp " + ch);
		wif.write("CardHelp " + ch);
		if (ch == null) {
			System.out.println("no solution");
			List<PinPosHelp> list = PinPosHelp.getSmallestDiff(list_PinPosHelp_v1);
			List<PinPosHelp> list_rev = PinPosHelp.getSmallestDiff(list_PinPosHelp_v2);
			if (list.size() == 1 && list_rev.size() == 1) {
				if (list.get(0).equals(list_rev.get(0))) {
					pph = list.get(0);
				} else {
					if (list_rev.get(0).getDiff() < 4) {
						pph = list_rev.get(0);
					}
				}
			} else {
				list = chooseOrientation(list, trePos);
				// if (ph.getPinPos().isGlued()) {
				// pph = ph;
				// // TODO nach Ausrichtung der Schatzkarte richten
				// // TODO nur Rotationsunterschied? -> moeglichst unnuetz anbringen
				//
				// break;
				// }
				pph = list.get(0);
			}
		}
		return pph;
	}

	private List<PinPosHelp> chooseOrientation(List<PinPosHelp> list, Position trePos) {
		List<PinPosHelp> tmp = new ArrayList<>();
		for (PinPosHelp pph : list) {
			Position p = pph.getPinPos();
			Card c = pph.getCardHelp().getC();
			if (trePos.getCol() == p.getCol()) {
				// if()
			} else if (trePos.getRow() == p.getRow()) {

			}
		}
		return list;
	}

	private PinPosHelp shortestPath(List<Position> list, Position trePos) {
		Position pos = null;
		double diff = x * y;
		for (Position p : list) {
			double tmp = p.diff(trePos);
			wif.write(p + " " + trePos + " " + tmp);
			if (tmp < diff) {
				diff = tmp;
				pos = p;
			}
		}
		return new PinPosHelp(pos, diff);
	}

	private PinPosHelp shortestPath(List<Position> list, List<Position> list_rev) {
		Position pos = null;
		double diff = x * y;
		for (Position p : list) {
			for (Position pr : list_rev) {
				double tmp = p.diff(pr);
				wif_v2.write(p + " " + pr + " " + tmp);
				if (tmp < diff) {
					diff = tmp;
					pos = p;
				}
			}
		}
		return new PinPosHelp(pos, diff);
	}

	private CardHelp simpleSolution(Position trePos) {
		List<Position> l = new ArrayList<>();
		List<Position> l_rev = new ArrayList<>();
		Board board;
		Position oldPinPos;
		Position shiftPos;
		CardHelp ch;

		List<Card> list_c = betterBoard.getShiftCard().getPossibleRotations();
		for (Card c : list_c) {
			for (int i = 1; i < 6; i += 2) {
				for (int k = 0; k < 7; k += 6) {
					for (int j = 0; j < 2; j++) {
						board = (Board) betterBoard.clone();
						c.setPin(new Pin());
						shiftPos = new Position(k + (i - k) * j, i + (k - i) * j);
						board.proceedShift(shiftPos, c);
						oldPinPos = board.getPinPos(PlayerID);
						wif.writeNewLine(1);
						wif.write("PinPos: " + oldPinPos + " " + "ShiftPos " + shiftPos);
						l.clear();
						l = findPossiblePos(board, l, oldPinPos);
						l_rev.clear();
						l_rev = findPossiblePos(board, l_rev, trePos);
						possPos.write(board.toString());
						possPos.write(oldPinPos.toString());
						possPos.writeList(l);
						possPos.writeNewLine(2);
						ch = new CardHelp(c, shiftPos);
						if (l.contains(trePos)) {
							wif.write(board.toString());
							wif.write(oldPinPos.toString());
							wif.write("************************************************\n" + ch.toString() + "\n************************************************");
							return ch;
						}
						calcData(oldPinPos, trePos, ch, l, l_rev);
					}
				}
			}
		}
		return null;
	}

	private void calcData(Position start, Position trePos, CardHelp ch, List<Position> list, List<Position> list_rev) {
		wif.write("PinPos: " + start + " " + ch.debug() + " " + list.size() + " " + list_rev.size());
		PinPosHelp pph = shortestPath(list, trePos);
		pph.setCardHelp(ch);
		list_PinPosHelp_v1.add(pph);
		pph = shortestPath(list, list_rev);
		list_PinPosHelp_v2.add(pph);
	}

	private List<Position> findPossiblePos(Board b, List<Position> list, Position start) {
		count++;
		Position p;
		int col_start = start.getCol();
		int row_start = start.getRow();
		Card c = new Card(b.getCard(row_start, col_start));
		if (col_start < x - 1) {
			p = new Position(row_start, col_start + 1);
			if (!list.contains(p) && b.getCard(row_start, col_start + 1).getOpenings().isLeft() && c.getOpenings().isRight()) {
				list.add(p);
				list = findPossiblePos(b, list, p);
			}
		}
		if (col_start > 0) {
			p = new Position(row_start, col_start - 1);
			if (!list.contains(p) && b.getCard(row_start, col_start - 1).getOpenings().isRight() && c.getOpenings().isLeft()) {
				list.add(p);
				list = findPossiblePos(b, list, p);
			}
		}
		if (row_start > 0) {
			p = new Position(row_start - 1, col_start);
			if (!list.contains(p) && b.getCard(row_start - 1, col_start).getOpenings().isBottom() && c.getOpenings().isTop()) {
				list.add(p);
				list = findPossiblePos(b, list, p);
			}
		}
		if (row_start < y - 1) {
			p = new Position(row_start + 1, col_start);
			if (!list.contains(p) && b.getCard(row_start + 1, col_start).getOpenings().isTop() && c.getOpenings().isBottom()) {
				list.add(p);
				list = findPossiblePos(b, list, p);
			}
		}
		return list;
	}

	public int[][] findPath(Position start, Position end) {
		int[][] reachable = new int[x][y];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				reachable[i][j] = 0;
			}
		}
		reachable[start.getRow()][start.getCol()] = MARKER1;
		count = 0;
		if (!tilTheEnd(reachable, start, end, MARKER1)) {
			System.out.println("count " + count);
			reachable[end.getRow()][end.getCol()] = MARKER2;
			count = 0;
			tilTheEnd(reachable, end, start, MARKER2);
			System.out.println("count " + count);
			// Path p = new Path(reachable);

			// card is glued? -> false : shift to the other side?
		} else {
			System.out.println("count " + count);
			// TODO
			// find possible position for shifting Card without destroying the
			// calculated path
			// 0: find alternative
			// 1: block other player if possible
		}
		return reachable;
	}

	public static class PinPosHelp {
		private Position pinPos;
		private double diff = Double.MAX_VALUE;
		private CardHelp ch;

		public PinPosHelp(Position pinPos, double diff) {
			this.pinPos = pinPos;
			this.diff = diff;
		}

		public PinPosHelp(Position pinPos, CardHelp ch) {
			this.pinPos = pinPos;
			this.ch = ch;
		}

		public Position getPinPos() {
			return pinPos;
		}

		public double getDiff() {
			return diff;
		}

		public static List<PinPosHelp> getSmallestDiff(List<PinPosHelp> list) {
			List<PinPosHelp> res = new ArrayList<>();
			double min = Double.MAX_VALUE;
			double diff;
			wif.write("-----------");
			for (PinPosHelp pph : list) {
				diff = pph.getDiff();
				wif.write(pph.debug() + "\t" + min);
				if (diff == min) {
					res.add(pph);
				}
				if (diff < min) {
					min = diff;
					res.clear();
					res.add(pph);
				}
			}
			wif.write("-----");
			for (PinPosHelp pp : res) {
				wif.write(pp.debug());
			}
			wif.write("-----------");
			return res;
		}

		public CardHelp getCardHelp() {
			return ch;
		}

		public void setCardHelp(CardHelp ch) {
			this.ch = ch;
		}

		public String toString() {
			return "PinPos: " + pinPos + " " + ch;
		}

		public String debug() {
			return "PinPos: " + pinPos + " " + ch.debug() + "\t" + diff;
		}

		public boolean equals(PinPosHelp pph) {
			return pph.ch.equals(this.getCardHelp()) && this.pinPos.equals(pph.getPinPos());
		}
	}

	public class CardHelp {
		private Card c;
		private Position p;

		public CardHelp(Card c, Position p) {
			this.c = c;
			this.p = p;
		}

		public Card getC() {
			return c;
		}

		public Position getP() {
			return p;
		}

		public String toString() {
			return "CardPos: " + p + "\n" + c;
		}

		public String debug() {
			return "CardPos: " + p + " Card: " + c.getShape() + c.getOrientation().value();
		}

		public boolean equals(CardHelp ch) {
			return this.c.equals(ch.getC()) && this.p.equals(ch.getP());
		}
	}

	private boolean tilTheEnd(int[][] reachable, Position start, Position end, int marker) {
		count++;
		boolean res = false;
		int col_start = start.getCol();
		int row_start = start.getRow();
		int col_end = end.getCol();
		int row_end = end.getRow();
		Card c = new Card(betterBoard.getCard(row_start, col_start));
		if (col_start < x - 1) {
			if (betterBoard.getCard(row_start, col_start + 1).getOpenings().isLeft() && c.getOpenings().isRight() && 0 == reachable[row_start][col_start + 1]) {
				if (row_start == row_end && col_start + 1 == col_end) {
					System.out.println("1 " + row_start + "/" + (col_start + 1));
					return true;
				}
				reachable[row_start][col_start + 1] = marker;
				res = tilTheEnd(reachable, new Position(row_start, col_start + 1), end, marker) || res;
			}
		}
		if (col_start > 0) {
			if (betterBoard.getCard(row_start, col_start - 1).getOpenings().isRight() && c.getOpenings().isLeft() && 0 == reachable[row_start][col_start - 1]) {
				if (row_start == row_end && col_start - 1 == col_end) {
					System.out.println("2 " + row_start + "/" + (col_start + 1));
					return true;
				}
				reachable[row_start][col_start - 1] = marker;
				res = tilTheEnd(reachable, new Position(row_start, col_start - 1), end, marker) || res;
			}
		}
		if (row_start > 0) {
			if (betterBoard.getCard(row_start - 1, col_start).getOpenings().isBottom() && c.getOpenings().isTop() && reachable[row_start - 1][col_start] == 0) {
				if (row_start - 1 == row_end && col_start == col_end) {
					System.out.println("3 " + (row_start - 1) + "/" + col_start);
					return true;
				}
				reachable[row_start - 1][col_start] = marker;
				res = tilTheEnd(reachable, new Position(row_start - 1, col_start), end, marker) || res;
			}
		}
		if (row_start < y - 1) {
			if (betterBoard.getCard(row_start + 1, col_start).getOpenings().isTop() && c.getOpenings().isBottom() && reachable[row_start + 1][col_start] == 0) {
				if (row_start + 1 == row_end && col_start == col_end) {
					System.out.println("4 " + (row_start + 1) + "/" + col_start);
					return true;
				}
				reachable[row_start + 1][col_start] = marker;
				res = tilTheEnd(reachable, new Position(row_start + 1, col_start), end, marker) || res;
			}
		}

		return res;
	}

}
