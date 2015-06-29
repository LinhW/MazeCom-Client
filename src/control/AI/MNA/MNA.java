package control.AI.MNA;

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
import control.network.Connection;

public class MNA implements Player {
	private int playerID;
	private Connection connection;
	private MNA_Logic logic;
	private MNA_Move lastMove;
	private TreasureType treasure;
	private Board board;
	private ArrayList<TreasureType> treasuresFound;
	private ArrayList<TreasuresToGoType> treasuresToGo;
	
	@Override
	public String login() {
		return "MazeNet Alpha";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		playerID = message.getNewID();
		logic = new MNA_Logic(this);
		System.out.println("MazeNet Alpha logged in with ID " + playerID);
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		treasure = message.getTreasure();
		board = new Board(message.getBoard());
		treasuresFound = new ArrayList<TreasureType>(message.getFoundTreasures());
		treasuresToGo = new ArrayList<TreasuresToGoType>(message.getTreasuresToGo());
		
		MNA_Move finalMove = logic.getMove();
		lastMove = new MNA_Move(finalMove);
		sendMoveMessage(playerID,
				finalMove.getShiftCard(),
				finalMove.getShiftPosition(),
				finalMove.getMovePosition());
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		System.out.println("I have been disconnected! Reason: " + message.getErrorCode().name());
		connection.sendDisconnect(message.getErrorCode(), playerID);
	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		if (message.getWinner().getId() == playerID) {
			System.out.println("I HAVE WON!!!");
			connection.sendWin(message.getWinner());
		}
		else {
			System.out.println("Player " + message.getWinner().getValue() + " (" +
					message.getWinner().getId() + ") has won the game!");
		}
	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		if (message.getErrorCode() != ErrorType.NOERROR) {
			System.out.println(message.getErrorCode().value());
			System.out.println("LAST MOVE:\nCARD: " + lastMove.getShiftCard().getShape().name() + " "
					+ lastMove.getShiftCard().getOrientation().name() + "\nPOS: "
					+ new Position(lastMove.getShiftPosition()) + "\nMOVE: "
					+ new Position(lastMove.getMovePosition()));
		}
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
	}
	
	public MNA(Connection connection) {
		this.connection = connection;
	}

	public int getPlayerID() {
		return playerID;
	}

	public TreasureType getTreasure() {
		return treasure;
	}

	public Board getBoard() {
		return board;
	}

	public ArrayList<TreasureType> getTreasuresFound() {
		return treasuresFound;
	}

	public ArrayList<TreasuresToGoType> getTreasuresToGo() {
		return treasuresToGo;
	}

}
