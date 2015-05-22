package network;

import model.Board;
import jaxb.ErrorType;
import jaxb.MazeCom;
import jaxb.MazeComType;
import jaxb.ObjectFactory;
import jaxb.WinMessageType.Winner;

public class MazeComMessageFactory {

	static private ObjectFactory of = new ObjectFactory();

	public MazeCom createLoginReplyMessage(int newID) {
		MazeCom mc = of.createMazeCom();
		mc.setMcType(MazeComType.LOGINREPLY);
		mc.setId(newID);
		mc.setLoginReplyMessage(of.createLoginReplyMessageType());
		mc.getLoginReplyMessage().setNewID(newID);
		return mc;
	}

	public MazeCom createAcceptMessage(int playerID, ErrorType et) {
		MazeCom mc = of.createMazeCom();
		mc.setMcType(MazeComType.ACCEPT);
		mc.setId(playerID);
		mc.setAcceptMessage(of.createAcceptMessageType());
		mc.getAcceptMessage().setErrorCode(et);
		mc.getAcceptMessage().setAccept(et == ErrorType.NOERROR);
		return mc;
	}

	public MazeCom createWinMessage(int playerID, int winnerId, String name,
			Board b) {
		MazeCom mc = of.createMazeCom();
		mc.setMcType(MazeComType.WIN);
		mc.setId(playerID);
		mc.setWinMessage(of.createWinMessageType());
		Winner w = of.createWinMessageTypeWinner();
		w.setId(winnerId);
		w.setValue(name);
		mc.getWinMessage().setWinner(w);
		mc.getWinMessage().setBoard(b);
		return mc;
	}

	public MazeCom createDisconnectMessage(int playerID, String name, ErrorType et) {
		MazeCom mc = of.createMazeCom();
		mc.setMcType(MazeComType.DISCONNECT);
		mc.setId(playerID);
		mc.setDisconnectMessage(of.createDisconnectMessageType());
		mc.getDisconnectMessage().setErroCode(et);
		mc.getDisconnectMessage().setName(name);
		return mc;
	}

	public MazeCom createLoginMessage(String string) {
		MazeCom mc = of.createMazeCom();
		mc.setMcType(MazeComType.LOGIN);
		mc.setId(-1);
		mc.setLoginMessage(of.createLoginMessageType());
		mc.getLoginMessage().setName(string);
		return mc;
	}
}
