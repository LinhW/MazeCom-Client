package ai;

import gui.data.Board;
import gui.data.Card;
import gui.data.Position;

import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import jaxb.AcceptMessageType;
import jaxb.AwaitMoveMessageType;
import jaxb.CardType;
import jaxb.DisconnectMessageType;
import jaxb.LoginReplyMessageType;
import jaxb.MoveMessageType;
import jaxb.PositionType;
import jaxb.TreasureType;
import jaxb.WinMessageType;
import network.Connection;

public class TryAndError implements Player {
	private Connection con;
	private int PlayerID;
	private Random random;

	public TryAndError(Connection con) {
		this.con = con;
	}

	@Override
	public String login() {
		return "Doofus";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		this.PlayerID = message.getNewID();
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		calcMove(Util.getBoard(message), message.getTreasure());
	}

	private void calcMove(Board b, TreasureType t) {
		Board board = b;
		Position oldPinPos = Util.getPinPos(b, PlayerID);
		List<PositionType> l = b.getAllReachablePositions(oldPinPos);
		Position pt = Util.getTreasurePos(b, t);
		Card shift = Util.getShiftCard(b);
		Position shiftPos;
		MoveMessageType message = new MoveMessageType();
		for (int i = 1; i < 6; i += 2) {
			for (int j = 0; j < 4; j++) {
				Card c = Util.rotateCard(shift, j * 90);
				for (int k = 0; k < 7; k += 6) {
					b = board;
					b.setShiftCard(c);
					shiftPos = new Position(k, i);
					message.setShiftPosition(shiftPos);
					message.setShiftCard(shift);
					b.proceedShift(message);
					l = b.getAllReachablePositions(oldPinPos);
					if (Util.containsInList(pt, l) != null) {
						sendMoveMessage(PlayerID, c, shiftPos, pt);
						return;
					}
				}
			}
		}

		random(board);

	}

	private void random(Board b) {
		CardType shift = Util.getShiftCard(b);
		MoveMessageType move = new MoveMessageType();
		Position shiftPos = new Position();
		List<PositionType> list;
		Position pinPos = new Position();
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
		list = b.getAllReachablePositions(b.findPlayer(PlayerID));
		pinPos = new Position(list.get(random.nextInt(list.size())));
		sendMoveMessage(PlayerID, shift, shiftPos, pinPos);
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(null, "Ende");

	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveMoveMessage(MoveMessageType moveMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, Position shift, Position pin) {
		con.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
