package control.AI.ava;

import model.Board;
import model.Card;
import model.Position;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.MoveMessageType;
import model.jaxb.WinMessageType;
import control.AI.Player;
import control.AI.Util;
import control.AI.ava.Pathfinding.PinPosHelp;
import control.network.Connection;

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
		Pathfinding p = new Pathfinding(b, id);
		PinPosHelp pph = p.ava(Util.getPinPos(b, id), Util.getTreasurePos(b, b.getTreasure()));
		sendMoveMessage(id, pph.getCardHelp().getC(), pph.getCardHelp().getP(), pph.getPinPos());
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		// TODO Auto-generated method stub
		System.out.println("Ava receives a disconnect Message:");
		System.out.println(message.getErrorCode());

	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		System.out.println(message);
	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		System.out.println("Ava receives an Accept message");
		System.out.println(message.getErrorCode());
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, Position shift, Position pin) {
		System.out.println("CardPos: " + shift);
		System.out.println("PinPos: " + pin);
		System.out.println(new Card(c));
		con.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
