package ai.ava;


public class PathPos {
	private int row;
	private int col;
	private boolean hasRight;
	private boolean hasLeft;
	private boolean hasTop;
	private boolean hasBot;

	public PathPos(int row, int col) {
		this.row = row;
		this.col = col;
	}

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

	public boolean hasRight() {
		return hasRight;
	}

	public void setHasRight(boolean hasRight) {
		this.hasRight = hasRight;
	}

	public boolean hasLeft() {
		return hasLeft;
	}

	public void setHasLeft(boolean hasLeft) {
		this.hasLeft = hasLeft;
	}

	public boolean hasTop() {
		return hasTop;
	}

	public void setHasTop(boolean hasTop) {
		this.hasTop = hasTop;
	}

	public boolean hasBot() {
		return hasBot;
	}

	public void setHasBot(boolean hasBot) {
		this.hasBot = hasBot;
	}

	public boolean hasNeighbour() {
		return hasTop || hasBot || hasLeft || hasRight;
	}

}
