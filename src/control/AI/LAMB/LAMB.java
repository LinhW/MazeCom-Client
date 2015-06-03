package control.AI.LAMB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import model.Board;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import model.jaxb.WinMessageType;
import control.AI.Player;
import control.network.Connection;

public class LAMB implements Player {
	private int playerID;
	private Parameter parameter;
	private final Connection connection;
	private ArrayList<TreasureType> treasures_to_go;
	private ReentrantLock moveLock;
	
	public LAMB(Connection connection) {
		this.connection = connection;
		first_move = true;
	}
	
	private void calculateMove() {
		/* -------------------- INITIALIZATION -------------------- */
		ArrayList<Move> moves = new ArrayList<Move>();
		ArrayList<AnalyseThread> threads = new ArrayList<AnalyseThread>();
		parameter.setMoves(moves);
		/* --------------------- CALCULATIONS --------------------- */
		for (AnalyseThread.Side s : AnalyseThread.Side.values()) {
			threads.add(new AnalyseThread(parameter, s));
			threads.get(threads.size() -1).start();
		}
		for (AnalyseThread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				System.err.println("Thread " + t.getId() + " has been interrupted!");
			}
		}
		threads.clear();
		Move bestMove = Collections.max(moves);
		moves.clear();
		sendMoveMessage(playerID, bestMove.getShiftCard(), bestMove.getShiftPosition(), bestMove.getMovePosition());
	}
	
	@Override
	public String login() {
//		String name = JOptionPane.showInputDialog("Nickname");
//		return name + "(Labymann)";
		return "Labymann";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		this.playerID = message.getNewID();
		treasures_to_go = new ArrayList<TreasureType>();
		for (TreasureType t : TreasureType.values()) {
			treasures_to_go.add(t);
		}
		treasures_to_go.trimToSize();
		moveLock = new ReentrantLock();
		parameter = new Parameter();
		parameter.setLock(moveLock);
		parameter.setPlayerID(playerID);
		System.out.println("Login successful.");
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		List<TreasureType> found = message.getFoundTreasures();
		for (TreasureType t : found) {
			treasures_to_go.remove(t);
		}
		parameter.setPlayerCount(message.getTreasuresToGo().size());
		parameter.setTreasure(message.getTreasure());
		parameter.setBoard(new Board(message.getBoard()));
		parameter.setTreasuresToGo(message.getTreasuresToGo());
		calculateMove();
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		System.out.println("I have been disconnected!");
	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		System.out.println("Player " + message.getWinner().getValue() + " (" + message.getWinner().getId() + ") has won the game!");
	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		System.out.println(message.getErrorCode().value());
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
