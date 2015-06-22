package control.AI.MNA_S;

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

/**
 * Assistance class for MNA_S. Contains all logic.
 */
public class MNA_S_Assist {
	// #################### //
	// --- CONTROLLINGS --- //
	// #################### //

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
		POSITION_SHIFTABLE(20);

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

	// #################### //
	// --- TEMPORAL VAR --- //
	// #################### //

	private int playerCount;
	private int recursionDepth;
	private ArrayList<Integer> allPlayers;
	private boolean[] canFindTreasure;
	private int[] remainingTreasures;
	private ArrayList<Position> lastPositions;
	private ArrayList<TreasureType> tempFound;

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

	// ######################################################################################### //
	// -------------------------------- MOVE SELECTION METHODS --------------------------------- //
	// ######################################################################################### //

	/**
	 * Delivers the final movement decision. Decides which calculation to use.
	 * 
	 * @return MNA_S_Move
	 */
	public MNA_S_Move getMove() {
		calculatePlayers(mna.getTreasuresToGo(), mna.getPlayerID());
		for (int i = 0; i < 4; i++) {
			canFindTreasure[i] = false;
		}
		MNA_S_Move finalMove;
		// Use a final move if it is possible to finish the game
		if (isLastTreasure(mna.getTreasure())) {
			finalMove = calculateFinishMove(mna.getPlayerID(), mna.getBoard(), mna.getTreasuresToGo(),
					mna.getTreasuresFound(), mna.getTreasure());
		}
		// Calculate a move
		else {
			finalMove = calculateMove(mna.getPlayerID(), mna.getBoard(), mna.getTreasuresToGo(),
					mna.getTreasuresFound(), mna.getTreasure());
			lastPositions.add(finalMove.getMovePosition());
			if (lastPositions.size() == 3) {
				if (lastPositions.get(2).equals(lastPositions.get(0)) && lastPositions.get(2).equals(lastPositions.get(1))) {
					finalMove = randomMove(mna.getPlayerID(), mna.getBoard(), mna.getTreasure());
				}
				lastPositions.remove(0);
				lastPositions.trimToSize();
			}
		}
		return finalMove;
	}

	/**
	 * Main calculation method for mid game calculations.
	 * 
	 * @return MNA_S_Move
	 */
	private MNA_S_Move calculateMove(int playerID, Board oldBoard, List<TreasuresToGoType> ttgo,
			List<TreasureType> tfound, TreasureType treasure) {
		MNA_S_Move finalMove = null;
		if (treasure == null) {
			ArrayList<TreasureType> notFound = new ArrayList<TreasureType>(allTreasures);
			notFound.removeAll(tfound);
			notFound.remove(tempFound);
			notFound.remove(mna.getTreasure());
			Collections.shuffle(notFound);
			treasure = notFound.get(0);
		}

		int nextPlayer = getNextPlayer(playerID);
		while (nextPlayer != playerID) {
			if (remainingTreasures[nextPlayer - 1] == 1) {
				finalMove = blockOpponent(playerID, nextPlayer, oldBoard);
				break;
			}
			nextPlayer = getNextPlayer(playerID);
		}
		
		if (finalMove == null) {
			nextPlayer = getNextPlayer(playerID);
			ArrayList<MNA_S_Move> allMoves = new ArrayList<MNA_S_Move>();
			canFindTreasure[playerID - 1] = false;

			// TODO Threads
			// Iterate over all board moves
			for (MNA_S_Move boardMove : getAllBoardMoves(oldBoard)) {
				Board board = doMovement(0, oldBoard, boardMove);
				// Ignore moves that lead to win of next player
				PositionType treasurePosition = board.findTreasure(treasure);
				int boardValue = calculateBoardValue(playerID, board, ttgo, tfound, treasure);

				// Iterate over all possible positions for this board move
				for (PositionType position : board.getAllReachablePositions(board.findPlayer(playerID))) {
					int positionValue = calculatePositionValue(playerID, board, new Position(position),
							treasurePosition, treasure);
					MNA_S_Move tempMove = new MNA_S_Move(boardMove);
					tempMove.setMovePosition(new Position(position));
					tempMove.setValue(boardValue + positionValue);
					allMoves.add(tempMove);
				}
			}

			if (allMoves.size() == 0) {
				return randomMove(playerID, oldBoard, treasure);
			}

			if (!canFindTreasure[playerID - 1] && recursionDepth < maxRecursionDepth) {
				recursionDepth++;
				ArrayList<Integer> tempPlayers = new ArrayList<Integer>(allPlayers);
				Collections.rotate(tempPlayers, tempPlayers.size() - (tempPlayers.indexOf(playerID) + 1));
				for (MNA_S_Move move : allMoves) {
					Board board = doMovement(playerID, oldBoard, move);
					MNA_S_Move secondMove = new MNA_S_Move();
					for (int i = 0; i < tempPlayers.size() - 1; i++) {
						int tempID = tempPlayers.get(i);
						if (remainingTreasures[tempID - 1] == 1) {
							secondMove = calculateFinishMove(tempID, board, ttgo, tfound,
									TreasureType.valueOf("START_0" + tempID));
						}
						if (secondMove == null || remainingTreasures[tempID - 1] != 1) {
							secondMove = calculateMove(tempID, board, ttgo, tfound, null);
						}
						board = doMovement(tempID, board, secondMove);
					}
					secondMove = calculateMove(playerID, board, ttgo, tfound, treasure);
					move.setValue(move.getValue() + (playerCount - 1) * secondMove.getValue());
				}
				recursionDepth--;
			}
			finalMove = Collections.max(allMoves);
		}

		return finalMove;
	}

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
	private MNA_S_Move calculateFinishMove(int playerID, Board oldBoard, List<TreasuresToGoType> ttgo,
			List<TreasureType> tfound, TreasureType treasure) {
		MNA_S_Move finalMove = isFinishable(playerID, oldBoard);
		if (finalMove == null) {
			boolean hasBlocked = false;
			int nextPlayer = getNextPlayer(playerID);
			while (nextPlayer != playerID) {
				if (remainingTreasures[nextPlayer - 1] == 1) {
					finalMove = blockOpponent(playerID, nextPlayer, oldBoard);
					hasBlocked = true;
					break;
				}
				nextPlayer = getNextPlayer(playerID);
			}
			if (finalMove == null) {
				if (hasBlocked) {
					System.out.println("HAS BLOCKED BUT NULL");
				}
				ArrayList<MNA_S_Move> shortestMoves = new ArrayList<MNA_S_Move>();
				int distance = 12;
				for (MNA_S_Move move : getAllBoardMoves(oldBoard)) {
					Board board = doMovement(playerID, oldBoard, move);
					PositionType treasurePosition = board.findTreasure(treasure);
					for (PositionType movePosition : board.getAllReachablePositions(board.findPlayer(playerID))) {
						if (getDistance(movePosition, treasurePosition) <= Math.max(distance, 2)) {
							if (getDistance(movePosition, treasurePosition) < Math.max(distance, 2)) {
								shortestMoves.clear();
								distance = getDistance(movePosition, treasurePosition);
							}
							MNA_S_Move temp = new MNA_S_Move(move);
							temp.setMovePosition(movePosition);
							temp.setValue(calculateBoardValue(playerID, board, ttgo, tfound, treasure));
							if (isShiftable(movePosition)) {
								temp.setValue(temp.getValue() - MNA_S_Points.POSITION_SHIFTABLE.value());
							}
							shortestMoves.add(temp);
						}
					}
				}
				finalMove = Collections.max(shortestMoves);
			}
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
	
	private MNA_S_Move blockOpponent(int playerID, int nextPlayer, Board oldBoard) {
		ArrayList<MNA_S_Move> moves = new ArrayList<MNA_S_Move>();
		PositionType treasurePosition = oldBoard.findTreasure(getLastTreasure(nextPlayer));
		for (MNA_S_Move boardMove : getAllBoardMoves(oldBoard)) {
			Board board = doMovement(0, oldBoard, boardMove);
			int positions = board.getAllReachablePositions(treasurePosition).size();
			MNA_S_Move move = new MNA_S_Move(boardMove);
			if (board.pathPossible(board.findPlayer(playerID), board.findTreasure(mna.getTreasure()))) {
				move.setMovePosition(board.findTreasure(mna.getTreasure()));
				move.setValue(positions - 3);
			}
			else {
				move.setValue(positions);
			}
			moves.add(move);
		}
		MNA_S_Move finalMove = Collections.min(moves);
		if (finalMove.getMovePosition() == null) {
			Board board = doMovement(0, oldBoard, finalMove);
			treasurePosition = board.findTreasure(mna.getTreasure());
			int distance = 12;
			ArrayList<PositionType> allPositions = new ArrayList<PositionType>();
			for (PositionType movePosition : board.getAllReachablePositions(board.findPlayer(playerID))) {
				if (getDistance(movePosition, treasurePosition) <= Math.max(distance, 2)) {
					if (getDistance(movePosition, treasurePosition) < Math.max(distance, 2)) {
						allPositions.clear();
						distance = getDistance(movePosition, treasurePosition);
					}
					allPositions.add(movePosition);
				}
			}
			finalMove.setMovePosition(allPositions.get(0));
			for (PositionType movePosition : allPositions) {
				if (!isShiftable(movePosition)) {
					finalMove.setMovePosition(movePosition);
					break;
				}
			}
		}
		return finalMove;
	}

	// ######################################################################################### //
	// --------------------------- EVALUATION AND FILTERING METHODS ---------------------------- //
	// ######################################################################################### //

	/**
	 * Calculates the value for the given board. Uses the number of reachable positions.
	 * 
	 * @param playerID
	 * @param board
	 * @param ttgo
	 * @param tfound
	 * @param treasure
	 * @return int
	 */
	private int calculateBoardValue(int playerID, Board board, List<TreasuresToGoType> ttgo,
			List<TreasureType> tfound, TreasureType treasure) {
		int boardValue = 0;
		int nextPlayer = getNextPlayer(playerID);

		for (TreasuresToGoType ttg : ttgo) {
			int treasureCounter = 0;
			PositionType playerPos = board.findPlayer(ttg.getPlayer());
			List<PositionType> reachablePos = board.getAllReachablePositions(playerPos);
			for (PositionType pos : reachablePos) {
				TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
				if ((ttype != null) && (ttype != treasure) && !tfound.contains(ttype)) {
					treasureCounter++;
				}
			}
			double factor = -1.0;
			if (ttg.getPlayer() == playerID) {
				factor = 3.0;
			}
			else if (ttg.getPlayer() == nextPlayer) {
				factor = -2.0;
			}
			boardValue += (int) (factor * treasureCounter / ttg.getTreasures())
					* MNA_S_Points.TREASURE_REACHABLE.value();
		}
		if (board.findTreasure(treasure) == null) {
			boardValue += MNA_S_Points.TARGET_MISSING.value();
		}
		return boardValue;
	}

	/**
	 * Calculates the value for the given position. Uses the number of reachable positions, and the distance
	 * to the given treasure.
	 * 
	 * @param playerID
	 * @param board
	 * @param position
	 * @param tPosition
	 * @param treasure
	 * @return
	 */
	private int calculatePositionValue(int playerID, Board board, Position position, PositionType tPosition,
			TreasureType treasure) {
		int positionValue = 2 * board.getAllReachablePositions(position).size();
		// Calculate the distance to currently needed target
		if (tPosition != null) {
			if (position.equals(tPosition)) {
				positionValue += MNA_S_Points.OWN_TARGET.value();
				canFindTreasure[playerID - 1] = true;
			}
			else {
				positionValue += 2 * (12 - getDistance(position, tPosition));
			}
		}
		return positionValue;
	}

	// ######################################################################################### //
	// ------------------------------------ HELPER METHODS ------------------------------------- //
	// ######################################################################################### //

	/**
	 * Decides if the given treasure is a starting field.
	 * 
	 * @param treasure
	 * @return boolean
	 */
	private boolean isLastTreasure(TreasureType treasure) {
		return treasure.name().startsWith("ST");
	}
	
	private TreasureType getLastTreasure(int playerID) {
		return TreasureType.valueOf("START0" + playerID);
	}
	
	private boolean isShiftable(PositionType position) {
		if (((position.getCol() % 2) == 0) && ((position.getRow() % 2) == 0)) {
			return false;
		}
		else {
			return true;
		}
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

	/**
	 * Returns the index of the next player.
	 * 
	 * @param playerID
	 * @return int
	 */
	private int getNextPlayer(int playerID) {
		return allPlayers.get((allPlayers.indexOf(playerID) + 1) % playerCount);
	}
}
