package view.testClasses;

import jaxb.AcceptMessageType;
import jaxb.AwaitMoveMessageType;
import jaxb.BoardType;
import jaxb.DisconnectMessageType;
import jaxb.LoginMessageType;
import jaxb.LoginReplyMessageType;
import jaxb.MazeCom;
import jaxb.MoveMessageType;
import jaxb.TreasureType;
import jaxb.TreasuresToGoType;
import jaxb.WinMessageType;
import tools.Debug;
import tools.DebugLevel;
import view.data.Context;
import view.data.PersData;
import view.testClasses.userInterface.UI;
import config.Settings;
import javax.swing.JOptionPane;

public class Tmp_testGUI {

	private UI gui;

	public static void receiveServerMessage(MazeCom message) {
		Tmp_testGUI ttgui = new Tmp_testGUI();
		ttgui.init();
		System.out.println("receive Message: " + message.getId() + " " + message.getMcType());
		switch (message.getMcType()) {
		case ACCEPT:
			AcceptMessageType accept = message.getAcceptMessage();
			System.out.println(accept + " " + accept.isAccept());
			break;
		case AWAITMOVE:
			AwaitMoveMessageType await = message.getAwaitMoveMessage();
			TreasureType t = await.getTreasure();
			String n = t.name();
			System.out.println("----");
			System.out.println(await + " " + await.getBoard().getShiftCard() + " " + n);
			Context.getInstance().setValue(Context.BOARD, await.getBoard());
			for (TreasuresToGoType tt : await.getTreasuresToGo()) {
				System.out.println(tt);
			}
			((PersData) Context.getInstance().getValue(Context.USER)).setCurrentTreasure(await.getTreasure());
			((PersData) Context.getInstance().getValue(Context.USER)).setTreasuresToFind(await.getTreasuresToGo().size());
			ttgui.gui.init((Board) await.getBoard());
			System.out.println("----");
			break;
		case DISCONNECT:
			DisconnectMessageType disconnect = message.getDisconnectMessage();
			System.out.println(disconnect + " " + disconnect.getName());
			break;
		case LOGIN:
			LoginMessageType login = message.getLoginMessage();
			System.out.println(login + " " + login.getName());
			break;
		case LOGINREPLY:
			LoginReplyMessageType lr = message.getLoginReplyMessage();
			((PersData) Context.getInstance().getValue(Context.USER)).setID(lr.getNewID());
			System.out.println(lr + " " + lr.getNewID());
			break;
		case MOVE:
			MoveMessageType move = message.getMoveMessage();
			System.out.println(move + " " + move.getShiftCard() + " " + move.getShiftPosition() + " " + move.getNewPinPos());
			ttgui.gui.displayMove(move, (Board) Context.getInstance().getValue(Context.BOARD), Settings.MOVEDELAY, Settings.SHIFTDELAY);
			break;
		case WIN:
			WinMessageType win = message.getWinMessage();
			System.out.println(win + " " + win.getWinner() + " " + win.getWinner().getId());
			JOptionPane.showConfirmDialog(null, win.getWinner() + " hat gewonnen", "Spielende", JOptionPane.OK_OPTION);
			break;
		}

	}

	private void init() {
		Debug.addDebugger(System.out, Settings.DEBUGLEVEL);
		Debug.print(Messages.getInstance().getString("Game.Constructor"), DebugLevel.DEBUG); //$NON-NLS-1$
		gui = Settings.USERINTERFACE;
	}

	public static String first() {
		String name = JOptionPane.showInputDialog("Nickname");
		Context.getInstance().setValue(Context.USER, new PersData(name));
		return name;
	}
}
