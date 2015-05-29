package control.AI.labymann;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import control.AI.Player;
import control.network.Connection;
import model.Board;
import model.Card;
import model.Position;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.MoveMessageType;
import model.jaxb.TreasureType;
import model.jaxb.WinMessageType;

public class Labymann implements Player {
	private int player_id;
	private int player_count;
	private Connection connection;
	private ArrayList<TreasureType> treasuresToGo;
	
	public Labymann(Connection connection) {
		this.connection = connection;
		treasuresToGo = new ArrayList<TreasureType>();
		for (TreasureType t : TreasureType.values()) {
			treasuresToGo.add(t);
		}
		treasuresToGo.trimToSize();
	}
	
	@Override
	public String login() {
		String name = JOptionPane.showInputDialog("Nickname");
		return name + "(Labymann)";
	}
	
	private void calculateMove(Board board, TreasureType treasure) {
		/* -------------------- INITIALIZATION -------------------- */
		Card shiftCard = new Card(board.getShiftCard());
		Position shiftPos = new Position(board.getForbidden());
		Position oldPinPos = new Position(board.findPlayer(player_id));
		Position newPinPos = new Position(oldPinPos);
		/* --------------------- CALCULATIONS --------------------- */
		
		/* ------------------- CALCULATIONS END ------------------- */
		sendMoveMessage(player_id, shiftCard, shiftPos, newPinPos);
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		this.player_id = message.getNewID();
		System.out.println("Login successful.");
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		List<TreasureType> found = message.getFoundTreasures();
		for (TreasureType t : found) {
			treasuresToGo.remove(t);
		}
		this.player_count = message.getTreasuresToGo().size();
		calculateMove(new Board(message.getBoard()), message.getTreasure());
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
	public void receiveMoveMessage(MoveMessageType moveMessage) {
		System.out.println("MOVE");
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, Position shift, Position pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
