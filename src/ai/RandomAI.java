package ai;

import gui.data.Position;

import java.util.Random;

import javax.swing.JOptionPane;

import jaxb.AcceptMessageType;
import jaxb.AwaitMoveMessageType;
import jaxb.BoardType;
import jaxb.CardType;
import jaxb.DisconnectMessageType;
import jaxb.LoginReplyMessageType;
import jaxb.MoveMessageType;
import jaxb.WinMessageType;
import jaxb.ErrorType;
import network.Connection;

public class RandomAI implements Player {
	private String name;
	private int player_id;
	private Connection connection;
	private BoardType board;
	private Random random;
	
	public RandomAI(Connection connection) {
		this.connection = connection;
		random = new Random();
	}
	
	@Override
	public String login() {
		String name = JOptionPane.showInputDialog("Nickname");
		this.name = name;
		return name;
	}
	
	private void calculateMove(BoardType b) {
		CardType shift = b.getShiftCard();
		Position shiftPos = new Position();
		Position pinPos = new Position();
		int c = b.getForbidden().getCol();
		int r = b.getForbidden().getRow();
		while (c == b.getForbidden().getCol() && r == b.getForbidden().getRow()) {
			switch (random.nextInt(4)) {
			case 0:
				c = 0;
				r = random.nextInt(3) * 2 + 1;
				break;
			case 1:
				c = 6;
				r = random.nextInt(3) * 2 + 1;
				break;
			case 2:
				r = 0;
				c = random.nextInt(3) * 2 + 1;
				break;
			case 3:
				r = 6;
				c = random.nextInt(3) * 2 + 1;
				break;
			default:
				System.out.println("RANDOM ERROR");
				break;
			}
		}
		sendMoveMessage(player_id, shift, shiftPos, pinPos);
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		player_id = message.getNewID();
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		calculateMove(message.getBoard());
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		JOptionPane.showMessageDialog(null, name + " has been disconnected", "Disconnected", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		JOptionPane.showMessageDialog(null, "`-.~*^°^*~.-´", "WIN!", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		System.out.println(message.getErrorCode().value());
		switch (message.getErrorCode()) {
		case NOERROR:
			break;
		case ERROR:
			break;
		case ILLEGAL_MOVE:
			calculateMove(board);
			break;
		case TIMEOUT:
			break;
		case TOO_MANY_TRIES:
			break;
		default:
			break;
		}
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
