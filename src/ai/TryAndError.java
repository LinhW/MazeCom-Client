package ai;

import gui.data.Board;
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
import ai.ava.Pathfinding;
import ai.ava.Pathfinding.CardHelp;

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
		CardHelp ch = Pathfinding.calcMove(b, Util.getPinPos(b, PlayerID), t, PlayerID);
		if (ch == null) {
			random(b);
		} else {
			sendMessage(ch, Util.getTreasurePos(b, t));
		}

	}

	private void sendMessage(CardHelp ch, Position tPos) {
		sendMoveMessage(PlayerID, ch.getC(), ch.getP(), tPos);
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
