package ai.ava;

import gui.data.Board;
import gui.data.Card;
import gui.data.Context;
import gui.data.PersData;
import gui.data.Position;
import jaxb.AcceptMessageType;
import jaxb.AwaitMoveMessageType;
import jaxb.DisconnectMessageType;
import jaxb.LoginReplyMessageType;
import jaxb.MoveMessageType;
import jaxb.WinMessageType;
import network.Connection;
import ai.Player;

public class Ava implements Player {
	private Connection con;

	public Ava(Connection con) {
		this.con = con;
	}

	@Override
	public String login() {
		return "Ava";
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		((PersData) Context.getInstance().getValue(Context.USER)).setID(message.getNewID());
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		Board b = new Board(message.getBoard());
		int[][] weg = Pathfinding.findShortestPath(b, new Position(b.findPlayer(((PersData) Context.getInstance().getValue(Context.USER)).getID())),
				new Position(b.findTreasure(b.getTreasure())));
		for (int i = 0; i < weg.length; i++) {
			for (int j = 0; j < weg[0].length; i++) {
				System.out.println(weg[i][j]);
			}
			System.out.println();
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
	public void sendMoveMessage(int PlayerID, Card c, Position shift, Position pin) {
		// TODO Auto-generated method stub

	}

}
