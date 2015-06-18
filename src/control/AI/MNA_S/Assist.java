package control.AI.MNA_S;

import java.util.ArrayList;

import model.Board;
import model.Card;
import model.Position;
import model.jaxb.MoveMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import control.AI.MNA_S.Move;

@SuppressWarnings("unused")
/**
 * Assistance class for MNA_S.
 * Contains all logic.
 */
public class Assist {
	/**
	 * Enumeration to represent the four edges of the board.
	 */
	private enum Side {
		UP, RIGHT, DOWN, LEFT
	}

	/**
	 * Enumeration for putting weights to diverse events.
	 */
	private enum Points {
		OWN_START(5000),
		OWN_TARGET(100),
		TARGET_MISSING(-10),
		OTHER_START_OPEN(-700),
		TREASURE_REACHABLE(25);

		private final int value;

		private Points(int v) {
			this.value = v;
		}

		public int value() {
			return value;
		}
	}
	
	/**
	 * The MNA_S instance which is using this Assist instance.
	 */
	private final MNA_S mna;
	
	/**
	 * Constructor for MNA_S.
	 * Links an MNA_S instance with this Assist instance.
	 * 
	 * @param mna
	 * @return Assist
	 */
	public Assist(MNA_S mna) {
		this.mna = mna;
	}
	
	/**
	 * Delivers the final movement decision.
	 * Decides which calculation to use.
	 * 
	 * @return Move
	 */
	public Move getMove() {
		Move finalMove;
		// Use a final move if it is possible to finish the game
		// Calculate a normal move otherwise
		if (isLastTreasure(mna.getTreasure())) {
			finalMove = isFinishable(mna.getPlayerID(), mna.getBoard());
			if (finalMove == null) {
				finalMove = calculateMove();
			}
		}
		else {
			finalMove = calculateMove();
		}
		return finalMove;
	}
	
	/**
	 * Main calculation method for mid game calculations.
	 * 
	 * @return Move
	 */
	private Move calculateMove() {
		return new Move();
	}
	
	// ################################################# //
	// ---------------- HELPER METHODS ----------------- //
	// ################################################# //
	
	/**
	 * Decides if the given treasure is a starting field.
	 * @param treasure
	 * @return boolean
	 */
	private boolean isLastTreasure(TreasureType treasure) {
		return treasure.name().startsWith("St");
	}
	
	/**
	 * Returns a list of positions in which the shift card
	 * can be put.
	 * 
	 * @param oldBoard
	 * @return ArrayList<Position>
	 */
	private ArrayList<Position> getShiftPositions(Board oldBoard) {
		ArrayList<Position> positionList = new ArrayList<Position>();
		
		// Create senseless values, if there is no forbidden position
		Position forbiddenPosition;
		if (oldBoard.getForbidden() == null) {
			forbiddenPosition = new Position();
			forbiddenPosition.setCol(-1);
			forbiddenPosition.setRow(-1);
		} else {
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
				} else {
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
	
	/**
	 * Decides if the given player can finish the game with the
	 * current given board.
	 * Returns a Move instance if this is possible, null otherwise.
	 * 
	 * @param playerID
	 * @param oldBoard
	 * @return Move
	 */
	private Move isFinishable(int playerID, Board oldBoard) {
		TreasureType treasure = TreasureType.valueOf("START_0" + playerID);
		
		// Iterate over all legal shift positions
		for (Position shiftPosition : getShiftPositions(oldBoard)) {
			MoveMessageType moveMessage = new MoveMessageType();
			moveMessage.setShiftPosition(shiftPosition);
			
			// Iterate over all possible rotations of the shift card
			for (Card shiftRotation : new Card(oldBoard.getShiftCard()).getPossibleRotations()) {
				// Create a shifted board
				moveMessage.setShiftCard(shiftRotation);
				Board board = oldBoard.fakeShift(moveMessage);
				PositionType tPosition = board.findTreasure(treasure);
				
				// Return a move if the treasure is reachable
				if (board.pathPossible(board.findPlayer(playerID), tPosition)) {
					Move finalMove = new Move();
					finalMove.setShiftCard(shiftRotation);
					finalMove.setShiftPosition(shiftPosition);
					finalMove.setMovePosition(new Position(tPosition));
					return finalMove;
				}
			}
		}
		return null;
	}
}
