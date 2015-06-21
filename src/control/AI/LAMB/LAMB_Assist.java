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

public class LAMB_Assist {
	public enum Side {
		UP,
		RIGHT,
		DOWN,
		LEFT
	}

	public enum LAMB_Points {
		OWN_START(5000),
		OWN_TARGET(100),
		TARGET_MISSING(-10),
		OTHER_START_OPEN(-700),
		TREASURE_REACHABLE(25);

		private final int value;

		private LAMB_Points(int v) {
			this.value = v;
		}

		public int value() {
			return value;
		}
	}

	private LAMB lamb;
	private boolean[] canFindTreasure;
	private int depth;
	private int maxDepth;
	private ArrayList<TreasureType> allTreasures;
	private ArrayList<TreasureType> tempFound;
	private int nextPlayer;

	public LAMB_Assist(LAMB lamb) {
		this.lamb = lamb;
		this.depth = 1;
		this.maxDepth = 2;
		this.allTreasures = new ArrayList<TreasureType>();
		for (TreasureType treasure : TreasureType.values()) {
			allTreasures.add(treasure);
		}
		this.tempFound = new ArrayList<TreasureType>();
	}

	public LAMB_Move randomMove(Board oldBoard, int playerID, TreasureType treasure) {
		LAMB_Move finalMove = new LAMB_Move();
		ArrayList<Position> shiftPositions = getShiftPositions(oldBoard);
		Collections.shuffle(shiftPositions);
		finalMove.setShiftPosition(new Position(shiftPositions.get(0)));
		finalMove.setShiftCard(new Card(oldBoard.getShiftCard()));
		MoveMessageType moveMessage = new MoveMessageType();
		moveMessage.setShiftCard(new Card(oldBoard.getShiftCard()));
		moveMessage.setShiftPosition(shiftPositions.get(0));
		Board b = oldBoard.fakeShift(moveMessage);
		List<PositionType> positions = b.getAllReachablePositions(b.findPlayer(playerID));
		int distance = 12;
		PositionType treasurePosition = oldBoard.findTreasure(treasure);
		for (PositionType movePosition : positions) {
			if (getDistance(treasurePosition, movePosition) < distance) {
				distance = getDistance(treasurePosition, movePosition);
				finalMove.setMovePosition(movePosition);
			}
		}
		return finalMove;
	}

	public LAMB_Move calculateMove() {
		canFindTreasure = new boolean[4];
		tempFound.clear();
		ArrayList<Integer> players = new ArrayList<Integer>();
		for (TreasuresToGoType t : lamb.getTreasuresToGo()) {
			players.add(t.getPlayer());
		}
		Collections.sort(players);
		nextPlayer = players.get((players.indexOf(lamb.getPlayerID()) + 1) % players.size());
		LAMB_Move finalMove;
		if (lamb.getTreasure().name().startsWith("Start0")) {
			finalMove = calculateFinishMove(lamb.getPlayerID(), lamb.getBoard(), lamb.getTreasuresToGo(),
					lamb.getTreasuresFound(), lamb.getTreasure());
		}
		else {
			finalMove = calculateMove(lamb.getPlayerID(), lamb.getBoard(), lamb.getTreasuresToGo(),
					lamb.getTreasuresFound(), lamb.getTreasure());
		}
		ArrayList<Position> lp = lamb.getLastPositions();
		if (lp.size() == 2 && lp.get(0).equals(finalMove.getMovePosition())
				&& lp.get(1).equals(finalMove.getMovePosition())) {
			finalMove = randomMove(lamb.getBoard(), lamb.getPlayerID(), lamb.getTreasure());
		}
		return finalMove;
	}

	private ArrayList<Position> getShiftPositions(Board oldBoard) {
		ArrayList<Position> positionList = new ArrayList<Position>();
		Position forbiddenPosition;
		if (oldBoard.getForbidden() == null) {
			forbiddenPosition = new Position();
			forbiddenPosition.setCol(-1);
			forbiddenPosition.setRow(-1);
		}
		else {
			forbiddenPosition = new Position(oldBoard.getForbidden());
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
				if (!shiftPosition.equals(forbiddenPosition)) {
					positionList.add(new Position(shiftPosition));
				}
			}
		}
		return positionList;
	}

	private LAMB_Move isFinishable(int playerID, Board oldBoard) {
		TreasureType treasure = TreasureType.valueOf("START_0" + playerID);
		for (Position shiftPosition : getShiftPositions(oldBoard)) {
			for (Card shiftRotation : new Card(oldBoard.getShiftCard()).getPossibleRotations()) {
				Board board = (Board) oldBoard.clone();
				MoveMessageType moveMessage = new MoveMessageType();
				moveMessage.setShiftCard(shiftRotation);
				moveMessage.setShiftPosition(shiftPosition);
				board.proceedShift(moveMessage);
				PositionType tPosition = board.findTreasure(treasure);
				if (board.pathPossible(board.findPlayer(playerID), tPosition)) {
					LAMB_Move finalMove = new LAMB_Move();
					finalMove.setShiftCard(shiftRotation);
					finalMove.setShiftPosition(shiftPosition);
					finalMove.setMovePosition(new Position(tPosition));
					return finalMove;
				}
			}
		}
		return null;
	}

	private LAMB_Move calculateFinishMove(int playerID, Board oldBoard, List<TreasuresToGoType> ttgo,
			List<TreasureType> tfound, TreasureType treasure) {
		LAMB_Move finalMove = isFinishable(playerID, oldBoard);
		if (finalMove == null) {
			ArrayList<LAMB_Move> shortestMoves = new ArrayList<LAMB_Move>();
			int distance = 12;
			for (Position shiftPosition : getShiftPositions(oldBoard)) {
				for (Card shiftRotation : new Card(oldBoard.getShiftCard()).getPossibleRotations()) {
					Board board = (Board) oldBoard.clone();
					MoveMessageType moveMessage = new MoveMessageType();
					moveMessage.setShiftCard(shiftRotation);
					moveMessage.setShiftPosition(shiftPosition);
					board.proceedShift(moveMessage);
					PositionType treasurePosition = board.findTreasure(treasure);
					for (PositionType movePosition : board
							.getAllReachablePositions(board.findPlayer(playerID))) {
						if (getDistance(movePosition, treasurePosition) <= distance) {
							if (getDistance(movePosition, treasurePosition) < distance) {
								shortestMoves.clear();
								distance = getDistance(movePosition, treasurePosition);
							}
							LAMB_Move temp = new LAMB_Move();
							temp.setShiftCard(shiftRotation);
							temp.setShiftPosition(shiftPosition);
							temp.setMovePosition(movePosition);
							shortestMoves.add(temp);
						}
					}
				}
			}
			for (LAMB_Move move : shortestMoves) {
				MoveMessageType moveMessage = new MoveMessageType();
				moveMessage.setShiftCard(move.getShiftCard());
				moveMessage.setShiftPosition(move.getShiftPosition());
				Board board = oldBoard.fakeShift(moveMessage);
				move.setValue(calculateBoardValue(playerID, board, ttgo, tfound, treasure));
			}
			finalMove = Collections.max(shortestMoves);
		}
		return finalMove;
	}

	@SuppressWarnings("unchecked")
	private LAMB_Move calculateMove(int playerID, Board oldBoard, List<TreasuresToGoType> ttgo,
			List<TreasureType> tfound, TreasureType treasure) {
		canFindTreasure[playerID - 1] = false;
		ArrayList<LAMB_Move> moves = new ArrayList<LAMB_Move>();
		if (treasure == null) {
			ArrayList<TreasureType> notFound = (ArrayList<TreasureType>) allTreasures.clone();
			notFound.removeAll(tfound);
			notFound.remove(tempFound);
			notFound.remove(lamb.getTreasure());
			Collections.shuffle(notFound);
			treasure = notFound.get(0);
		}
		for (Position shiftPosition : getShiftPositions(oldBoard)) {
			for (Card shiftRotation : new Card(oldBoard.getShiftCard()).getPossibleRotations()) {
				Board board = (Board) oldBoard.clone();
				MoveMessageType moveMessage = new MoveMessageType();
				moveMessage.setShiftCard(shiftRotation);
				moveMessage.setShiftPosition(shiftPosition);
				board.proceedShift(moveMessage);
				PositionType tPosition = board.findTreasure(treasure);
				int boardValue = calculateBoardValue(playerID, board, ttgo, tfound, treasure);
				for (PositionType position : board.getAllReachablePositions(board.findPlayer(playerID))) {
					LAMB_Move tempMove = new LAMB_Move();
					int positionValue = calculatePositionValue(playerID, board, new Position(position),
							tPosition, treasure);
					tempMove.setShiftCard(shiftRotation);
					tempMove.setShiftPosition(shiftPosition);
					tempMove.setMovePosition(new Position(position));
					tempMove.setValue(boardValue + positionValue);
					moves.add(tempMove);
				}
			}
		}
		LAMB_Move finalMove;
		if (!canFindTreasure[playerID - 1] && (depth < maxDepth) && !treasure.name().startsWith("Start0")) {
			depth++;
			int median = 0;
			for (LAMB_Move m : moves) {
				median += m.getValue();
			}
			median = median / moves.size();
			median = (Collections.max(moves).getValue() + median) / 2;
			for (int i = 0; i < moves.size(); i++) {
				LAMB_Move m = moves.get(i);
				if (m.getValue() > median) {
					calculateNewMove(playerID, oldBoard, m);
				}
				else {
					moves.remove(i);
					i--;
				}
			}
			depth--;
			finalMove = Collections.max(moves);
		}
		else {
			finalMove = Collections.max(moves);
		}
		return finalMove;
	}

	private void calculateNewMove(int playerID, Board oldBoard, LAMB_Move oldMove) {
		Board board = (Board) oldBoard.clone();
		MoveMessageType moveMessage = new MoveMessageType();
		LAMB_Move tempMove;
		moveMessage.setShiftCard(oldMove.getShiftCard());
		moveMessage.setShiftPosition(oldMove.getShiftPosition());
		board.proceedShift(moveMessage);
		movePlayer(playerID, board, oldMove.getMovePosition().getRow(), oldMove.getMovePosition().getCol());
		for (int i = 1; i < lamb.getPlayerCount(); i++) {
			int tempID = playerID + i;
			if (tempID > lamb.getPlayerCount()) {
				tempID -= lamb.getPlayerCount();
			}
			tempMove = calculateMove(tempID, board, lamb.getTreasuresToGo(), lamb.getTreasuresFound(), null);
			moveMessage = new MoveMessageType();
			moveMessage.setShiftCard(tempMove.getShiftCard());
			moveMessage.setShiftPosition(tempMove.getShiftPosition());
			board.proceedShift(moveMessage);
			movePlayer(tempID, board, tempMove.getMovePosition().getRow(), tempMove.getMovePosition().getCol());
		}
		tempMove = calculateMove(playerID, board, lamb.getTreasuresToGo(), lamb.getTreasuresFound(),
				lamb.getTreasure());
		oldMove.setValue(oldMove.getValue() + tempMove.getValue());
	}

	private int calculateBoardValue(int playerID, Board board, List<TreasuresToGoType> ttgo,
			List<TreasureType> tfound, TreasureType treasure) {
		int boardValue = 0;
		for (TreasuresToGoType ttg : ttgo) {
			PositionType playerPos = board.findPlayer(ttg.getPlayer());
			List<PositionType> reachablePos = board.getAllReachablePositions(playerPos);
			if (ttg.getPlayer() == playerID) {
				// Count own reachable treasures in relation to full number of
				// remaining treasures
				int treasureCounter = 0;
				for (PositionType pos : reachablePos) {
					TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
					if ((ttype != null) && (ttype != treasure) && !tfound.contains(ttype)) {
						treasureCounter++;
					}
				}
				boardValue += (int) (2.0 * treasureCounter / ttg.getTreasures())
						* LAMB_Points.TREASURE_REACHABLE.value();
			}
			else {
				if (ttg.getPlayer() == nextPlayer && ttg.getTreasures() == 1) {
					if (isFinishable(nextPlayer, board) != null) {
						boardValue += LAMB_Points.OTHER_START_OPEN.value();
					}
				}
				// Count reachable treasures of opponent in relation to full
				// number of remaining treasures
				int treasureCounter = 0;
				for (PositionType pos : reachablePos) {
					TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
					if ((ttype != null) && (ttype != treasure) && !tfound.contains(ttype)) {
						treasureCounter++;
					}
				}
				boardValue -= (int) (1.0 * treasureCounter / ttg.getTreasures())
						* LAMB_Points.TREASURE_REACHABLE.value();
			}
		}
		if (board.findTreasure(treasure) == null) {
			boardValue += LAMB_Points.TARGET_MISSING.value();
		}
		return boardValue;
	}

	private int calculatePositionValue(int playerID, Board board, Position position, PositionType tPosition,
			TreasureType treasure) {
		int positionValue = 2 * board.getAllReachablePositions(position).size();
		// Calculate the distance to currently needed target
		if (tPosition != null) {
			if (position.equals(tPosition)) {
				positionValue += LAMB_Points.OWN_TARGET.value();
				canFindTreasure[playerID - 1] = true;
				tempFound.add(treasure);
			}
			else {
				positionValue += 2 * (12 - getDistance(position, tPosition));
			}
		}
		return positionValue;
	}

	private void movePlayer(int playerID, Board board, int row, int col) {
		Position p = new Position(board.findPlayer(playerID));
		List<Integer> pinPlayer = board.getCard(p.getRow(), p.getCol()).getPin().getPlayerID();
		pinPlayer.remove(pinPlayer.indexOf(playerID));
		board.getCard(row, col).getPin().getPlayerID().add(playerID);
		p = new Position(board.findPlayer(playerID));
	}

	private int getDistance(PositionType a, PositionType b) {
		if (a == null || b == null) {
			return -1;
		}
		else {
			return Math.abs(a.getCol() - b.getCol()) + Math.abs(a.getRow() - b.getRow());
		}
	}
}
