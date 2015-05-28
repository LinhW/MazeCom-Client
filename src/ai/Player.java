package ai;

import gui.data.Position;
import jaxb.AcceptMessageType;
import jaxb.AwaitMoveMessageType;
import jaxb.CardType;
import jaxb.DisconnectMessageType;
import jaxb.LoginReplyMessageType;
import jaxb.MoveMessageType;
import jaxb.WinMessageType;

public interface Player {
	public String login();

	public void receiveLoginReply(LoginReplyMessageType message);

	public void receiveAwaitMoveMessage(AwaitMoveMessageType message);

	public void receiveDisconnectMessage(DisconnectMessageType message);

	public void receiveWinMessage(WinMessageType message);

	public void receiveAcceptMessage(AcceptMessageType message);

	public void receiveMoveMessage(MoveMessageType moveMessage);

	public void sendMoveMessage(int PlayerID, CardType c, Position shift, Position pin);
}