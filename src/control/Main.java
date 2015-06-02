package control;

import gui.view.AISelector;

import javax.swing.JOptionPane;

import control.AI.Player;
import control.AI.RandomAIAdvanced;
import control.AI.RandomAISimple;
import control.AI.TryAndError;
import control.AI.ava.Ava;
import control.AI.labymann.Labymann;
import control.network.Connection;

public class Main {
	public static void main(String[] args) {
		int selection = JOptionPane.showConfirmDialog(null, "Do you want to start an artificial intelligence?", "AI selection", JOptionPane.YES_NO_OPTION);
		Connection connection = new Connection();
		Player player;
		if (selection == JOptionPane.YES_OPTION) {
			AISelector selector = new AISelector();
			selection = selector.showDialog();
			System.out.println(selection);
			switch (selection) {
			case -1:
				JOptionPane.showMessageDialog(null, "Client will exit now.", "Exit", JOptionPane.INFORMATION_MESSAGE);
				return;
			case 0:
				player = new RandomAISimple(connection);
				break;
			case 1:
				player = new Ava(connection);
				break;
			case 2:
				player = new RandomAIAdvanced(connection);
				break;
			case 3:
				player = new TryAndError(connection);
				break;
			case 4:
				player = new Labymann(connection);
				break;
			default:
				player = new RandomAISimple(connection);
			}
		} else {
			System.out.println("start");
			player = new EventController(connection);
		}
		connection.setPlayer(player);
		connection.establishConnection("localhost", Settings.PORT);
	}
}
