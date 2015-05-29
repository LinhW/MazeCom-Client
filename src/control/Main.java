package control;

import gui.view.AISelector;

import javax.swing.JOptionPane;

import network.Connection;
import ai.Player;
import ai.RandomAI;
import ai.ava.Ava;

public class Main {
	private void run() {
		int selection = JOptionPane.showConfirmDialog(null, "Do you want to start an artificial intelligence?", "AI selection", JOptionPane.YES_NO_OPTION);
		Connection connection = new Connection();
		Player player;
		if (selection == JOptionPane.YES_OPTION) {
			AISelector selector = new AISelector();
			selection = selector.showDialog();
			switch (selection) {
			case -1:
				JOptionPane.showMessageDialog(null, "Client will exit now.", "Exit", JOptionPane.INFORMATION_MESSAGE);
				return;
			case 0:
				player = new RandomAI(connection);
				break;
			case 1:
				player = new Ava(connection);
				break;
			default:
				player = new RandomAI(connection);
			}
		} else {
			System.out.println("start");
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
