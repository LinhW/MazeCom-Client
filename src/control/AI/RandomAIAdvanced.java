package control.AI;

import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import control.network.Connection;
import model.Board;
import model.Position;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.MoveMessageType;
import model.jaxb.PositionType;
import model.jaxb.TreasureType;
import model.jaxb.WinMessageType;

public class RandomAIAdvanced implements Player {
	private int player_id;
	private Connection connection;
	private Board board;
	private Random random;

	public RandomAIAdvanced(Connection connection) {
		this.connection = connection;
		random = new Random();
	}

	@Override
	public String login() {
		String name = JOptionPane.showInputDialog("Nickname");
		return name + " (Random AI Advanced)";
	}

	private void calculateMove(Board b, TreasureType treasure) {
		CardType shift = Util.getShiftCard(b);
		MoveMessageType move = new MoveMessageType();
		Position shiftPos = new Position();
		List<PositionType> list;
		Position pinPos = new Position();
		PositionType target;
		PositionType f = b.getForbidden();
		if (f == null) {
			f = new PositionType();
			f.setCol(-1);
			f.setRow(-1);
		}
		int c = f.getCol();
		int r = f.getRow();
		while (c == f.getCol() && r == f.getRow()) {
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
		shiftPos.setCol(c);
		shiftPos.setRow(r);
		move.setShiftPosition(shiftPos);
		move.setShiftCard(shift);
		b.proceedShift(move);
		target = b.findTreasure(treasure);
		list = b.getAllReachablePositions(b.findPlayer(player_id));
		pinPos = new Position(list.get(random.nextInt(list.size())));
		if (target != null) {
			for (PositionType t : list) {
				if (new Position(t).equals(target)) {
					pinPos = new Position(target);
					break;
				}
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
		board = new Board(message.getBoard());
		calculateMove(board, message.getTreasure());
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		JOptionPane.showMessageDialog(null, "You have been disconnected", "Disconnected", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		JOptionPane.showMessageDialog(null, "I have won!", "WIN!", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		System.out.println(message.getErrorCode().value());
		switch (message.getErrorCode()) {
		case NOERROR:
			break;
		case ERROR:
			JOptionPane.showMessageDialog(null, "Common error!", "Error!", JOptionPane.ERROR_MESSAGE);
			break;
		case ILLEGAL_MOVE:
			break;
		case TIMEOUT:
			JOptionPane.showMessageDialog(null, "Timeout by client!", "Timeout!", JOptionPane.ERROR_MESSAGE);
			break;
		case TOO_MANY_TRIES:
			JOptionPane.showMessageDialog(null, "Too many tries!", "Stupid AI!", JOptionPane.ERROR_MESSAGE);
			break;
		default:
			JOptionPane.showMessageDialog(null, "WTF happened?", "WTF?", JOptionPane.ERROR_MESSAGE);
			break;
		}
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
