package control.AI.LAMB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Board;
import model.Card;
import model.Position;
import model.jaxb.MoveMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;

public class Assist {
	public enum Side {UP, RIGHT, DOWN, LEFT}
	public enum Points {
		OWN_START(5000),
		OWN_TARGET(100),
		TARGET_MISSING(-10),
		OTHER_START_OPEN(-100),
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
				if (shiftPosition.equals(forbiddenPosition)) {
					continue;
				}
				for (Card shiftRotation : new Card(lboard.getShiftCard()).getPossibleRotations()) {
					Board board = (Board) lboard.clone();
					MoveMessageType moveMessage = new MoveMessageType();
					moveMessage.setShiftCard(shiftRotation);
					moveMessage.setShiftPosition(shiftPosition);
					board.proceedShift(moveMessage);
					int boardValue = calculateBoardValue(lamb.getPlayerID(), board, lamb.getTreasuresToGo(), lamb.getTreasuresFound(), lamb.getTreasure());
					for (PositionType position : board.getAllReachablePositions(board.findPlayer(lamb.getPlayerID()))) {
						Move tempMove = new Move();
						tempMove.setShiftCard(shiftRotation);
						tempMove.setShiftPosition(shiftPosition);
						tempMove.setMovePosition(new Position(position));
						tempMove.setValue(boardValue + calculatePositionValue(lamb.getPlayerID(), board, new Position(position), board.findTreasure(lamb.getTreasure()), lamb.getTreasure()));
						moves.add(tempMove);
//						System.out.println(tempMove);
					}
				}
			}
		}
//		System.out.println(Collections.max(moves));
		return Collections.max(moves);
	}
	
	public static int calculateBoardValue(int playerID, Board board, List<TreasuresToGoType> ttgo, List<TreasureType> tfound, TreasureType treasure) {
		int boardValue = 0;
		for (TreasuresToGoType ttg : ttgo) {
			PositionType playerPos = board.findPlayer(ttg.getPlayer());
			List<PositionType> reachablePos = board.getAllReachablePositions(playerPos);
			if (ttg.getPlayer() == playerID) {
					// Count own reachable treasures in relation to full number of remaining treasures
					int treasureCounter = 0;
					for (PositionType pos : reachablePos) {
						TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
						if ((ttype != null) && (ttype != treasure) && !tfound.contains(ttype)) {
							treasureCounter++;
						}
					}
					boardValue -= (int) (2.0 * treasureCounter / ttg.getTreasures()) * Points.OTHER_TREASURE_REACHABLE.value();
			}
			else {
				if (ttg.getTreasures() == 1) {
					// Check if opponent might have the chance to win after this move
					for (PositionType pos : reachablePos) {
						if (new Position(board.findTreasure(TreasureType.fromValue("Start0" + ttg.getPlayer()))).equals(new Position(pos))) {
							boardValue += Points.OTHER_START_OPEN.value();
						}
					}
				}
				else {
					// Count reachable treasures of opponent in relation to full number of treasures
					int treasureCounter = 0;
					for (PositionType pos : reachablePos) {
						TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
						if ((ttype != null) && (ttype != treasure) && !tfound.contains(ttype)) {
							treasureCounter++;
						}
					}
					boardValue += (int) (1.0 * treasureCounter / ttg.getTreasures()) * Points.OTHER_TREASURE_REACHABLE.value();
				}
			}
			if (board.findTreasure(treasure) == null) {
				boardValue += Points.TARGET_MISSING.value();
			}
		}
		return boardValue;
	}
	
	public static int calculatePositionValue(int playerID, Board board, Position position, PositionType tPosition, TreasureType treasure) {
		int positionValue = 2 * board.getAllReachablePositions(position).size();
		// Calculate the distance to currently needed target
		if (tPosition != null) {
			int dist = Math.abs(position.getCol() - tPosition.getCol()) - Math.abs(position.getRow() - tPosition.getRow());
			if (dist == 0) {
				positionValue += Points.OWN_TARGET.value();
			}
			else {
				positionValue += 2 * (12 - dist);
			}
		}
		if (treasure.name().startsWith("Start0") && position.equals(tPosition)) {
			positionValue += Points.OWN_START.value();
		}
		return positionValue;
	}
}
