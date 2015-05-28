package ai.ava;

import gui.data.Board;
import gui.data.Position;
import jaxb.AcceptMessageType;
import jaxb.AwaitMoveMessageType;
import jaxb.CardType;
import jaxb.DisconnectMessageType;
import jaxb.LoginReplyMessageType;
import jaxb.MoveMessageType;
import jaxb.WinMessageType;
import network.Connection;
import ai.Player;

public class Ava implements Player {
	private Connection con;
	private int id;

	public Ava(Connection con) {
		this.con = con;
	}

	public Ava() {
		login();
	}

	@Override
	public String login() {
		return "Ava";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		System.out.println("Ava receives a login reply");
		id = message.getNewID();
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		System.out.println("Ava receives an await move message");
		Board b = new Board(message.getBoard());
		Pathfinding.findPath(b, new Position(b.findPlayer(id)), new Position(b.findTreasure(b.getTreasure())));
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
