package control.AI.MNA_S;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Board;
import model.Card;
import model.Position;
import model.jaxb.CardType;
import model.jaxb.MoveMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;

/**
 * Assistance class for MNA_S. Contains all logic.
 */
public class MNA_S_Assist {
	// ##################################################################################################### //
	// ------------------------------------------- CONTROLLINGS -------------------------------------------- //
	// ##################################################################################################### //

	/**
	 * Enumeration to represent the four edges of the board.
	 */
	private enum Side {
		UP,
		RIGHT,
		DOWN,
		LEFT
	}

	/**
	 * Enumeration for putting weights to diverse events.
	 */
	private enum MNA_S_Points {
		OWN_TARGET(100),
		TARGET_MISSING(-10),
		OTHER_START_OPEN(-700),
		TREASURE_REACHABLE(25),
		POSITION_WRONG_SIDE(-10),
		POSITION_SHIFTABLE(-5);

		private final int value;

		private MNA_S_Points(int v) {
			this.value = v;
		}

		public int value() {
			return value;
		}
	}

	/**
	 * The MNA_S instance which is using this MNA_S_Assist instance.
	 */
	private final MNA_S mna;
	private final int maxRecursionDepth = 2;
	private ArrayList<TreasureType> allTreasures;

	// ##################################################################################################### //
	// ------------------------------------------- TEMPORAL VARS ------------------------------------------- //
	// ##################################################################################################### //

	private int playerCount;
	private int recursionDepth;
	private ArrayList<Integer> allPlayers;
	private boolean[] canFindTreasure;
	private int[] remainingTreasures;
	private ArrayList<Position> lastPositions;
	private ArrayList<TreasureType> treasuresFound;
	private ArrayList<TreasureType> tempFound;
	private ArrayList<TreasuresToGoType> ttgo;

	/**
	 * Constructor for MNA_S. Links an MNA_S instance with this MNA_S_Assist instance.
	 * 
	 * @param mna
	 * @return MNA_S_Assist
	 */
	public MNA_S_Assist(MNA_S mna) {
		this.mna = mna;
		canFindTreasure = new boolean[4];
		remainingTreasures = new int[4];
		recursionDepth = 1;
		lastPositions = new ArrayList<Position>();
		allTreasures = new ArrayList<TreasureType>();
		for (TreasureType treasure : TreasureType.values()) {
			allTreasures.add(treasure);
		}
		this.tempFound = new ArrayList<TreasureType>();
	}

	// ##################################################################################################### //
	// -------------------------------------- MOVE SELECTION METHODS --------------------------------------- //
	// ##################################################################################################### //

	/**
	 * Delivers the final movement decision. Decides which calculation to use.
	 * 
	 * @return MNA_S_Move
	 */
	public MNA_S_Move getMove() {
		tempFound.clear();
		ttgo = mna.getTreasuresToGo();
		treasuresFound = mna.getTreasuresFound();
		calculatePlayers(mna.getTreasuresToGo(), mna.getPlayerID());
		for (int i = 0; i < 4; i++) {
			canFindTreasure[i] = false;
		}
		
		MNA_S_Move finalMove = null;
		boolean blocked = false;
		// Use a final move if it is possible to finish the game
		int nextPlayer = getNextPlayer(mna.getPlayerID());
		while (playerCount > 1 && nextPlayer != mna.getPlayerID()) {
			if (remainingTreasures[nextPlayer - 1] == 1) {
				finalMove = calculateBlockMove(mna.getPlayerID(), mna.getBoard(), nextPlayer, mna.getTreasure());
				blocked = true;
				break;
			}
			nextPlayer = getNextPlayer(nextPlayer);
		}
		if (!blocked) {
			if (isLastTreasure(mna.getTreasure())) {
				finalMove = calculateFinishMove(mna.getPlayerID(), mna.getBoard(), mna.getTreasure());
			}
			// Calculate a move
			else {
				finalMove = calculateMove(mna.getPlayerID(), mna.getBoard(), mna.getTreasure());
			}
			lastPositions.add(finalMove.getMovePosition());
			if (lastPositions.size() == 3) {
				if (lastPositions.get(2).equals(lastPositions.get(0)) && lastPositions.get(2).equals(lastPositions.get(1))) {
					finalMove = randomMove(mna.getPlayerID(), mna.getBoard(), mna.getTreasure());
				}
				lastPositions.remove(0);
				lastPositions.trimToSize();
			}
		}
		if (finalMove == null) {
			System.out.println("NULL");
		}
		return finalMove;
	}

	private MNA_S_Move calculateMove(int playerID, Board oldBoard, TreasureType treasure) {
		canFindTreasure[playerID - 1] = false;
		ArrayList<MNA_S_Move> moves = new ArrayList<MNA_S_Move>();
		if (treasure == null) {
			ArrayList<TreasureType> notFound = new ArrayList<TreasureType>(allTreasures);
			notFound.removeAll(treasuresFound);
			notFound.remove(tempFound);
			notFound.remove(mna.getTreasure());
			Collections.shuffle(notFound);
			treasure = notFound.get(0);
		}
		for (MNA_S_Move boardMove : getAllBoardMoves(oldBoard)) {
			Board board = doMovement(0, oldBoard, boardMove);
			PositionType tPosition = board.findTreasure(treasure);
			int boardValue = calculateBoardValue(playerID, board, treasure);
			for (PositionType position : board.getAllReachablePositions(board.findPlayer(playerID))) {
				MNA_S_Move tempMove = new MNA_S_Move(boardMove);
				int positionValue = calculatePositionValue(playerID, board, new Position(position),
						tPosition, treasure);
				tempMove.setMovePosition(new Position(position));
				tempMove.setValue(boardValue + positionValue);
				moves.add(tempMove);
			}
		}
		MNA_S_Move finalMove;
		if (!canFindTreasure[playerID - 1] && (recursionDepth < maxRecursionDepth) && !treasure.name().startsWith("ST")) {
			recursionDepth++;
			int median = 0;
			for (MNA_S_Move m : moves) {
				median += m.getValue();
			}
			median = median / moves.size();
			median = (Collections.max(moves).getValue() + median) / 2;
			for (int i = 0; i < moves.size(); i++) {
				MNA_S_Move m = moves.get(i);
				if (m.getValue() > median) {
					calculateNewMove(playerID, oldBoard, m);
				}
				else {
					moves.remove(i);
					i--;
				}
			}
			recursionDepth--;
		}
		finalMove = Collections.max(moves);
		return finalMove;
	}
	
	private void calculateNewMove(int playerID, Board oldBoard, MNA_S_Move oldMove) {
		Board board = (Board) oldBoard.clone();
		MoveMessageType moveMessage = new MoveMessageType();
		MNA_S_Move tempMove;
		moveMessage.setShiftCard(oldMove.getShiftCard());
		moveMessage.setShiftPosition(oldMove.getShiftPosition());
		board.proceedShift(moveMessage);
		movePlayer(playerID, board, oldMove.getMovePosition().getRow(), oldMove.getMovePosition().getCol());
		for (int i = 1; i < playerCount; i++) {
			int tempID = playerID + i;
			if (tempID > playerCount) {
				tempID -= playerCount;
			}
			tempMove = calculateMove(tempID, board, null);
			moveMessage = new MoveMessageType();
			moveMessage.setShiftCard(tempMove.getShiftCard());
			moveMessage.setShiftPosition(tempMove.getShiftPosition());
			board.proceedShift(moveMessage);
			movePlayer(tempID, board, tempMove.getMovePosition().getRow(), tempMove.getMovePosition().getCol());
		}
		tempMove = calculateMove(playerID, board, mna.getTreasure());
		oldMove.setValue(oldMove.getValue() + tempMove.getValue() / (playerCount * 3));
	}
	
/*	/**
//	 * Main calculation method for mid game calculations.
//	 * 
//	 * @return MNA_S_Move
//	 */
//	private MNA_S_Move calculateMove(int playerID, Board oldBoard, TreasureType treasure) {
//		MNA_S_Move finalMove = null;
//		if (treasure == null) {
//			ArrayList<TreasureType> notFound = new ArrayList<TreasureType>(allTreasures);
//			notFound.removeAll(treasuresFound);
//			notFound.remove(tempFound);
//			notFound.remove(mna.getTreasure());
//			Collections.shuffle(notFound);
//			treasure = notFound.get(0);
//		}
//		ArrayList<MNA_S_Move> allMoves = getAllBoardMoves(oldBoard);
//		int nextPlayer = getNextPlayer(playerID);
//		while (nextPlayer != playerID) {
//			if (remainingTreasures[nextPlayer - 1] == 1) {
//				allMoves = blockOpponent(playerID, nextPlayer, oldBoard, treasure);
//			}
//			nextPlayer = getNextPlayer(nextPlayer);
//		}
//		canFindTreasure[playerID - 1] = false;
//
//		// TODO Threads
//		// Iterate over all board moves
//		for (MNA_S_Move boardMove : allMoves) {
//			Board board = doMovement(0, oldBoard, boardMove);
//			// Ignore moves that lead to win of next player
//			PositionType treasurePosition = board.findTreasure(treasure);
//			int boardValue = calculateBoardValue(playerID, board, treasure);
//
//			// Iterate over all possible positions for this board move
//			int maxValue = Integer.MIN_VALUE;
//			for (PositionType position : board.getAllReachablePositions(board.findPlayer(playerID))) {
//				int positionValue = calculatePositionValue(playerID, board, new Position(position),
//						treasurePosition, treasure);
//				if (positionValue > maxValue) {
//					maxValue = positionValue;
//					boardMove.setMovePosition(position);
//					boardMove.setValue(boardValue + positionValue);
//				}
//			}
//		}
//
//		if (playerCount < 3) {
//			if (!canFindTreasure[playerID - 1] && recursionDepth < maxRecursionDepth) {
//				recursionDepth++;
//				ArrayList<Integer> tempPlayers = new ArrayList<Integer>(allPlayers);
//				Collections.rotate(tempPlayers, tempPlayers.size() - (tempPlayers.indexOf(playerID) + 1));
//				for (MNA_S_Move move : allMoves) {
//					Board board = doMovement(playerID, oldBoard, move);
//					MNA_S_Move secondMove ;
//					for (int i = 0; i < tempPlayers.size() - 1; i++) {
//						secondMove = null;
//						int tempID = tempPlayers.get(i);
//						if (remainingTreasures[tempID - 1] == 1) {
//							secondMove = calculateFinishMove(tempID, board, getLastTreasure(tempID));
//						}
//						if (secondMove == null) {
//							secondMove = calculateMove(tempID, board, null);
//						}
//						board = doMovement(tempID, board, secondMove);
//					}
//					secondMove = calculateMove(playerID, board, treasure);
//					move.setValue(move.getValue() + secondMove.getValue());
//				}
//				recursionDepth--;
//			}
//			finalMove = Collections.max(allMoves);
//		}
//		else {
//			for (MNA_S_Move move : allMoves) {
//				Board board = doMovement(0, oldBoard, move);
//				if (board.findTreasure(treasure) != null) {
//					List<PositionType> playerWays = board.getAllReachablePositions(board.findPlayer(playerID));
//					List<PositionType> treasureWays = board.getAllReachablePositions(board.findTreasure(treasure));
//					int distance = 12;
//					for (PositionType playerPosition : playerWays) {
//						for (PositionType treasurePosition : treasureWays) {
//							if (getDistance(playerPosition, treasurePosition) <= distance) {
//								distance = getDistance(playerPosition, treasurePosition);
//								move.setMovePosition(playerPosition);
//								move.setValue(12 - distance);
//							}
//							if (getDistance(playerPosition, treasurePosition) == 0) {
//								move.setValue(MNA_S_Points.OWN_TARGET.value());
//							}
//						}
//					}
//				}
//			}
//			finalMove = Collections.max(allMoves);
//		}
//
//		return finalMove;
//	}*/
	
	/**
	 * Calculates a suitable move for end game.
	 * 
	 * @param playerID
	 * @param oldBoard
	 * @param ttgo
	 * @param tfound
	 * @param treasure
	 * @return MNA_S_Move
	 */
	private MNA_S_Move calculateFinishMove(int playerID, Board oldBoard, TreasureType treasure) {
		MNA_S_Move finalMove = isFinishable(playerID, oldBoard);
		if (finalMove == null) {
			ArrayList<MNA_S_Move> allMoves = getAllBoardMoves(oldBoard);
			for (MNA_S_Move move : allMoves) {
				Board board = doMovement(playerID, oldBoard, move);
				PositionType treasurePosition = board.findTreasure(treasure);
				int maxValue = Integer.MIN_VALUE;
				for (PositionType movePosition : board.getAllReachablePositions(board.findPlayer(playerID))) {
					int positionValue = getDistance(movePosition, treasurePosition);
					if (isShiftable(movePosition)) {
						positionValue += MNA_S_Points.POSITION_SHIFTABLE.value();
					}
					if (positionValue > maxValue) {
						maxValue = positionValue;
						move.setMovePosition(movePosition);
					}
				}
			}
			finalMove = Collections.max(allMoves);
		}
		return finalMove;
	}

	/**
	 * Creates a random shift move and uses the shortest distance to the treasure.
	 * 
	 * @param playerID
	 * @param oldBoard
	 * @param treasure
	 * @return MNA_S_Move
	 */
	private MNA_S_Move randomMove(int playerID, Board oldBoard, TreasureType treasure) {
		ArrayList<MNA_S_Move> allBoardMoves = getAllBoardMoves(oldBoard);
		Collections.shuffle(allBoardMoves);
		MNA_S_Move randomMove = allBoardMoves.get(0);
		Board board = doMovement(0, oldBoard, randomMove);
		List<PositionType> positions = board.getAllReachablePositions(board.findPlayer(playerID));
		int distance = 12;
		PositionType treasurePosition = oldBoard.findTreasure(treasure);
		for (PositionType movePosition : positions) {
			if (getDistance(treasurePosition, movePosition) < distance) {
				distance = getDistance(treasurePosition, movePosition);
				randomMove.setMovePosition(movePosition);
			}
		}
		return randomMove;
	}
	
	private MNA_S_Move calculateBlockMove(int playerID, Board oldBoard, int blockID, TreasureType treasure) {
		ArrayList<MNA_S_Move> tempMoves = new ArrayList<MNA_S_Move>();
		for (MNA_S_Move boardMove : getAllBoardMoves(oldBoard)) {
			Board board = doMovement(0, oldBoard, boardMove);
			MNA_S_Move tempMove = new MNA_S_Move(boardMove);
			int reachablePositions = -2 * board.getAllReachablePositions(board.findTreasure(getLastTreasure(blockID))).size();
			int opponentDistance = 0, ownDistance = 0;
			List<PositionType> playerWays = board.getAllReachablePositions(board.findPlayer(blockID));
			List<PositionType> treasureWays = board.getAllReachablePositions(board.findTreasure(getLastTreasure(blockID)));
			int distance = 0;
			for (PositionType playerPosition : playerWays) {
				for (PositionType treasurePosition : treasureWays) {
					if (getDistance(playerPosition, treasurePosition) > distance) {
						distance = getDistance(board.findPlayer(blockID), board.findTreasure(getLastTreasure(blockID)));
						opponentDistance = 2 * distance;
					}
				}
			}
			if (board.findTreasure(treasure) != null) {
				playerWays = board.getAllReachablePositions(board.findPlayer(playerID));
				treasureWays = board.getAllReachablePositions(board.findTreasure(treasure));
				distance = 12;
				for (PositionType playerPosition : playerWays) {
					for (PositionType treasurePosition : treasureWays) {
						if (getDistance(playerPosition, treasurePosition) < distance) {
							distance = getDistance(board.findPlayer(playerID), board.findTreasure(getLastTreasure(playerID)));
							ownDistance = 2 - distance;
							tempMove.setMovePosition(playerPosition);
							if (distance == 0) {
								if (isLastTreasure(treasure)) {
									return tempMove;
								}
							}
						}
					}
				}
			}
			else {
				tempMove.setMovePosition(board.findPlayer(playerID));
			}
			tempMove.setValue(reachablePositions + opponentDistance + ownDistance);
			tempMoves.add(tempMove);
		}
		if (tempMoves.isEmpty()) {
			System.out.println("GIVE UP!");
			return randomMove(blockID, oldBoard, treasure);
		}
		return Collections.max(tempMoves);
	}
	
	/**
	 * Tries to find a move to block the given player as good as possible.
	 * 
	 * @param playerID
	 * @param nextPlayer
	 * @param oldBoard
	 * @return MNA_S_Move
	 */
//	private ArrayList<MNA_S_Move> blockOpponent(int playerID, int nextPlayer, Board oldBoard, TreasureType treasure) {
//		ArrayList<Integer> tempPlayers = new ArrayList<Integer>(allPlayers);
//		Collections.rotate(tempPlayers, tempPlayers.size() - (tempPlayers.indexOf(playerID) + 1));
//		ArrayList<MNA_S_Move> allMoves = new ArrayList<MNA_S_Move>(getAllBoardMoves(oldBoard));
//		
//		filterFinishableMoves(allMoves, tempPlayers, oldBoard, playerID, nextPlayer);
//		
//		// No way to prevent next player win
//		if (allMoves.size() == 0) {
//			return getAllBoardMoves(oldBoard);
//		}
//		else {
//			return allMoves;
//		}
//	}

	// ##################################################################################################### //
	// --------------------------------- EVALUATION AND FILTERING METHODS ---------------------------------- //
	// ##################################################################################################### //

/*	/**
//	 * Calculates the value for the given board. Uses the number of reachable positions.
//	 * 
//	 * @param playerID
//	 * @param board
//	 * @param ttgo
//	 * @param tfound
//	 * @param treasure
//	 * @return int
//	 */
//	private int calculateBoardValue(int playerID, Board board, TreasureType treasure) {
//		int boardValue = 0;
//
//		for (TreasuresToGoType ttg : ttgo) {
//			int treasureCounter = 0;
//			PositionType playerPos = board.findPlayer(ttg.getPlayer());
//			List<PositionType> reachablePos = board.getAllReachablePositions(playerPos);
//			for (PositionType pos : reachablePos) {
//				TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
//				if ((ttype != null) && (ttype != treasure) && !treasuresFound.contains(ttype)) {
//					treasureCounter++;
//				}
//			}
//			double factor = -1.0;
//			if (ttg.getPlayer() == playerID) {
//				factor = 2.0;
//			}
//			boardValue += (int) (factor * treasureCounter / ttg.getTreasures())
//					* MNA_S_Points.TREASURE_REACHABLE.value();
//		}
//		if (board.findTreasure(treasure) == null) {
//			boardValue += MNA_S_Points.TARGET_MISSING.value();
//		}
//		return boardValue;
//	}*/
	
	private int calculateBoardValue(int playerID, Board board, TreasureType treasure) {
		int boardValue = 0;

		for (TreasuresToGoType ttg : ttgo) {
			int treasureCounter = 0;
			PositionType playerPos = board.findPlayer(ttg.getPlayer());
			List<PositionType> reachablePos = board.getAllReachablePositions(playerPos);
			for (PositionType pos : reachablePos) {
				TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
				if ((ttype != null) && (ttype != treasure) && !treasuresFound.contains(ttype)) {
					treasureCounter++;
				}
			}
			double factor = -1.0;
			if (ttg.getPlayer() == playerID) {
				factor = 2.0;
			}
			boardValue += (int) (factor * treasureCounter / ttg.getTreasures())
					* MNA_S_Points.TREASURE_REACHABLE.value();
		}
		if (board.findTreasure(treasure) == null) {
			boardValue += MNA_S_Points.TARGET_MISSING.value();
		}
		return boardValue;
	}

	private int calculatePositionValue(int playerID, Board board, Position position, PositionType tPosition,
			TreasureType treasure) {
		int positionValue = 2 * board.getAllReachablePositions(position).size();
		// Calculate the distance to currently needed target
		if (tPosition != null) {
			if (position.equals(tPosition)) {
				positionValue += MNA_S_Points.OWN_TARGET.value();
				canFindTreasure[playerID - 1] = true;
				tempFound.add(treasure);
			}
			else {
				positionValue += 2 * (12 - getDistance(position, tPosition));
			}
			if (isWrongSide(board, position, tPosition)) {
				positionValue += MNA_S_Points.POSITION_WRONG_SIDE.value();
			}
		}
		return positionValue;
	}

/*	/**
//	 * Calculates the value for the given position. Uses the number of reachable positions, and the distance
//	 * to the given treasure.
//	 * 
//	 * @param playerID
//	 * @param board
//	 * @param position
//	 * @param tPosition
//	 * @param treasure
//	 * @return
//	 */
//	private int calculatePositionValue(int playerID, Board board, Position position, PositionType tPosition,
//			TreasureType treasure) {
//		int positionValue = 2 * board.getAllReachablePositions(position).size();
//		// Calculate the distance to currently needed target
//		if (tPosition != null) {
//			if (position.equals(tPosition)) {
//				positionValue += MNA_S_Points.OWN_TARGET.value();
//				canFindTreasure[playerID - 1] = true;
//				tempFound.add(treasure);
//			}
//			else {
//				positionValue += 3 * (12 - getDistance(position, tPosition));
//			}
//			if (isWrongSide(board, position, tPosition)) {
//				positionValue += MNA_S_Points.POSITION_WRONG_SIDE.value();
//			}
//		}
//		return positionValue;
//	}*/
	
	private void calculateValues(ArrayList<MNA_S_Move> allMoves, Board oldBoard, int playerID, TreasureType treasure) {
		for (MNA_S_Move move : allMoves) {
			Board board = doMovement(0, oldBoard, move);
			move.setValue(calculateBoardValue(playerID, board, treasure));
			if (board.findTreasure(treasure) != null) {
				List<PositionType> playerWays = board.getAllReachablePositions(board.findPlayer(playerID));
				List<PositionType> treasureWays = board.getAllReachablePositions(board.findTreasure(treasure));
				int distance = 12;
				for (PositionType playerPosition : playerWays) {
					for (PositionType treasurePosition : treasureWays) {
						if (getDistance(playerPosition, treasurePosition) <= distance) {
							distance = getDistance(playerPosition, treasurePosition);
							move.setMovePosition(playerPosition);
							move.setValue(12 - distance);
						}
						if (getDistance(playerPosition, treasurePosition) == 0) {
							move.setValue(MNA_S_Points.OWN_TARGET.value());
						}
					}
				}
			}
			else {
				move.setValue(MNA_S_Points.TARGET_MISSING.value());
			}
		}
	}
	
	private boolean filterFinishableMoves(ArrayList<MNA_S_Move> allMoves, ArrayList<Integer> playerList, Board oldBoard, int playerID, int blockPlayer) {
		//TODO Threads
		if (remainingTreasures[blockPlayer - 1] != 1) {
			return false;
		}
		ArrayList<MNA_S_Move> tempMoves = new ArrayList<MNA_S_Move>();
		for (MNA_S_Move move : allMoves) {
			Board board = doMovement(0, oldBoard, move);
			if (isFinishable(blockPlayer, board) == null) {
				tempMoves.add(move);
			}
		}
		return allMoves.retainAll(tempMoves);
	}

	// ##################################################################################################### //
	// ------------------------------------------ HELPER METHODS ------------------------------------------- //
	// ##################################################################################################### //

	/**
	 * Creates a list with all remaining players in game. Returns the index of the next player.
	 * 
	 * @param ttgo
	 * @param playerID
	 * @return int
	 */
	private void calculatePlayers(List<TreasuresToGoType> ttgo, int playerID) {
		allPlayers = new ArrayList<Integer>();
		for (TreasuresToGoType ttg : ttgo) {
			allPlayers.add(ttg.getPlayer());
			remainingTreasures[ttg.getPlayer() - 1] = ttg.getTreasures();
		}
		allPlayers.trimToSize();
		Collections.sort(allPlayers);
		playerCount = allPlayers.size();
	}

	/**
	 * Fakes performing a move on the board.
	 * 
	 * @param oldBoard
	 * @param move
	 * @return Board
	 */
	private Board doMovement(int playerID, Board oldBoard, MNA_S_Move move) {
		MoveMessageType moveMessage = new MoveMessageType();
		moveMessage.setShiftPosition(move.getShiftPosition());
		moveMessage.setShiftCard(move.getShiftCard());
		Board board = oldBoard.fakeShift(moveMessage);
		if ((playerID != 0) && (move.getMovePosition() != null)) {
			movePlayer(playerID, board, move.getMovePosition().getRow(), move.getMovePosition().getCol());
		}
		return board;
	}

	/**
	 * Returns a list of all moves, without pin moving.
	 * 
	 * @param oldBoard
	 * @return ArrayList<MNA_S_Move>
	 */
	public ArrayList<MNA_S_Move> getAllBoardMoves(Board oldBoard) {
		ArrayList<MNA_S_Move> allMoves = new ArrayList<MNA_S_Move>();
		// Iterate over all legal shift positions
		for (Position shiftPosition : getShiftPositions(oldBoard)) {
			// Iterate over all possible rotations of the shift card
			for (Card shiftRotation : new Card(oldBoard.getShiftCard()).getPossibleRotations()) {
				MNA_S_Move tempMove = new MNA_S_Move();
				tempMove.setShiftCard(shiftRotation);
				tempMove.setShiftPosition(shiftPosition);
				allMoves.add(tempMove);
			}
		}
		return allMoves;
	}

	/**
	 * Calculates the board distance between two positions.
	 * 
	 * @param a
	 * @param b
	 * @return int
	 */
	private int getDistance(PositionType a, PositionType b) {
		if (a == null || b == null) {
			return -1;
		}
		else {
			return Math.abs(a.getCol() - b.getCol()) + Math.abs(a.getRow() - b.getRow());
		}
	}
	
	/**
	 * Returns the start field for the given player.
	 * @param playerID
	 * @return TreasureType
	 */
	private TreasureType getLastTreasure(int playerID) {
		return TreasureType.fromValue("Start0" + playerID);
	}

	/**
	 * Returns the index of the next player.
	 * 
	 * @param playerID
	 * @return int
	 */
	private int getNextPlayer(int playerID) {
		return allPlayers.get((allPlayers.indexOf(playerID) + 1) % playerCount);
	}
	
	/**
	 * Returns a list of positions in which the shift card can be put.
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

	/**
	 * Wrapper for isFinishable(int, Board, ArrayList<MNA_S_Move>). Uses getAllBoardMoves(oldBoard).
	 * 
	 * @param playerID
	 * @param oldBoard
	 * @return MNA_S_Move
	 */
	private MNA_S_Move isFinishable(int playerID, Board oldBoard) {
		return isFinishable(playerID, oldBoard, getAllBoardMoves(oldBoard));
	}

	/**
	 * Decides if the given player can finish the game with the current given board. Returns a MNA_S_Move
	 * instance if this is possible, null otherwise.
	 * 
	 * @param playerID
	 * @param oldBoard
	 * @param allBoardMoves
	 * @return MNA_S_Move
	 */
	private MNA_S_Move isFinishable(int playerID, Board oldBoard, ArrayList<MNA_S_Move> allBoardMoves) {
		// TODO Threads
		if (remainingTreasures[playerID - 1] == 1) {
			TreasureType treasure = TreasureType.valueOf("START_0" + playerID);
			for (MNA_S_Move tempMove : allBoardMoves) {
				// Create a shifted board
				Board board = doMovement(0, oldBoard, tempMove);
				PositionType tPosition = board.findTreasure(treasure);

				// Return a move if the treasure is reachable
				if (board.pathPossible(board.findPlayer(playerID), tPosition)) {
					MNA_S_Move finalMove = new MNA_S_Move();
					finalMove.setShiftCard(tempMove.getShiftCard());
					finalMove.setShiftPosition(tempMove.getShiftPosition());
					finalMove.setMovePosition(new Position(tPosition));
					return finalMove;
				}
			}
		}
		return null;
	}
	
	/**
	 * Decides if the given treasure is a starting field.
	 * 
	 * @param treasure
	 * @return boolean
	 */
	private boolean isLastTreasure(TreasureType treasure) {
		return treasure.name().startsWith("ST");
	}
	
	/**
	 * Returns if the given position is a movable position.
	 * 
	 * @param position
	 * @return boolean
	 */
	private boolean isShiftable(PositionType position) {
		if (((position.getCol() % 2) == 0) && ((position.getRow() % 2) == 0)) {
			return false;
		}
		else {
			return true;
		}
	}
	
	private boolean isWrongSide(Board board, PositionType position, PositionType treasurePosition) {
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

	/**
	 * Moves the player on the given Board.
	 * 
	 * @param playerID
	 * @param board
	 * @param row
	 * @param col
	 */
	private void movePlayer(int playerID, Board board, int row, int col) {
		Position p = new Position(board.findPlayer(playerID));
		List<Integer> pinPlayer = board.getCard(p.getRow(), p.getCol()).getPin().getPlayerID();
		pinPlayer.remove(pinPlayer.indexOf(playerID));
		board.getCard(row, col).getPin().getPlayerID().add(playerID);
		p = new Position(board.findPlayer(playerID));
	}
}
