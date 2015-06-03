package control.AI.LAMB;

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
		// Calculate board value
		for (TreasuresToGoType ttg : p.getTreasuresToGo()) {
			PositionType playerPos = board.findPlayer(ttg.getPlayer());
			List<PositionType> reachablePos = board.getAllReachablePositions(playerPos);
			if (ttg.getPlayer() == p.getPlayerID()) {
				continue;
			}
			// Count reachable treasures of opponent in relation to full number of treasures
			int treasureCounter = 0;
			for (PositionType pos : reachablePos) {
				TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
				if ((ttype != null) && (ttype != p.getTreasure()) && p.getTreasuresFound().contains(ttype)) {
					treasureCounter++;
				}
			}			boardValue += (int) (1.0 * treasureCounter / ttg.getTreasures()) * Points.OTHER_TREASURE_REACHABLE.value;
			
		}
		// calculate position value
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
//			System.out.println(posValue + " " + new Position(pos).toString());
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
			if ((shiftPos.getCol() == forbidden.getCol()) && (shiftPos.getRow() == forbidden.getRow())) {
				continue;
			}
//			System.out.println("ShiftPos: " + new Position(shiftPos).toString());
			for (Card tempCard : shiftCard.getPossibleRotations()) {
//				System.out.println("Card: " + tempCard.getShape().name() + " " + tempCard.getOrientation().name());
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
