package control.AI;

import model.jaxb.AcceptMessageType;
import model.jaxb.AwaitMoveMessageType;
import model.jaxb.CardType;
import model.jaxb.DisconnectMessageType;
import model.jaxb.LoginReplyMessageType;
import model.jaxb.PositionType;
import model.jaxb.WinMessageType;

public interface Player {
	public String login();

	public void receiveLoginReply(LoginReplyMessageType message);

	public void receiveAwaitMoveMessage(AwaitMoveMessageType message);

	public void receiveDisconnectMessage(DisconnectMessageType message);

	public void receiveWinMessage(WinMessageType message);

	public void receiveAcceptMessage(AcceptMessageType message);

	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin);
	
}