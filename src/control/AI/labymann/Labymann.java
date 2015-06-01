package control.AI.labymann;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import model.Board;
import model.Card;
import model.Position;
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

public class Labymann implements Player {
	private int player_id;
	private final Connection connection;
	private final ArrayList<TreasureType> treasuresToGo;
	
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
		message.getTreasuresToGo().size();
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
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
