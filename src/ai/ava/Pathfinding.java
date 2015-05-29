package ai.ava;

import gui.data.Board;
import gui.data.Card;
import gui.data.Position;

import java.util.ArrayList;
import java.util.List;

import jaxb.MoveMessageType;
import jaxb.PositionType;
import jaxb.TreasureType;
import ai.Util;

public class Pathfinding {

	private static int x;
	private static int y;
	private static Board b;
	private static final int MARKER1 = 1;
	private static final int MARKER2 = 2;
	private static int PlayerID;

	private static int[][] findPath(Position start, Position end) {
		int[][] reachable = new int[x][y];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				reachable[i][j] = 0;
			}
		}
		reachable[start.getRow()][start.getCol()] = MARKER1;
		if (!tilTheEnd(reachable, start, end, MARKER1)) {
			reachable[end.getRow()][end.getCol()] = MARKER2;
			tilTheEnd(reachable, end, start, MARKER2);
			Path p = new Path(reachable);

			// card is glued? -> false : shift to the other side?
		} else {
			// TODO
			// find possible position for shifting Card without destroying the
			// calculated path
			// 0: find alternative
			// 1: block other player if possible
		}
		return reachable;
	}

	private static List<Position> getPossiblePositions(Position start, Position end) {
		// TODO
		List<Position> l = new ArrayList<Position>();
		int[][] reachable = new int[x][y];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				reachable[i][j] = 0;
			}
		}
		reachable[start.getRow()][start.getCol()] = MARKER1;
		if (!tilTheEnd(reachable, start, end, MARKER1)) {
			reachable[end.getRow()][end.getCol()] = MARKER2;
			tilTheEnd(reachable, end, start, MARKER2);
			Path p = new Path(reachable);

			// card is glued? -> false : shift to the other side?
		} else {
			// TODO
			// find possible position for shifting Card without destroying the
			// calculated path
			// 0: find alternative
			// 1: block other player if possible
		}
		return l;
	}

	public static CardHelp calcMove(Board board, Position start, TreasureType t, int PlayerID) {
		Pathfinding pf = new Pathfinding();
		Pathfinding.PlayerID = PlayerID;
		b = board;
		x = b.getRow().size();
		y = b.getRow().get(0).getCol().size();
		return pf.simpleSolution(Util.getTreasurePos(b, t), b);
	}

	private CardHelp simpleSolution(Position pt, Board bo) {
		Board board = (Board) bo.clone();
		Position oldPinPos = Util.getPinPos(bo, PlayerID);
		List<PositionType> l = board.getAllReachablePositions(oldPinPos);
		Card shift = Util.getShiftCard(bo);
		Position shiftPos;
		MoveMessageType message = new MoveMessageType();
		for (int i = 1; i < 6; i += 2) {
			for (int j = 0; j < 4; j++) {
				Card c = Util.rotateCard(shift, j * 90);
				for (int k = 0; k < 7; k += 6) {
					board = (Board) bo.clone();
					board.setShiftCard(c);
					shiftPos = new Position(k, i);
					message.setShiftPosition(shiftPos);
					message.setShiftCard(shift);
					board.proceedShift(message);
					l = board.getAllReachablePositions(oldPinPos);
					if (Util.containsInList(pt, l) != null) {
						return new CardHelp(c, shiftPos);
					}
				}
			}
		}
		return advancedSolution();
	}

	private static CardHelp advancedSolution() {
		return null;
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

	}

	private static boolean tilTheEnd(int[][] reachable, Position start, Position end, int marker) {
		boolean res = false;
		int col_start = start.getCol();
		int row_start = start.getRow();
		int col_end = end.getCol();
		int row_end = end.getRow();
		Card c = new Card(b.getCard(row_start, col_start));
		if (col_start < x - 1) {
			if (b.getCard(row_start, col_start + 1).getOpenings().isLeft() && c.getOpenings().isRight() && 0 == reachable[row_start][col_start + 1]) {
				if (row_start == row_end && col_start + 1 == col_end) {
					System.out.println(row_start + "/" + (col_start + 1));
					return true;
				}
				reachable[row_start][col_start + 1] = marker;
				res = tilTheEnd(reachable, new Position(row_start, col_start + 1), end, marker) || res;
			}
		}
		if (col_start > 0) {
			if (b.getCard(row_start, col_start - 1).getOpenings().isRight() && c.getOpenings().isLeft() && 0 == reachable[row_start][col_start - 1]) {
				if (row_start == row_end && col_start - 1 == col_end) {
					System.out.println(row_start + "/" + col_start + 1);
					return true;
				}
				reachable[row_start][col_start - 1] = marker;
				res = tilTheEnd(reachable, new Position(row_start, col_start - 1), end, marker) || res;
			}
		}
		if (row_start > 0) {
			if (b.getCard(row_start - 1, col_start).getOpenings().isBottom() && c.getOpenings().isTop() && reachable[row_start - 1][col_start] == 0) {
				if (row_start - 1 == row_end && col_start == col_end) {
					System.out.println(row_start - 1 + "/" + col_start);
					return true;
				}
				reachable[row_start - 1][col_start] = marker;
				res = tilTheEnd(reachable, new Position(row_start - 1, col_start), end, marker) || res;
			}
		}
		if (row_start < y - 1) {
			if (b.getCard(row_start + 1, col_start).getOpenings().isTop() && c.getOpenings().isBottom() && reachable[row_start + 1][col_start] == 0) {
				if (row_start + 1 == row_end && col_start == col_end) {
					System.out.println(row_start + 1 + "/" + col_start);
					return true;
				}
				reachable[row_start + 1][col_start] = marker;
				res = tilTheEnd(reachable, new Position(row_start + 1, col_start), end, marker) || res;
			}
		}

		return res;
	}

}
