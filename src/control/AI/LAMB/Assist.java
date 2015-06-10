package control.AI.LAMB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Board;
import model.Card;
import model.Position;
import model.jaxb.MoveMessageType;
import model.jaxb.TreasuresToGoType;

public class Assist {
	public enum Side {UP, RIGHT, DOWN, LEFT}
	public enum Points {
		OWN_START(Integer.MAX_VALUE),
		OWN_TARGET(100),
		TARGET_MISSING(-10),
		OTHER_START_OPEN(-50),
		OTHER_TREASURE_REACHABLE(-25);
		
		private final int value;
		
		private Points(int v) {
			this.value = v;
		}
		
		public int value() {
			return value;
		}
	}
	
	public static Move calculateMove(LAMB lamb) {
		ArrayList<Move> moves = new ArrayList<Move>();
		Board lboard = lamb.getBoard();
		Position forbiddenPosition;
		if (lboard.getForbidden() == null) {
			forbiddenPosition = new Position();
			forbiddenPosition.setCol(-1);
			forbiddenPosition.setRow(-1);
		}
		else {
			forbiddenPosition = new Position(lboard.getForbidden());
		}
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
			for (int positionAxis = 1; positionAxis <= 5; positionAxis += 2) {
				if (side == Side.UP || side == Side.DOWN) {
					shiftPosition.setCol(positionAxis);
				}
				else {
					shiftPosition.setRow(positionAxis);
				}
				for (Card shiftRotation : new Card(lboard.getShiftCard()).getPossibleRotations()) {
					Board board = (Board) lboard.clone();
					MoveMessageType moveMessage = new MoveMessageType();
					moveMessage.setShiftCard(shiftRotation);
					moveMessage.setShiftPosition(shiftPosition);
				}
			}
		}
		return Collections.max(moves);
	}
	
	public static int calculateBoardValue(int playerID, Board board, List<TreasuresToGoType> ttgo) {
		int boardValue = 0;
		
		return boardValue;
	}
	
	public static int calculatePositionValue(int playerID, Board board, Position position) {
		int positionValue = 0;
		
		return positionValue;
	}
}
