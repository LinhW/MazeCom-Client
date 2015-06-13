package control.AI.LAMB;

import java.util.ArrayList;

import model.Board;
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
import control.network.Connection;

public class LAMB implements Player {
	private final Connection connection;
	private int playerID;
	private int playerCount = 0;
	private ArrayList<TreasureType> treasuresFound;
	private ArrayList<TreasuresToGoType> treasuresToGo;
	private TreasureType treasure;
	private Board board;
	private Assist assist;

	public LAMB(Connection connection) {
		this.connection = connection;
	}

	@Override
	public String login() {
		return "LAMB";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		this.playerID = message.getNewID();
		System.out.println("Login successful.");
		assist = new Assist(this);
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		playerCount = message.getTreasuresToGo().size();
		board = new Board(message.getBoard());
		treasuresToGo = new ArrayList<TreasuresToGoType>(message.getTreasuresToGo());
		treasuresToGo.trimToSize();
		treasuresFound = new ArrayList<TreasureType>(message.getFoundTreasures());
		treasuresFound.trimToSize();
		treasure = message.getTreasure();
		Move move = assist.calculateMove();
		sendMoveMessage(playerID, move.getShiftCard(), move.getShiftPosition(), move.getMovePosition());
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

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}

	public ArrayList<TreasureType> getTreasuresFound() {
		return treasuresFound;
	}

	public void setTreasuresFound(ArrayList<TreasureType> treasuresFound) {
		this.treasuresFound = treasuresFound;
	}

	public ArrayList<TreasuresToGoType> getTreasuresToGo() {
		return treasuresToGo;
	}

	public void setTreasuresToGo(ArrayList<TreasuresToGoType> treasuresToGo) {
		this.treasuresToGo = treasuresToGo;
	}
	
	public TreasureType getTreasure() {
		return treasure;
	}
	
	public void setTreasure(TreasureType treasure) {
		this.treasure = treasure;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

}
