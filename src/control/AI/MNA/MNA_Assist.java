package control.AI.MNA;

import java.util.ArrayList;
import java.util.List;

import model.Board;
import model.Card;
import model.Position;
import model.jaxb.CardType;
import model.jaxb.MoveMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;

public class MNA_Assist {
	private enum Side {
		UP,
		RIGHT,
		DOWN,
		LEFT
	}
	
	private static ArrayList<TreasureType> allTreasures = null;
	
	public static void initializeAllTreasures() {
		if (allTreasures != null) {
			return;
		}
		allTreasures = new ArrayList<TreasureType>();
		for (TreasureType treasure : TreasureType.values()) {
			allTreasures.add(treasure);
		}
		allTreasures.trimToSize();
	}
	
	public static ArrayList<TreasureType> getAllTreasures() {
		// Not modifiable
		return new ArrayList<TreasureType>(allTreasures);
	}

	public static ArrayList<Position> getShiftPositions(Board oldBoard) {
		ArrayList<Position> positionList = new ArrayList<Position>();

		// Create senseless values, if there is no forbidden position
		Position forbiddenPosition;
		if (oldBoard.getForbidden() == null) {
			forbiddenPosition = new Position();
			forbiddenPosition.setCol(-1);
			forbiddenPosition.setRow(-1);
		}
		else {
			forbiddenPosition = new Position(oldBoard.getForbidden());
		}

		// Iterate over all four edges of the board
		for (Side side : Side.values()) {
			Position shiftPosition = new Position();
			switch (side) {
			case UP:
				shiftPosition.setRow(0);
				break;
			case RIGHT:
				shiftPosition.setCol(6);
				break;
			case DOWN:
				shiftPosition.setRow(6);
				break;
			case LEFT:
				shiftPosition.setCol(0);
				break;
			}

			// Iterate over all all three shift positions per edge
			for (int positionAxis = 1; positionAxis <= 5; positionAxis += 2) {
				if (side == Side.UP || side == Side.DOWN) {
					shiftPosition.setCol(positionAxis);
				}
				else {
					shiftPosition.setRow(positionAxis);
				}
				// Add only if this is not the forbidden position
				if (!shiftPosition.equals(forbiddenPosition)) {
					positionList.add(new Position(shiftPosition));
				}
			}
		}
		return positionList;
	}

	public static ArrayList<MNA_Move> getAllBoardMoves(Board oldBoard) {
		ArrayList<MNA_Move> allMoves = new ArrayList<MNA_Move>();
		// Iterate over all legal shift positions
		for (Position shiftPosition : getShiftPositions(oldBoard)) {
			// Iterate over all possible rotations of the shift card
			for (Card shiftRotation : new Card(oldBoard.getShiftCard()).getPossibleRotations()) {
				MNA_Move tempMove = new MNA_Move();
				tempMove.setShiftCard(shiftRotation);
				tempMove.setShiftPosition(shiftPosition);
				Board board = doMovement(0, oldBoard, tempMove);
				tempMove.setAfterMove(board);
				allMoves.add(tempMove);
			}
		}
		return allMoves;
	}
	
	public static Board doMovement(int playerID, Board oldBoard, MNA_Move move) {
		MoveMessageType moveMessage = new MoveMessageType();
		moveMessage.setShiftPosition(move.getShiftPosition());
		moveMessage.setShiftCard(move.getShiftCard());
		Board board = oldBoard.fakeShift(moveMessage);
		if ((playerID != 0) && (move.getMovePosition() != null)) {
			movePlayer(playerID, board, move.getMovePosition().getRow(), move.getMovePosition().getCol());
		}
		return board;
	}
	
	public static void movePlayer(int playerID, Board board, int row, int col) {
		Position p = new Position(board.findPlayer(playerID));
		List<Integer> pinPlayer = board.getCard(p.getRow(), p.getCol()).getPin().getPlayerID();
		pinPlayer.remove(pinPlayer.indexOf(playerID));
		board.getCard(row, col).getPin().getPlayerID().add(playerID);
		p = new Position(board.findPlayer(playerID));
	}
	
	public static ArrayList<MNA_Move> getAllMoves(int playerID, ArrayList<MNA_Move> boardMoves) {
		ArrayList<MNA_Move> allMoves = new ArrayList<MNA_Move>();
		for (MNA_Move boardMove : boardMoves) {
			for (PositionType position : boardMove.getAfterMove().getAllReachablePositions(boardMove.getAfterMove().findPlayer(playerID))) {
				MNA_Move tempMove = new MNA_Move(boardMove);
				tempMove.setMovePosition(position);
				movePlayer(playerID, tempMove.getAfterMove(), position.getRow(), position.getCol());
				allMoves.add(tempMove);
			}
		}
		return allMoves;
	}
	
	public static int getDistance(PositionType a, PositionType b) {
		if (a == null || b == null) {
			return -1;
		}
		else {
			return Math.abs(a.getCol() - b.getCol()) + Math.abs(a.getRow() - b.getRow());
		}
	}
	
	public static boolean isShiftable(PositionType position) {
		if (((position.getCol() % 2) == 0) && ((position.getRow() % 2) == 0)) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static boolean isWrongSide(Board board, PositionType position, PositionType treasurePosition) {
		CardType.Openings treasureCard = board.getCard(treasurePosition.getRow(), treasurePosition.getCol()).getOpenings();
		short higher = 0, righter = 0;
		short diffRow = (short) (position.getRow() - treasurePosition.getRow());
		short diffCol = (short) (position.getCol() - treasurePosition.getCol());
		if (diffRow > 0) {
			higher = 1;
		}
		else if (diffRow < 0) {
			higher = -1;
		}
		if (diffCol > 0) {
			righter = 1;
		}
		else if (diffCol < 0) {
			righter = -1;
		}
		if (treasureCard.isTop()) {
			if (higher > 0) {
				if ((righter == 0) || (Math.abs(diffCol) < Math.abs(diffRow))) {
					return false;
				}
			}
		}
		if (treasureCard.isRight()) {
			if (righter > 0) {
				if ((higher == 0) || (Math.abs(diffCol) > Math.abs(diffRow))) {
					return false;
				}
			}
		}
		if (treasureCard.isBottom()) {
			if (higher < 0) {
				if ((righter == 0) || (Math.abs(diffCol) < Math.abs(diffRow))) {
					return false;
				}
			}
		}
		if (treasureCard.isLeft()) {
			if (righter < 0) {
				if ((higher == 0) || (Math.abs(diffCol) > Math.abs(diffRow))) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static TreasureType getLastTreasure(int playerID) {
		return TreasureType.fromValue("Start0" + playerID);
	}
	
	public static boolean isLastTreasure(TreasureType treasure) {
		return treasure.name().startsWith("ST");
	}
	
	public static Position getVerticalTreasureNeighbor(Position treasurePosition) {
		Position neighbor = new Position(treasurePosition);
		if (neighbor.getRow() == 0) {
			neighbor.setRow(1);
		}
		else if (neighbor.getRow() == 6) {
			neighbor.setRow(5);
		}
		return neighbor;
	}
	
	public static Position getHorizontalTreasureNeighbor(Position treasurePosition) {
		Position neighbor = new Position(treasurePosition);
		if (neighbor.getCol() == 0) {
			neighbor.setCol(1);
		}
		else if (neighbor.getCol() == 6) {
			neighbor.setCol(5);
		}
		return neighbor;
	}
}
