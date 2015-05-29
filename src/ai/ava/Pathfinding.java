package ai.ava;

import gui.data.Board;
import gui.data.Card;
import gui.data.Position;

import java.util.ArrayList;
import java.util.List;

import ai.Util;
import ai.ava.Path.Neighbour;

public class Pathfinding {

	private static int x;
	private static int y;
	private static Board b;
	private static final int MARKER1 = 1;
	private static final int MARKER2 = 2;

	public static int[][] findPath(Board board, Position start, Position end) {
		b = board;
		x = b.getRow().size();
		y = b.getRow().get(0).getCol().size();
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
			simpleSolution(p.getNeighbours(MARKER1, MARKER2));

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

	private static List<Position> simpleSolution(List<Neighbour> neighbours) {
		List<Position> l = new ArrayList<>();
		for (Neighbour n : neighbours) {
			if (n.sameRow()) {
				Util.getCard(b, n.getP1().getRow(), n.getP1().getCol() + 1);
				Util.getCard(b, n.getP1().getRow(), n.getP1().getCol() - 1);
				Util.getCard(b, n.getP1().getRow(), n.getP2().getCol() + 1);
				Util.getCard(b, n.getP1().getRow(), n.getP2().getCol() - 1);
			} else {
				if (n.sameCol()) {

				} else {
					Card shift;
					Card glued;
					if(Util.isGlued(n.getP1())){
						shift = Util.getCard(b, n.getP1());
						glued = Util.getCard(b, n.getP2());
					}else{
						glued = Util.getCard(b, n.getP1());
						shift = Util.getCard(b, n.getP2());
					}
//					if (glued.getOpenings())
				}
			}
		}
		return l;
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
					System.out.println(row_start + "/" + col_start + 1);
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
