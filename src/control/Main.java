package control;

import network.Connection;

public class Main {	
	private void run() {
		Connection connection = new Connection();
		connection.establishConnection("localhost", 5123);
	}
	
	public static void main(String[] args) {
		Main m = new Main();
		m.run();
	}
}
