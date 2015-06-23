package control.AI.Fridolin;

import tools.WriteIntoFile;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.ErrorType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.WinMessageType;
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
	private WriteIntoFile wif;

	public Fridolin(Connection con) {
		this.con = con;
		wif = new WriteIntoFile(FILEPATH + WriteIntoFile.FILEEXTENSION);
	}

	@Override
	public String login() {
		return "Fridolin";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		System.out.println("Fridolin recieve a login reply");
		this.id = message.getNewID();
		p = new Pathfinding(id);
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		wif.writeln("Fridolin receives AwaitMoveMessage");
		PinPosHelp pph;
		long tmp = System.nanoTime();
		if (accept) {
			Board b = new Board(message.getBoard());
			b.setTreasure(message.getTreasure());
			p.setBoard(b);
			p.setTreToGo(message.getTreasuresToGo());
			p.setFoundTreasures(message.getFoundTreasures());
			pph = p.start();
		} else {
			pph = p.getNewMove();
		}
		tmp = ((System.nanoTime() - tmp) / (1000 * 1000 * 1000));
		wif.writeln(tmp + "");
		if (tmp > 10) {
			System.err.println((System.nanoTime() - tmp) / (1000 * 1000 * 1000));
		}
		wif.writeln(id + "\n" + pph.getCardHelp().getCard() + " " + pph.getCardHelp().getPos() + pph.getPinPos());
		sendMoveMessage(id, pph.getCardHelp().getCard(), pph.getCardHelp().getPos(), pph.getPinPos());

	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		System.out.println("Fridolin receives a disconnect Message:");
		System.out.println(message.getErrorCode());
		con.sendDisconnect(message.getErrorCode(), id);
		if (message.getErrorCode() == ErrorType.TIMEOUT) {
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
		wif.writeln("Fridolin receives: " + message.isAccept());
		accept = message.isAccept();
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		if (!dc) {
			con.sendMoveMessage(PlayerID, c, shift, pin);
		}
	}

}
