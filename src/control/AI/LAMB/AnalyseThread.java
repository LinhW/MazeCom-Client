//package control.AI.LAMB;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import model.Board;
//import model.Card;
//import model.Position;
//import model.jaxb.MoveMessageType;
//import model.jaxb.PositionType;
//import model.jaxb.TreasureType;
//import model.jaxb.TreasuresToGoType;
//import control.AI.LAMB.Assist.Points;
//import control.AI.LAMB.Assist.Side;
//
//public class AnalyseThread extends Thread {	
//	private Parameter p;
//	private Side side;
//	private static final int max_iterations = 2;
//	
//	public AnalyseThread(Parameter p, Side side) {
//		this.p = p;
//		this.side = side;
//	}
//	
//	private void main(Parameter p, int iteration) {
//		// Initialize
//		Card shiftCard = new Card(p.getBoard().getShiftCard());
//		Move bestMove = new Move();
//		Move tempMove;
//		bestMove.setValue(Integer.MIN_VALUE);
//		Position shiftPos = new Position();
//		Position forbidden;
//		if (p.getBoard().getForbidden() == null) {
//			forbidden = new Position();
//			forbidden.setCol(-1);
//			forbidden.setRow(-1);
//		}
//		else {
//			forbidden = new Position(p.getBoard().getForbidden());
//		}
//		switch (side) {
//		case UP:
//			shiftPos.setRow(0);
//			break;
//		case RIGHT:
//			shiftPos.setCol(6);
//			break;
//		case DOWN:
//			shiftPos.setRow(6);
//			break;
//		case LEFT:
//			shiftPos.setCol(0);
//			break;
//		default:
//			throw new IllegalStateException("Side state not allowed!");
//		}
//
//		// Iteration
//		outer:
//			for (int i = 1; i < 6; i += 2) {
//				if ((side == Side.UP) || (side == Side.DOWN)) {
//					shiftPos.setCol(i);
//				}
//				else {
//					shiftPos.setRow(i);
//				}
//				if (shiftPos.equals(forbidden)) {
//					continue;
//				}
//				for (Card tempCard : shiftCard.getPossibleRotations()) {
//					tempMove = analyseBoard((Board) p.getBoard().clone(), shiftPos, tempCard, iteration);
//					if (tempMove.compareTo(bestMove) == 1) {
//						bestMove = tempMove;
//					}
//					if (bestMove.getValue() == Points.OWN_START.value()) {
//						break outer;
//					}
//				}
//
//			}
//
//		// Add move
//		p.getLock().lock();
//		p.getMoves().add(bestMove);
//		p.getLock().unlock();
//	}
//	
//	private Move analyseBoard(Board board, Position shiftPos, Card shiftCard, int iteration) {
//		// Initialization
//		Move move = new Move();
//		int boardValue = 0;
//		int posValue = Integer.MIN_VALUE;
//		move.setShiftCard(shiftCard);
//		move.setShiftPosition(shiftPos);
//		MoveMessageType moveMessage = new MoveMessageType();
//		moveMessage.setShiftCard(shiftCard);
//		moveMessage.setShiftPosition(shiftPos);
//		board.proceedShift(moveMessage);
//		
//		// Calculate board value
//		for (TreasuresToGoType ttg : p.getTreasuresToGo()) {
//			PositionType playerPos = board.findPlayer(ttg.getPlayer());
//			List<PositionType> reachablePos = board.getAllReachablePositions(playerPos);
//			if (ttg.getPlayer() == p.getPlayerID()) {
//				if (ttg.getTreasures() == 1) {
//					// Check if player can end the game with this move
//					for (PositionType pos : reachablePos) {
//						if (new Position(board.findTreasure(TreasureType.fromValue("Start0" + p.getPlayerID()))).equals(new Position(pos))) {
//							move.setMovePosition(new Position(pos));
//							move.setValue(Points.OWN_START.value());
//							return move;
//						}
//					}
//				}
//				else {
//					// Count own reachable treasures in relation to full number of remaining treasures
//					int treasureCounter = 0;
//					for (PositionType pos : reachablePos) {
//						TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
//						if ((ttype != null) && (ttype != p.getTreasure()) && !p.getTreasuresFound().contains(ttype)) {
//							treasureCounter++;
//						}
//					}
//					boardValue -= (int) (2.0 * treasureCounter / ttg.getTreasures()) * Points.OTHER_TREASURE_REACHABLE.value();
//				}
//			}
//			else {
//				if (ttg.getTreasures() == 1) {
//					// Check if opponent might have the chance to win after this move
//					for (PositionType pos : reachablePos) {
//						if (new Position(board.findTreasure(TreasureType.fromValue("Start0" + ttg.getPlayer()))).equals(new Position(pos))) {
//							boardValue += Points.OTHER_START_OPEN.value();
//						}
//					}
//				}
//				else {
//					// Count reachable treasures of opponent in relation to full number of treasures
//					int treasureCounter = 0;
//					for (PositionType pos : reachablePos) {
//						TreasureType ttype = board.getCard(pos.getRow(), pos.getCol()).getTreasure();
//						if ((ttype != null) && (ttype != p.getTreasure()) && !p.getTreasuresFound().contains(ttype)) {
//							treasureCounter++;
//						}
//					}
//					boardValue += (int) (1.0 * treasureCounter / ttg.getTreasures()) * Points.OTHER_TREASURE_REACHABLE.value();
//				}
//			}
//		}
//		
//		// Calculate position value
//		PositionType tPos = board.findTreasure(p.getTreasure());
//		for (PositionType pos : board.getAllReachablePositions(board.findPlayer(p.getPlayerID()))) {
//			// Initialize with number of reachable positions after this move
//			int tempValue = 2 * board.getAllReachablePositions(pos).size();
//			// Calculate the distance to currently needed target
//			if (tPos != null) {
//				int dist = (12 - Math.abs(pos.getCol() - tPos.getCol()) - Math.abs(pos.getRow() - tPos.getRow()));
//				if (dist == 12) {
//					tempValue += Points.OWN_TARGET.value();
//				}
//				else {
//					tempValue += 2 * dist;
//				}
//			}
//			else {
//				tempValue += Points.TARGET_MISSING.value();
//			}
//			if (tempValue > posValue) {
//				posValue = tempValue;
//				move.setMovePosition(new Position(pos));
//			}
//		}
//		int nextValue = 0;
//		if (iteration < max_iterations) {
//			Parameter np = new Parameter(p);
//			ArrayList<Move> nmoves = new ArrayList<Move>();
//			np.setMoves(nmoves);
//			np.setBoard((Board) board.clone());
//			main(np, iteration + 1);
//			nextValue = Collections.max(nmoves).getValue();
//		}
//		move.setValue(boardValue + posValue + nextValue);
//		return move;
//	}
//	
//	public void run() {
//		main(p, 1);
//	}
//}
