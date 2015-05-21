package control;

import network.Connection;

public class Controller {
	private Connection connection;
	
	private String name;

	public Controller() {
		connection = new Connection();
	}
	
	public void setName(String name) {
		this.name = name;
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
