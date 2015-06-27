package control.AI.Fridolin;

import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.ErrorType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.WinMessageType;
import tools.WriteIntoFile;
import control.AI.Player;
import control.AI.Fridolin.ownClasses.Board;
import control.AI.Fridolin.ownClasses.PinPosHelp;
import control.network.Connection;

public class Fridolin implements Player {
	private int id;
	private Connection con;
	private Pathfinding p;
	private boolean accept = true;
	private boolean dc = false;
	public static final String FILEPATH = "src/control/AI/Fridolin/tmp";
	public WriteIntoFile wif;

	public Fridolin(Connection con) {
		this.con = con;
		wif = new WriteIntoFile(FILEPATH + "_send" + WriteIntoFile.FILEEXTENSION);
	}

	@Override
	public String login() {
		return "Fridolin";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		this.id = message.getNewID();
		p = new Pathfinding(id);
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		PinPosHelp pph;
		if (accept) {
			Board b = new Board(message.getBoard());
			b.setTreasure(message.getTreasure());
			p.setBoard(b);
			wif.writeln(b.getForbidden() + "");
			p.setTreToGo(message.getTreasuresToGo());
			p.setFoundTreasures(message.getFoundTreasures());
			pph = p.start();
		} else {
			wif.writeln("getNewMove");
			pph = p.getNewMove();
		}
		wif.writeln(pph + "");
		sendMoveMessage(id, pph.getCardHelp().getCard(), pph.getCardHelp().getPos(), pph.getPinPos());
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		con.sendDisconnect(message.getErrorCode(), id);
		if (message.getErrorCode().equals(ErrorType.TIMEOUT) || message.getErrorCode().equals(ErrorType.TOO_MANY_TRIES)) {
			dc = true;
		} else {
			dc = false;
		}
	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		if (message.getWinner().getId() == id) {
			con.sendWin(message.getWinner());
		}
	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		accept = message.isAccept();
		wif.writeln(accept + "");
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		if (!dc) {
			con.sendMoveMessage(PlayerID, c, shift, pin);
		}
	}
}
