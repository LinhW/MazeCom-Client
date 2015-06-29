package control;

import gui.view.AISelector;

import javax.swing.JOptionPane;

import control.AI.Player;
import control.AI.RandomAIAdvanced;
import control.AI.RandomAISimple;
import control.AI.TryAndError;
import control.AI.Fridolin.Fridolin;
import control.AI.LAMB.LAMB;
import control.AI.MNA.MNA;
import control.AI.MNA_S.MNA_S;
import control.AI.ava.Ava;
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
				player = new LAMB(connection);
				break;
			case 5:
				player = new Fridolin(connection);
				break;
			case 6:
				player = new MNA_S(connection);
				break;
			case 7:
				player = new MNA(connection);
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
