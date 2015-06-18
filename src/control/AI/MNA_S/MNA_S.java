package control.AI.MNA_S;

import java.util.ArrayList;

import model.Board;
import model.Position;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.ErrorType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;
import model.jaxb.WinMessageType;
import control.AI.Player;
import control.AI.MNA_S.Assist;
import control.network.Connection;

public class MNA_S implements Player {
	private final Connection connection;
	private int playerID;
	
	private int playerCount = 0;
	private ArrayList<TreasureType> treasuresFound;
	private ArrayList<TreasuresToGoType> treasuresToGo;
	private TreasureType treasure;
	private Board board;
	private Assist assist;
	private Move lastMove;

	// ################################################# //
	// ---------------- LOGICAL METHODS ---------------- //
	// ################################################# //
	
	@Override
	public String login() {
		return "MNA_S";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		this.playerID = message.getNewID();
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		Move finalMove = assist.calculateMove();
		sendMoveMessage(playerID, finalMove.getShiftCard(), finalMove.getShiftPosition(), finalMove.getMovePosition());
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		System.out.println("I have been disconnected! Reason: " + message.getErrorCode().name());
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
			System.out.println("LAST MOVE:\nCARD: " + lastMove.getShiftCard().getShape().name() + " " + lastMove.getShiftCard().getOrientation().name() + 
					"\nPOS: " + new Position(lastMove.getShiftPosition()) + "\nMOVE: " + new Position(lastMove.getMovePosition()));
		}
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
	}
	
	// ################################################# //
	// ---------------- DEFAULT METHODS ---------------- //
	// ################################################# //
	
	public MNA_S(Connection connection) {
		this.connection = connection;
	}

	public int getPlayerID() {
		return playerID;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public ArrayList<TreasureType> getTreasuresFound() {
		return treasuresFound;
	}

	public ArrayList<TreasuresToGoType> getTreasuresToGo() {
		return treasuresToGo;
	}

	public TreasureType getTreasure() {
		return treasure;
	}

	public Board getBoard() {
		return board;
	}

}
