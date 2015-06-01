package control.AI.ava;

import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.WinMessageType;
import control.AI.Player;
import control.AI.Util;
import control.AI.ava.Pathfinding.PinPosHelp;
import control.AI.ava.ownClasses.Board;
import control.AI.ava.ownClasses.Card;
import control.AI.ava.ownClasses.Position;
import control.network.Connection;

public class Ava implements Player {
	private Connection con;
	private int id;
	private WriteIntoFile wif;

	public Ava(Connection con) {
		this.con = con;
		wif = new WriteIntoFile(WriteIntoFile.FILEPATH);
		System.out.println(wif.clearFile());
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
		wif.write("AWAIT MOVE MESSAGES");
		Board b = new Board(message.getBoard());
		Pathfinding p = new Pathfinding(b, id);
		PinPosHelp pph = p.ava(b.getPinPos(id), b.getTreasurePos());
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
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		System.out.println("CardPos: " + shift);
		System.out.println("PinPos: " + pin);
		System.out.println(new Card(c));
		con.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
