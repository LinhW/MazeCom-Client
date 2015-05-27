package control;

import gui.GUIController;
import gui.data.Board;
import gui.data.Card;
import gui.data.Context;
import gui.data.Messages;
import gui.data.PersData;
import gui.data.Position;

import javax.swing.JOptionPane;

import jaxb.AcceptMessageType;
import jaxb.AwaitMoveMessageType;
import jaxb.DisconnectMessageType;
import jaxb.LoginReplyMessageType;
import jaxb.MoveMessageType;
import jaxb.WinMessageType;
import network.Connection;
import network.MazeComMessageFactory;
import tools.Debug;
import tools.DebugLevel;
import config.Settings;

public class EventController implements Player {
	private GUIController ctrl_gui;
	private Connection connection;
	private int count = 0;
	private MoveMessageType move;

	public EventController(Connection connection) {
		this.connection = connection;
		ctrl_gui = new GUIController(this);
		Debug.addDebugger(System.out, Settings.DEBUGLEVEL);
		Debug.print(Messages.getInstance().getString("Game.Constructor"), DebugLevel.DEBUG); //$NON-NLS-1$
	}

	/**
	 * shows a dialog with a textfield, return: input
	 * 
	 * @return nickname
	 */
	public String login() {
		String name = JOptionPane.showInputDialog("Nickname");
		Context.getInstance().setValue(Context.USER, new PersData(name));
		return name;
	}

	public void receiveLoginReply(LoginReplyMessageType message) {
		JOptionPane.showMessageDialog(null, "Warten auf Mitspieler");
		((PersData) Context.getInstance().getValue(Context.USER)).setID(message.getNewID());
	}

	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		if (count == 0) {
			ctrl_gui.start();
			count++;
		}
		Context.getInstance().setValue(Context.BOARD, new Board(message.getBoard()));
		((PersData) Context.getInstance().getValue(Context.USER)).setCurrentTreasure(message.getTreasure());
		// TODO TreasureToFind != noch zu findenden Schaetze?
		Context.getInstance().setValue(Context.TREASURELIST, message.getTreasuresToGo());
		ctrl_gui.update();
	}

	public void receiveDisconnectMessage(DisconnectMessageType message) {
		ctrl_gui.close();
	}

	public void receiveWinMessage(WinMessageType message) {
		ctrl_gui.endGame(message.getWinner().getId(), message.getWinner().getValue());
	}

	public void receiveAcceptMessage(AcceptMessageType message) {
		ctrl_gui.displayMove(message.isAccept(), move);
	}

	public void receiveMoveMessage(MoveMessageType moveMessage) {
		// TODO Auto-generated method stub
		System.out.println("MOVE");
	}

	public void sendMoveMessage(int PlayerID, Card c, Position shift, Position pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
		move = new MazeComMessageFactory().createMoveMessage(PlayerID, c, shift, pin).getMoveMessage();
	}

}
