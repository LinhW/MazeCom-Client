package ai;

import gui.data.Board;
import gui.data.Card;
import gui.data.Position;

import java.util.List;
import java.util.Random;

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
		List<PositionType> l = b.getAllReachablePositions(Util.getPinPos(b, PlayerID));
		Position posT = Util.getTreasurePos(b, t);
		Position pinPos = Util.containsInList(posT, l);
		Card shift = Util.getShiftCard(b);
		if (pinPos == null) {
			for (int i = 1; i < 6; i += 2) {
				for (int j = 0; j < 4; j++) {
				}
			}
		} else {
			int count = 0;
			while (Util.containsInList(posT, l) == null && count < 12) {

				count++;
			}
		}

	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

}
