package control;

import gui.view.AISelector;

import javax.swing.JOptionPane;

import network.Connection;

public class Main {	
	private void run() {
		int selection = JOptionPane.showConfirmDialog(null, "Do you want to start an artificial intelligence?",
				"AI selection", JOptionPane.YES_NO_OPTION);
		Connection connection = new Connection();
		Player player;
		if (selection == JOptionPane.YES_OPTION) {
			AISelector selector = new AISelector();
			selection = selector.showDialog();
			player = new RandomAI(connection);
		}
		else {
			player = new EventController(connection);
		}
		connection.setPlayer(player);
		connection.establishConnection("localhost", 5123);
	}
	
	public static void main(String[] args) {
		Main m = new Main();
		m.run();
	}
}
