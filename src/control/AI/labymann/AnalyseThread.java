package control.AI.labymann;

import java.util.List;

import model.Board;
import model.Card;
import model.jaxb.MoveMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;

public class AnalyseThread extends Thread {
	public enum Side {UP, RIGHT, DOWN, LEFT}
	private enum Points {
		OWN_START(Integer.MAX_VALUE),
		OWN_TARGET(100),
		OTHER_START_OPEN(-50),
		OTHER_TREASURE_REACHABLE(-25);
		
		private final int value;
		
		private Points(int v) {
			this.value = v;
		}
	}
	
	private Parameter p;
	private Side side;
	
	public AnalyseThread(Parameter p, Side side) {
		this.p = p;
		this.side = side;
	}
	
	private Move analyseBoard(Board board, PositionType shiftPos, Card shiftCard) {
		Move move = new Move();
		int boardValue = 0;
		int posValue = Integer.MIN_VALUE;
		move.setShiftCard(shiftCard);
		move.setShiftPosition(shiftPos);
		MoveMessageType moveMessage = new MoveMessageType();
		moveMessage.setShiftCard(shiftCard);
		moveMessage.setShiftPosition(shiftPos);
		board.proceedShift(moveMessage);
		for (TreasuresToGoType ttg : p.getTreasuresToGo()) {
			PositionType playerPos = board.findPlayer(ttg.getPlayer());
			List<PositionType> reachablePos = board.getAllReachablePositions(playerPos);
			// Check for open start fields of opponents
			if (ttg.getTreasures() == 1) {
				PositionType targetPos = board.findTreasure(TreasureType.valueOf("START_0" + ttg.getPlayer()));
				for (PositionType pos : reachablePos) {
					if (pos.getCol() == targetPos.getCol() && pos.getRow() == targetPos.getRow()) {
						// If own start field is reachable and last target, return best move and finish
						if (ttg.getPlayer() == p.getPlayerID()) {
							move.setMovePosition(targetPos);
							move.setValue(Points.OWN_START.value);
							return move;
						}
						else {
							boardValue += Points.OTHER_START_OPEN.value;
						}
					}
				}
				continue;
			}
			if (ttg.getPlayer() == p.getPlayerID()) {
				continue;
			}
			// Count reachable treasures of opponent in relation to full number of treasures
			int treasureCounter = 0;
			for (PositionType pos : reachablePos) {
				TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
				if ((ttype != null) && (ttype != p.getTreasure())) {
					treasureCounter++;
				}
			}
			boardValue += (int) (treasureCounter * 1.0 / ((TreasureType.values().length - 4) / p.getPlayerCount())) * Points.OTHER_TREASURE_REACHABLE.value;
			
		}
		PositionType tPos = board.findTreasure(p.getTreasure());
		for (PositionType pos : board.getAllReachablePositions(board.findPlayer(p.getPlayerID()))) {
			int tempValue = board.getAllReachablePositions(pos).size();
			int dist = (12 - Math.abs(pos.getCol() - tPos.getCol()) - Math.abs(pos.getRow() - tPos.getRow()));
			if (dist == 12) {
				tempValue += Points.OWN_TARGET.value;
			}
			else {
				tempValue += 2 * dist;
			}
			if (tempValue > posValue) {
				posValue = tempValue;
				move.setMovePosition(pos);
			}
		}
		move.setValue(boardValue + posValue);
		return move;
	}
	
	public void run() {
		Card shiftCard = new Card(p.getBoard().getShiftCard());
		Move bestMove = new Move();
		Move tempMove;
		bestMove.setValue(Integer.MIN_VALUE);
		PositionType shiftPos = new PositionType();
		PositionType forbidden = p.getBoard().getForbidden();
		if (forbidden == null) {
			forbidden = new PositionType();
			forbidden.setCol(-1);
			forbidden.setRow(-1);
		}
		switch (side) {
		case UP:
			shiftPos.setRow(0);
			break;
		case RIGHT:
			shiftPos.setCol(6);
			break;
		case DOWN:
			shiftPos.setRow(6);
			break;
		case LEFT:
			shiftPos.setCol(0);
			break;
		default:
			throw new IllegalStateException("Side state not allowed!");
		}
		p.getLock().lock();
		outer:
		for (int i = 1; i < 6; i += 2) {
			if ((side == Side.UP) || (side == Side.DOWN)) {
				shiftPos.setCol(i);
			}
			else {
				shiftPos.setRow(i);
			}
			if (shiftPos.getCol() == forbidden.getCol() && shiftPos.getRow() == forbidden.getRow()) {
				continue;
			}
			for (Card tempCard : shiftCard.getPossibleRotations()) {
				tempMove = analyseBoard((Board) p.getBoard().clone(), shiftPos, tempCard);
				if (tempMove.compareTo(bestMove) == 1) {
					bestMove = tempMove;
				}
				if (bestMove.getValue() == Points.OWN_START.value) {
					break outer;
				}
			}
			
		}
		p.getMoves().add(bestMove);
		p.getLock().unlock();
	}
}
