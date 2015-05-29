package control;

import gui.Context;
import gui.GUIController;

import javax.swing.JOptionPane;

import control.AI.Player;
import control.network.Connection;
import control.network.MazeComMessageFactory;
import model.Board;
import model.Messages;
import model.PersData;
import model.Position;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.MoveMessageType;
import model.jaxb.WinMessageType;
import tools.Debug;
import tools.DebugLevel;

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

	public void sendMoveMessage(int PlayerID, CardType c, Position shift, Position pin) {
		connection.sendMoveMessage(PlayerID, c, shift, pin);
		move = new MazeComMessageFactory().createMoveMessage(PlayerID, c, shift, pin).getMoveMessage();
	}

}
