package control.AI.Fridolin;

import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
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
	private boolean accept;

	public Fridolin(Connection con) {
		this.con = con;
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
		PinPosHelp pph;
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
		sendMoveMessage(id, pph.getCardHelp().getCard(), pph.getCardHelp().getPos(), pph.getPinPos());

	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		System.out.println("Fridolin receives a disconnect Message:");
		System.out.println(message.getErrorCode());
		con.sendDisconnect(message.getErrorCode(), id);
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
	}

	@Override
	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		con.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
