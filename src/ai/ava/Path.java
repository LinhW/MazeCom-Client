package ai.ava;

import gui.data.Position;

import java.util.ArrayList;
import java.util.List;

public class Path {
	private int x;
	private int y;
	private List<List<Integer>> path;

	public Path(int[][] path) {
		this.path = new ArrayList<List<Integer>>();
		for (int i = 0; i < path.length; i++) {
			List tmp = new ArrayList<>();
			for (int j : path[i]) {
				tmp.add(j);
			}
			this.path.add(tmp);
		}
		y = this.path.get(0).size();
		x = this.path.size();
	}

	public String toString() {
		String s = "";
		for (int j = 0; j < x; j++) {
			for (int i = 0; i < y; i++) {
				s += (path.get(j).get(i) + " ");
			}
			s += "\n";
		}
		return s;
	}

	public List<Integer> getCol(int index) {
		List<Integer> l = new ArrayList<>();
		for (int i = 0; i < y; i++) {
			l.add(path.get(i).get(index));
		}
		return l;
	}

	public List<Neighbour> getNeighbours(int marker1, int marker2) {
		List<Neighbour> n = new ArrayList<>();
		for (int j = 0; j < x - 1; j++) {
			List<Integer> l = path.get(j);
			if (l.contains(marker1) && l.contains(marker2)) {
				for (int i = 0; i < y - 1; i++) {
					int tmp = path.get(j).get(i);
					int temp = path.get(j).get(i + 1);
					if ((tmp == marker1 && temp == marker2) || (tmp == marker2 && temp == marker1)) {
						n.add(new Neighbour(new Position(j, i), new Position(j, i + 1)));
					}
				}
			}
			l = getCol(j);
			if (l.contains(marker1) && l.contains(marker2)) {
				for (int i = 0; i < y - 1; i++) {
					int tmp = path.get(j).get(i);
					int temp = path.get(j + 1).get(i);
					if ((tmp == marker1 && temp == marker2) || (tmp == marker2 && temp == marker1)) {
						n.add(new Neighbour(new Position(j, i), new Position(j + 1, i)));
					}
				}
			}
		}
		int j = x - 1;
		List<Integer> l = path.get(j);
		if (l.contains(marker1) && l.contains(marker2)) {
			for (int i = 0; i < y - 1; i++) {
				int tmp = path.get(j).get(i);
				int temp = path.get(j).get(i + 1);
				if ((tmp == marker1 && temp == marker2) || (tmp == marker2 && temp == marker1)) {
					n.add(new Neighbour(new Position(j, i), new Position(j, i + 1)));
				}
			}
		}
		return n;
	}

	public List<Neighbour> getPossiblePaths(int marker1, int marker2) {
		List<Neighbour> n = new ArrayList<>();
		for (int j = 0; j < x - 1; j++) {
			List<Integer> l = path.get(j);
			if (l.contains(marker1) && l.contains(marker2)) {
				for (int i = 0; i < y - 2; i++) {
					int tmp = path.get(j).get(i);
					for (int k = 1; k <= 2; k++) {
						int temp = path.get(j).get(i + k);
						if ((tmp == marker1 && temp == marker2) || (tmp == marker2 && temp == marker1)) {
							n.add(new Neighbour(new Position(j, i), new Position(j, i + k)));
						}
					}
				}
				int k = 1, i = y - 1;
				int tmp = path.get(j).get(i);
				int temp = path.get(j).get(i + k);
				if ((tmp == marker1 && temp == marker2) || (tmp == marker2 && temp == marker1)) {
					n.add(new Neighbour(new Position(j, i), new Position(j, i + k)));
				}
			}
			l = getCol(j);
			if (l.contains(marker1) && l.contains(marker2)) {
				for (int i = 0; i < y - 2; i++) {
					int tmp = path.get(j).get(i);
					for (int k = 1; k <= 2; k++) {
						int temp = path.get(j + k).get(i);
						if ((tmp == marker1 && temp == marker2) || (tmp == marker2 && temp == marker1)) {
							n.add(new Neighbour(new Position(j, i), new Position(j + k, i)));
						}
					}
				}
				int k = 1, i = y - 1;
				int tmp = path.get(j).get(i);
				int temp = path.get(j + k).get(i);
				if ((tmp == marker1 && temp == marker2) || (tmp == marker2 && temp == marker1)) {
					n.add(new Neighbour(new Position(j, i), new Position(j + k, i)));
				}
			}
		}
		return n;
	}

	public int get(int row, int col) {
		return path.get(row).get(col);
	}
	
	public int get(Position p) {
		return get(p.getRow(), p.getCol());
	}

	public class Neighbour {
		private Position p1;
		private Position p2;

		public Neighbour(Position p1, Position p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		public Position getP1() {
			return p1;
		}

		public void setP1(Position p1) {
			this.p1 = p1;
		}

		public Position getP2() {
			return p2;
		}

		public void setP2(Position p2) {
			this.p2 = p2;
		}

		public String toString() {
			return p1 + "/" + p2;
		}

		public boolean sameRow() {
			return p1.getRow() == p2.getRow();
		}

		public boolean sameCol() {
			return p1.getCol() == p2.getCol();
		}

	}

}
