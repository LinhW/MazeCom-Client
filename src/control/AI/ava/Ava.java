package control.AI.ava;

import control.AI.Player;
import control.network.Connection;
import model.Board;
import model.Position;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.MoveMessageType;
import model.jaxb.WinMessageType;

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
//		Pathfinding.findPath(b, new Position(b.findPlayer(id)), new Position(b.findTreasure(b.getTreasure())));
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
