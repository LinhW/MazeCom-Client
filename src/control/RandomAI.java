package control;

import gui.data.Card;
import gui.data.Position;

import javax.swing.JOptionPane;

import jaxb.AcceptMessageType;
import jaxb.AwaitMoveMessageType;
import jaxb.DisconnectMessageType;
import jaxb.LoginReplyMessageType;
import jaxb.MoveMessageType;
import jaxb.WinMessageType;
import network.Connection;

public class RandomAI implements Player {
	private String name;
	private int player_id;
	private Connection connection;
	
	public RandomAI(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public String login() {
		String name = JOptionPane.showInputDialog("Nickname");
		this.name = name;
		return name;
	}

	@Override
	public void receiveLoginReply(LoginReplyMessageType message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveAwaitMoveMessage(AwaitMoveMessageType message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveDisconnectMessage(DisconnectMessageType message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveWinMessage(WinMessageType message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveAcceptMessage(AcceptMessageType message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveMoveMessage(MoveMessageType moveMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMoveMessage(int PlayerID, Card c, Position shift, Position pin) {
		// TODO Auto-generated method stub
		
	}

}
