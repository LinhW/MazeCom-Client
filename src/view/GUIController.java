package view;

import javax.swing.JOptionPane;

import view.testClasses.userInterface.UI;
import config.Settings;

public class GUIController {
	private UI gui;

	public GUIController() {
		gui = Settings.USERINTERFACE;
	}

	public void update() {

	}

	public void close() {
		// FIXME
		System.out.println("too close");
	}

	public void displayMove(boolean accept) {
		// TODO Auto-generated method stub

	}

	public void endGame(int player, String name) {
		JOptionPane.showMessageDialog(null, "Player " + player + " " + name + " wins!");
		close();
	}
}
