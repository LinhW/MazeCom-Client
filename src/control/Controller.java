package control;

import network.Connection;

public class Controller {
	public static enum NotificationType {NOTIFY_WAIT, NOTIFY_PLACE, NOTIFY_MOVE,
										 NOTIFY_YOUR_MOVE, NOTIFY_ILLEGAL, NOTIFY_WIN,
										 NOTIFY_DISCONNECT, NOTIFY_UNKNOWN}
	
	private Connection connection;
	//private Window window;
	
	private String name;

	public Controller() {
		connection = new Connection(this);
	}
	
	public void createWindow() {
		
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void notifyWindow(NotificationType type) {
		// window.sendNotification(type);
	}
	
	public void sendPlacement(int x, int y) {
		
	}
	
	public void sendMove(int x, int y) {
		
	}
	
	public boolean connect(String host, int port) {
		if (name == null) {
			return false;
		}
		else if (!connection.isConnected()) {
			return connection.establishConnection(name, host, port);
		}
		else {
			return false;
		}
	}
}
