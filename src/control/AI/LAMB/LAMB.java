package control.AI.LAMB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

import model.Board;
import model.Position;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.ErrorType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasuresToGoType;
import model.jaxb.WinMessageType;
import control.AI.Player;
import control.network.Connection;

public class LAMB implements Player {
	private int playerID;
	private Parameter parameter;
	private final Connection connection;
	private ReentrantLock moveLock;
	private ArrayList<Position> lastPos;
	private boolean loop;

	public LAMB(Connection connection) {
		this.connection = connection;
	}

	private void calculateMove() {
		/* -------------------- INITIALIZATION -------------------- */
		ArrayList<Move> moves = new ArrayList<Move>();
		ArrayList<AnalyseThread> threads = new ArrayList<AnalyseThread>();
		parameter.setMoves(moves);
		/* --------------------- CALCULATIONS --------------------- */
		for (Assist.Side s : Assist.Side.values()) {
			threads.add(new AnalyseThread(parameter, s));
			threads.get(threads.size() - 1).start();
		}
		for (AnalyseThread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				System.err.println("Thread " + t.getId() + " has been interrupted!");
			}
		}
		threads.clear();
		Move bestMove;
		if (loop) {
			moves.remove(Collections.max(moves));
			bestMove = Collections.max(moves);
		}
		else {
			bestMove = Collections.max(moves);
		}
		moves.clear();
		sendMoveMessage(playerID, bestMove.getShiftCard(), bestMove.getShiftPosition(), bestMove.getMovePosition());
	}

	@Override
	public String login() {
		return "LAMB";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		this.playerID = message.getNewID();
		moveLock = new ReentrantLock();
		parameter = new Parameter();
		parameter.setLock(moveLock);
		parameter.setPlayerID(playerID);
		System.out.println("Login successful.");
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		parameter.setPlayerCount(message.getTreasuresToGo().size());
		parameter.setTreasure(message.getTreasure());
		parameter.setBoard(new Board(message.getBoard()));
		parameter.setTreasuresToGo(message.getTreasuresToGo());
		parameter.setTreasuresFound(message.getFoundTreasures());
		if (lastPos == null) {
			loop = false;
			lastPos = new ArrayList<Position>();
			for (TreasuresToGoType ttg : message.getTreasuresToGo()) {
				lastPos.add(new Position(parameter.getBoard().findPlayer(ttg.getPlayer())));
			}
			lastPos.trimToSize();
			for (TreasuresToGoType ttg : message.getTreasuresToGo()) {
				lastPos.set(ttg.getPlayer() - 1, new Position(parameter.getBoard().findPlayer(ttg.getPlayer())));
			}
		}
		else {
			loop = true;
			for (TreasuresToGoType ttg : message.getTreasuresToGo()) {
				if (!lastPos.get(ttg.getPlayer() - 1).equals(parameter.getBoard().findPlayer(ttg.getPlayer()))) {
					loop = false;
					break;
				}
			}
		}
		calculateMove();
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		System.out.println("I have been disconnected!");
		connection.sendDisconnect(message.getErrorCode(), playerID);
	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		System.out.println("Player " + message.getWinner().getValue() + " (" + message.getWinner().getId() + ") has won the game!");
		if (message.getWinner().getId() == playerID) {
			connection.sendWin(message.getWinner());
		}
	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		if (message.getErrorCode() != ErrorType.NOERROR) {
			System.out.println(message.getErrorCode().value());
		}
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
