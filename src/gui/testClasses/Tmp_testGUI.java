package gui.testClasses;

import model.Card;
import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.MazeCom;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;
import model.jaxb.WinMessageType;

public class Tmp_testGUI {

	public static void receiveServerMessage(MazeCom message) {
		System.out.println("___________________________________________________\n" + "receive Message: " + message.getId() + " " + message.getMcType());
		switch (message.getMcType()) {
		case ACCEPT:
			AcceptMessageType accept = message.getAcceptMessage();
			System.out.println(accept.getErrorCode() + " " + accept.isAccept());
			break;
		case AWAITMOVE:
			AwaitMoveMessageType await = message.getAwaitMoveMessage();
			TreasureType t = await.getTreasure();
			System.out.println("Treasure: " + t);
			Card c = new Card(await.getBoard().getShiftCard());
			System.out.println("Card: " + c.value());
			for (TreasuresToGoType tt : await.getTreasuresToGo()) {
				System.out.println("TreasToGo: " + tt.getPlayer() + " " + tt.getTreasures());
			}
			break;
		case DISCONNECT:
			DisconnectMessageType disconnect = message.getDisconnectMessage();
			System.out.println(disconnect + " " + disconnect.getErrorCode());
			break;
		case LOGIN:
			LoginMessageType login = message.getLoginMessage();
			System.out.println(login + " " + login.getName());
			break;
		case LOGINREPLY:
			LoginReplyMessageType lr = message.getLoginReplyMessage();
			System.out.println(lr + " " + lr.getNewID());
			break;
		case WIN:
			WinMessageType win = message.getWinMessage();
			System.out.println(win + " " + win.getWinner() + " " + win.getWinner().getId());
			break;
		default:
			System.out.println("other stuff");
			break;
		}
		System.out.println("___________________________________________________\n");

	}
}
