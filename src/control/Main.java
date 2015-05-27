package control;

import javax.swing.JOptionPane;

import network.Connection;

public class Main {	
	private void run() {
		JOptionPane.showConfirmDialog(null, "Do you want to start an artificial intelligence",
				"AI selection", JOptionPane.YES_NO_OPTION);
		Connection connection = new Connection();
		Player player;
		connection.establishConnection("localhost", 5123);
	}
	
	public static void main(String[] args) {
		Main m = new Main();
		m.run();
	}
}
