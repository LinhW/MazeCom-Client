package control.AI.MNA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Board;
import model.Position;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;

public class MNA_Logic {
	private enum MNA_Points {
		OWN_TARGET(100),
		TARGET_MISSING(-10),
		OTHER_START_OPEN(-700),
		TREASURE_REACHABLE(25),
		POSITION_WRONG_SIDE(-10),
		POSITION_SHIFTABLE(-5);

		private final int value;

		private MNA_Points(int v) {
			this.value = v;
		}

		public int value() {
			return value;
		}
	}

	private MNA mna;
	private int maxRecursionDepth;
	private int recursionDepth;
	private int playerCount;
	private ArrayList<Integer> allPlayers;
	private boolean[] canFindTreasure;
	private int[] remainingTreasures;
	private ArrayList<Position> lastPositions;
	private ArrayList<TreasureType> treasuresFound;
	private ArrayList<TreasureType> tempFound;
	private ArrayList<TreasuresToGoType> ttgo;

	public MNA_Logic(MNA mna) {
		this.mna = mna;
		tempFound = new ArrayList<TreasureType>();
		lastPositions = new ArrayList<Position>();
		remainingTreasures = new int[4];
		canFindTreasure = new boolean[4];
	}

	public MNA_Move getMove() {
		treasuresFound = mna.getTreasuresFound();
		ttgo = mna.getTreasuresToGo();
		tempFound.clear();
		calculatePlayers();

		MNA_Move finalMove = calculateMove(mna.getPlayerID(), mna.getBoard(), mna.getTreasure());

		lastPositions.add(finalMove.getMovePosition());
		if (lastPositions.size() == 3) {
			if (lastPositions.get(2).equals(lastPositions.get(0))
					&& lastPositions.get(2).equals(lastPositions.get(1))) {
				// TODO Break loop
			}
			lastPositions.remove(0);
			lastPositions.trimToSize();
		}

		return finalMove;
	}

	private MNA_Move calculateMove(int playerID, Board board, TreasureType treasure) {
		MNA_Move finalMove = null;
		int blockID = 0;
		for (int i = 0; i < 4; i++) {
			if ((i != playerID - 1) && (remainingTreasures[i] == 1)) {
				blockID = i + 1;
			}
		}
		if (treasure == null) {
			ArrayList<TreasureType> notFound = MNA_Assist.getAllTreasures();
			notFound.removeAll(treasuresFound);
			notFound.remove(tempFound);
			notFound.remove(mna.getTreasure());
			Collections.shuffle(notFound);
			treasure = notFound.get(0);
		}
		canFindTreasure[playerID - 1] = false;
		
		if (blockID == 0) {
			ArrayList<MNA_Move> allMoves = MNA_Assist.getAllBoardMoves(board);
			for (MNA_Move boardMove : allMoves) {
				PositionType tPosition = boardMove.getAfterMove().findTreasure(treasure);
				boardMove.setTreasurePosition(tPosition);
				boardMove.setValue(calculateBoardValue(playerID, boardMove.getAfterMove(), treasure, tPosition));
			}
			allMoves = MNA_Assist.getAllMoves(playerID, allMoves);
			for (MNA_Move positionMove : allMoves) {
				positionMove.addValue(calculatePositionValue(playerID, positionMove.getAfterMove(), positionMove
						.getMovePosition(), positionMove.getTreasurePosition(), treasure));
			}
			if (!canFindTreasure[playerID - 1] && (recursionDepth < maxRecursionDepth)) {
				recursionDepth++;
				for (MNA_Move move : allMoves) {
					calculateNewMove(playerID, move);
				}
				recursionDepth--;
			}
			finalMove = Collections.max(allMoves);
			if (finalMove.getMovePosition().equals(finalMove.getTreasurePosition())) {
				tempFound.add(treasure);
			}
		}
		else {
			ArrayList<MNA_Move> allMoves = MNA_Assist.getAllBoardMoves(board);
		}
		return finalMove;
	}
	
	private void calculateNewMove(int playerID, MNA_Move move) {
		MNA_Move tempMove;
		Board board = (Board) move.getAfterMove().clone();
		for (int i = 1; i < playerCount; i++) {
			int tempID = getNextPlayer(playerID);
			tempMove = calculateMove(tempID, board, null);
			board = MNA_Assist.doMovement(playerID, board, tempMove);
		}
		tempMove = calculateMove(playerID, board, mna.getTreasure());
		move.addValue(tempMove.getValue());
	}

	private int calculateBoardValue(int playerID, Board board, TreasureType treasure, PositionType tPosition) {
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
					* MNA_Points.TREASURE_REACHABLE.value();
		}
		if (tPosition == null) {
			boardValue += MNA_Points.TARGET_MISSING.value();
		}
		return boardValue;
	}

	private int calculatePositionValue(int playerID, Board board, Position position, PositionType tPosition,
			TreasureType treasure) {
		int positionValue = 2 * board.getAllReachablePositions(position).size();
		// Calculate the distance to currently needed target
		if (tPosition != null) {
			if (position.equals(tPosition)) {
				positionValue += MNA_Points.OWN_TARGET.value();
				canFindTreasure[playerID - 1] = true;
			}
			else {
				positionValue += 2 * (12 - MNA_Assist.getDistance(position, tPosition));
			}
			if (MNA_Assist.isWrongSide(board, position, tPosition)) {
				positionValue += MNA_Points.POSITION_WRONG_SIDE.value();
			}
		}
		if (MNA_Assist.isShiftable(position)) {
			positionValue += MNA_Points.POSITION_SHIFTABLE.value();
		}
		return positionValue;
	}

	private void calculatePlayers() {
		allPlayers = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			remainingTreasures[i] = -1;
			canFindTreasure[i] = false;
		}
		for (TreasuresToGoType ttg : ttgo) {
			allPlayers.add(ttg.getPlayer());
			remainingTreasures[ttg.getPlayer() - 1] = ttg.getTreasures();
		}
		allPlayers.trimToSize();
		playerCount = allPlayers.size();
		Collections.sort(allPlayers);
	}
	
	private int getNextPlayer(int playerID) {
		return allPlayers.get((allPlayers.indexOf(playerID) + 1) % playerCount);
	}
}
