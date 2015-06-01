package control.AI.ava.ownClasses;

import model.jaxb.PositionType;

public class Position extends PositionType {

	public Position() {
		super();
		row = -1;
		col = -1;
	}

	public Position(PositionType p) {
		super();
		row = p.getRow();
		col = p.getCol();
	}

	public Position(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public boolean isGlued() {
		return (row % 2 == 0 && col % 2 == 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Position other = new Position((PositionType) obj);
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + row + "," + col + ")"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}

}
