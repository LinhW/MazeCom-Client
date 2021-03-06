package control.AI.ava;

import tools.WriteIntoFile;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.WinMessageType;
import control.AI.Player;
import control.AI.ava.Pathfinding.PinPosHelp;
import control.AI.ava.ownClasses.Board;
import control.network.Connection;

public class Ava2 implements Player {
	private Connection con;
	private int id;
	private WriteIntoFile wif;
	private WriteIntoFile wif_v2;
	private Pathfinding p;
	public static final String FILEPATH = "src/control/AI/ava/tmp";

	public Ava2(Connection con) {
		this.con = con;
		wif = new WriteIntoFile(FILEPATH + WriteIntoFile.FILEEXTENSION);
		wif_v2 = new WriteIntoFile(FILEPATH + "_v2" + WriteIntoFile.FILEEXTENSION);
		wif.writeln("Ava");
		wif_v2.writeln("Ava");
	}

	public Ava2() {
		login();
	}

	@Override
	public String login() {
		return "Ava2";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		System.out.println("Ava receives a login reply");
		id = message.getNewID();
		p = new Pathfinding(id);
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		wif.writeln("AWAIT MOVE MESSAGES");
		wif_v2.writeln("AWAIT MOVE MESSAGES");
		Board b = new Board(message.getBoard());
		b.setTreasure(message.getTreasure());
		p.setBoard(b);
		p.setTreToGo(message.getTreasuresToGo());
		p.setFoundTreasures(message.getFoundTreasures());
		PinPosHelp pph = p.start();
		wif.writeln(b.getShiftCard().toString());
		wif.writeln(pph.getCardHelp().getC().toString());
		sendMoveMessage(id, pph.getCardHelp().getC(), pph.getCardHelp().getP(), pph.getPinPos());
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		System.out.println("Ava receives a disconnect Message:");
		System.out.println(message.getErrorCode());
		con.sendDisconnect(message.getErrorCode(), id);
		wif.writeln(message.getErrorCode().toString());
		wif_v2.writeln(message.getErrorCode().toString());
	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		if (message.getWinner().getId() == id) {
			con.sendWin(message.getWinner());
		}
	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		wif.writeln(message.getErrorCode().toString());
		wif_v2.writeln(message.getErrorCode().toString());
		wif.writeNewLine(2);
		wif_v2.writeNewLine(2);
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		con.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
