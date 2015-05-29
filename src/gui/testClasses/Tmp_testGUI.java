package gui.testClasses;

import gui.data.Board;
import gui.data.Card;
import gui.data.Context;
import jaxb.AcceptMessageType;
import jaxb.AwaitMoveMessageType;
import jaxb.DisconnectMessageType;
import jaxb.LoginMessageType;
import jaxb.LoginReplyMessageType;
import jaxb.MazeCom;
import jaxb.MoveMessageType;
import jaxb.TreasureType;
import jaxb.TreasuresToGoType;
import jaxb.WinMessageType;

public class Tmp_testGUI {

	public static void receiveServerMessage(MazeCom message) {
		System.out.println("___________________________________________________\n" + 
							"receive Message: " + message.getId() + " " + message.getMcType());
		switch (message.getMcType()) {
		case ACCEPT:
			AcceptMessageType accept = message.getAcceptMessage();
			System.out.println(accept + " " + accept.isAccept());
			break;
		case AWAITMOVE:
			AwaitMoveMessageType await = message.getAwaitMoveMessage();
			TreasureType t = await.getTreasure();
			String n = t.name();
			System.out.println("Treasure: " + t + " " + n);
			Card c = new Card(await.getBoard().getShiftCard());
			System.out.println(await + " " + await.getBoard().getShiftCard());
			System.out.println("Card: " + c.getShape() + " " + c.getOrientation().value() + " " + c.value());
			Context.getInstance().setValue(Context.BOARD, new Board(await.getBoard()));
			for (TreasuresToGoType tt : await.getTreasuresToGo()) {
				System.out.println("TreasToGo: " + tt.getPlayer() + " " + tt.getTreasures());
			}
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
			System.out.println(lr + " " + lr.getNewID());
			break;
		case MOVE:
			MoveMessageType move = message.getMoveMessage();
			System.out.println(move + " " + move.getShiftCard() + " " + move.getShiftPosition() + " " + move.getNewPinPos());
			break;
		case WIN:
			WinMessageType win = message.getWinMessage();
			System.out.println(win + " " + win.getWinner() + " " + win.getWinner().getId());
			break;
		}

	}
}
