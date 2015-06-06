package control;

import gui.Context;
import gui.GUIController;

import javax.swing.JOptionPane;

import model.Board;
import model.Messages;
import model.PersData;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.WinMessageType;
import tools.Debug;
import tools.DebugLevel;
import control.AI.Player;
import control.network.Connection;

public class EventController implements Player {
	private final GUIController ctrl_gui;
	private final Connection connection;
	private int count = 0;

	EventController(Connection connection) {
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
		((PersData) Context.getInstance().getValue(Context.USER)).setCurrentTreasure(message.getTreasure());
		Context.getInstance().setValue(Context.TREASURELIST, message.getTreasuresToGo());
		ctrl_gui.update(new Board(message.getBoard()));
	}

	public void receiveDisconnectMessage(DisconnectMessageType message) {
		ctrl_gui.close();
	}

	public void receiveWinMessage(WinMessageType message) {
		ctrl_gui.endGame(message.getWinner().getId(), message.getWinner().getValue());
	}

	public void receiveAcceptMessage(AcceptMessageType message) {
		System.out.println(message.getErrorCode());
		switch (message.getErrorCode()) {
		case ILLEGAL_MOVE:
			JOptionPane.showMessageDialog(null, "Illegal Move. Try again");
			break;
		case TOO_MANY_TRIES:
			JOptionPane.showMessageDialog(null, "To many Tries. Bye Bye.");
			break;
		case TIMEOUT:
			JOptionPane.showMessageDialog(null, "Timeout");
			break;
		default:
			;
		}
	}

	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
	}

}
