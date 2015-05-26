package view;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import view.data.Context;
import view.data.GUIModel;
import view.testClasses.Board;
import view.testClasses.Card;
import view.testClasses.userInterface.GUI;

public class GUIController {
	private GUI gui;
	private GUIModel model;

	@SuppressWarnings("unchecked")
	public GUIController() {
		model = new GUIModel();

		Map<String, Integer> map = new HashMap<>();
		map.put(Context.ROTATE_LEFT, KeyEvent.VK_L);
		map.put(Context.ROTATE_RIGHT, KeyEvent.VK_R);
		map.put(Context.UP, KeyEvent.VK_UP);
		map.put(Context.DOWN, KeyEvent.VK_DOWN);
		map.put(Context.LEFT, KeyEvent.VK_LEFT);
		map.put(Context.RIGHT, KeyEvent.VK_RIGHT);
		Context.getInstance().setValue(Context.KEYEVENTS, map);
		model.setKeyEventMap((Map<String, Integer>) Context.getInstance().getValue(Context.KEYEVENTS));
	}

	public void start() {
		gui = new GUI(this, model);
		gui.setVisible(true);
	}

	public void update() {
		Card c = new Card(((Board) Context.getInstance().getValue(Context.BOARD)).getShiftCard());
		model.setCardOrientation(c.getOrientation().value());
		model.setCardType(c.getShape().toString());
		gui.update();
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

	public void onClose() {
		// TODO
	}

	public void rotated(int orientation) {
		model.setCardOrientation(orientation);
	}
}
