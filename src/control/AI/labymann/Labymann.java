package control.AI.labymann;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

import model.Board;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.BoardType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import model.jaxb.WinMessageType;
import control.AI.Player;
import control.network.Connection;

public class Labymann implements Player {
	private int playerID;
	private boolean first_move;
	private AnalyseThread.Parameter parameter;
	private final Connection connection;
	private ArrayList<TreasureType> treasures_to_go;
	private ReentrantLock moveLock;
	
	public Labymann(Connection connection) {
		this.connection = connection;
		first_move = true;
	}
	
	private void calculateMove(BoardType board, TreasureType treasure) {
		/* -------------------- INITIALIZATION -------------------- */
		ArrayList<Move> moves = new ArrayList<Move>();
		ArrayList<AnalyseThread> threads = new ArrayList<AnalyseThread>();
		parameter.setBoard(new Board(board));
		parameter.setMoves(moves);
		/* --------------------- CALCULATIONS --------------------- */
		for (AnalyseThread.Side s : AnalyseThread.Side.values()) {
			AnalyseThread t = new AnalyseThread(parameter, s);
			threads.add(t);
			t.start();
		}
		for (AnalyseThread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				System.err.println("Thread " + t.getId() + " has been interrupted!");
			}
		}
		Move bestMove = Collections.max(moves);
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
		parameter = new AnalyseThread.Parameter();
		parameter.setLock(moveLock);
		parameter.setPlayerID(playerID);
		System.out.println("Login successful.");
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		if (first_move) {
			parameter.setPlayerCount(message.getTreasuresToGo().size());
			first_move = false;
		}
		List<TreasureType> found = message.getFoundTreasures();
		for (TreasureType t : found) {
			treasures_to_go.remove(t);
		}
		message.getTreasuresToGo().size();
		calculateMove(message.getBoard(), message.getTreasure());
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		JOptionPane.showMessageDialog(null, "I have been disconnected...",
				"Disconnected", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		JOptionPane.showMessageDialog(null, message.getWinner().getValue() +
				" has won the game", "Game over", JOptionPane.INFORMATION_MESSAGE);
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
