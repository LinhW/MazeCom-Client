package control.network;

import model.jaxb.CardType;
import model.jaxb.MazeCom;
import model.jaxb.MazeComType;
import model.jaxb.MoveMessageType;
import model.jaxb.ObjectFactory;
import model.jaxb.PositionType;

public class MazeComMessageFactory {

	static private final ObjectFactory of = new ObjectFactory();

	public MazeCom createMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		MazeCom mc = of.createMazeCom();
		mc.setMcType(MazeComType.MOVE);
		mc.setId(PlayerID);
		MoveMessageType m = of.createMoveMessageType();
		m.setShiftCard(c);
		m.setShiftPosition(shift);
		m.setNewPinPos(pin);
		mc.setMoveMessage(m);
		return mc;
	}

	MazeCom createLoginMessage(String string) {
		MazeCom mc = of.createMazeCom();
		mc.setMcType(MazeComType.LOGIN);
		mc.setId(-1);
		mc.setLoginMessage(of.createLoginMessageType());
		mc.getLoginMessage().setName(string);
		return mc;
	}
}
