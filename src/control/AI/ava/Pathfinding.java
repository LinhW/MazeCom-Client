package control.AI.ava;

import java.util.ArrayList;
import java.util.List;

import model.Board;
import model.Card;
import model.Position;
import model.jaxb.MoveMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import control.AI.Util;

public class Pathfinding {

	private int x;
	private int y;
	private Board betterBoard;
	private final int MARKER1 = 1;
	private final int MARKER2 = 2;
	private int PlayerID;
	private int count;

	private List<PinPosHelp> list_PinPosHelp;

	public Pathfinding(Board b, int PlayerID) {
		this.betterBoard = b;
		this.PlayerID = PlayerID;
		x = b.getRow().size();
		y = b.getRow().get(0).getCol().size();
		list_PinPosHelp = new ArrayList<>();
	}

	public PinPosHelp ava(Position start, Position trePos) {
		CardHelp ch = simpleSolution(trePos);
		PinPosHelp pph = new PinPosHelp(trePos, ch);
		System.out.println("CardHelp " + ch);
		if (ch == null) {
			System.out.println("no solution");
			List<PinPosHelp> list = PinPosHelp.getSmallestDiff(list_PinPosHelp);
			if (list.size() == 1) {
				pph = list.get(0);
			} else {
				for (PinPosHelp ph : list) {
					if (Util.isGlued(ph.getPinPos())) {
						pph = ph;
						// TODO nach Ausrichtung der Schatzkarte richten
						break;
					}
				}
			}
		}
		return pph;
	}

	private PinPosHelp shortestPath(List<Position> list, Position trePos) {
		Position pos = null;
		double diff = x * y;
		for (Position p : list) {
			double tmp = diff(p, trePos);
			if (tmp < diff) {
				diff = tmp;
				pos = p;
			}
		}
		return new PinPosHelp(pos, diff);
	}

	private CardHelp simpleSolution(Position trePos) {
		Board board = (Board) betterBoard.clone();
		Position oldPinPos = Util.getPinPos(betterBoard, PlayerID);
		List<PositionType> l = board.getAllReachablePositions(oldPinPos);
		Card shift = Util.getShiftCard(betterBoard);
		Position shiftPos;
		CardHelp ch;
		MoveMessageType message = new MoveMessageType();
		for (int i = 1; i < 6; i += 2) {
			for (int j = 0; j < 4; j++) {
				Card c = Util.rotateCard(shift, j * 90);
				for (int k = 0; k < 7; k += 6) {
					board = (Board) betterBoard.clone();
					board.setShiftCard(c);
					shiftPos = new Position(k, i);
					message.setShiftPosition(shiftPos);
					message.setShiftCard(c);
					// System.out.println("i=" + i + "; j=" + j + "; k=" + k + "\n" + c);
					board.proceedShift(message);
					l = board.getAllReachablePositions(oldPinPos);
					ch = new CardHelp(c, shiftPos);
					if (Util.containsInList(trePos, l) != null) {
						return ch;
					}
					calcData(oldPinPos, trePos, ch);

					board = (Board) betterBoard.clone();
					shiftPos = new Position(i, k);
					message.setShiftPosition(shiftPos);
					// System.out.println("i=" + i + "; j=" + j + "; k=" + k + "\n" + c);
					board.proceedShift(message);
					l = board.getAllReachablePositions(oldPinPos);
					ch = new CardHelp(c, shiftPos);
					if (Util.containsInList(trePos, l) != null) {
						return ch;
					}
					calcData(oldPinPos, trePos, ch);
				}
			}
		}
		return null;
	}

	private void calcData(Position start, Position trePos, CardHelp ch) {
		int[][] reachable = new int[x][y];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				reachable[i][j] = 0;
			}
		}
		reachable[start.getRow()][start.getCol()] = MARKER1;
		count = 0;
		List<Position> list = new ArrayList<>();
		list = findPossiblePos(list, start);
		// System.out.println("durchlaeufe: " + count);
		PinPosHelp pph = shortestPath(list, trePos);
		pph.setCardHelp(ch);
		list_PinPosHelp.add(pph);
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
			for (PinPosHelp pph : list) {
				diff = pph.getDiff();
				if (diff == min) {
					res.add(pph);
				}
				if (pph.getDiff() < min) {
					res.clear();
					res.add(pph);
				}
			}
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
	}

	private double diff(Position p1, Position p2) {
		return Math.sqrt((p1.getRow() - p2.getRow()) * (p1.getRow() - p2.getRow()) + (p1.getCol() - p2.getCol()) * (p1.getCol() - p2.getCol()));
	}

	private List<Position> findPossiblePos(List<Position> list, Position start) {
		count++;
		Position p;
		int col_start = start.getCol();
		int row_start = start.getRow();
		Card c = new Card(betterBoard.getCard(row_start, col_start));
		if (col_start < x - 1) {
			p = new Position(row_start, col_start + 1);
			if (!list.contains(p) && betterBoard.getCard(row_start, col_start + 1).getOpenings().isLeft() && c.getOpenings().isRight()) {
				list.add(p);
				list = findPossiblePos(list, p);
			}
		}
		if (col_start > 0) {
			p = new Position(row_start, col_start - 1);
			if (!list.contains(p) && betterBoard.getCard(row_start, col_start - 1).getOpenings().isRight() && c.getOpenings().isLeft()) {
				list.add(p);
				list = findPossiblePos(list, p);
			}
		}
		if (row_start > 0) {
			p = new Position(row_start - 1, col_start);
			if (!list.contains(p) && betterBoard.getCard(row_start - 1, col_start).getOpenings().isBottom() && c.getOpenings().isTop()) {
				list.add(p);
				list = findPossiblePos(list, p);
			}
		}
		if (row_start < y - 1) {
			p = new Position(row_start + 1, col_start);
			if (!list.contains(p) && betterBoard.getCard(row_start + 1, col_start).getOpenings().isTop() && c.getOpenings().isBottom()) {
				list.add(p);
				list = findPossiblePos(list, p);
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

	public CardHelp calcMove(Position start, TreasureType t) {
		return simpleSolution(Util.getTreasurePos(betterBoard, t));
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
