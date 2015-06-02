package gui;

import gui.view.GUI;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import model.Board;
import model.Card;
import model.PersData;
import model.Position;
import control.EventController;

public class GUIController {
	private GUI gui;
	private GUIModel model;
	private EventController ctrl_event;
	private Board b;

	@SuppressWarnings("unchecked")
	public GUIController(EventController ctrl_event) {
		this.ctrl_event = ctrl_event;
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
		model.setRow(1);
		model.setCol(0);
	}

	public void start() {
		model.setPlayerID(((PersData) Context.getInstance().getValue(Context.USER)).getID());
		gui = new GUI(this, model);
		gui.setVisible(true);
	}

	public void update(Board b) {
		this.b = b;
		Card c = new Card(b.getShiftCard());
		model.setShiftCard(c);
		System.out.println("shiftcard gesendet");
		model.setCardTreasure(c.getTreasure());
		model.setPinPos(new Position(b.findPlayer(((PersData) Context.getInstance().getValue(Context.USER)).getID())));
		gui.update(b);
	}

	public void close() {
		// FIXME
		System.out.println("too close");
	}

	public void endGame(int player, String name) {
		JOptionPane.showMessageDialog(null, "Player " + player + " " + name + " wins!");
		close();
	}

	public void onClose() {
		// TODO
	}

	public void sendMove() {
		Card c = new Card(model.getCardShape(), model.getCardOrientation(), model.getCardTreasure());
		Position shift = new Position(model.getRow(), model.getCol());
		Position pin = model.getPinPos();
		ctrl_event.sendMoveMessage(model.getPlayerID(), c, shift, pin);
	}

	public void proceedShift() {
		gui.proceedShift(b);
	}

	public void moveTop() {
		if (b.getCard(model.getPinPos().getRow(), model.getPinPos().getCol()).getOpenings().isTop()
				&& b.getCard(model.getPinPos().getRow() - 1, model.getPinPos().getCol()).getOpenings().isBottom()) {
			Position p = new Position(b.findPlayer(model.getPlayerID()));
			p.setRow(p.getRow() - 1);
			model.setPinPos(p);
			b.setPinPos(model.getPlayerID(), p.getRow(), p.getCol());
			gui.movePin(b);
		}
	}

	public void moveBot() {
		if (b.getCard(model.getPinPos().getRow(), model.getPinPos().getCol()).getOpenings().isBottom()
				&& b.getCard(model.getPinPos().getRow() + 1, model.getPinPos().getCol()).getOpenings().isTop()) {
			Position p = new Position(b.findPlayer(model.getPlayerID()));
			p.setRow(p.getRow() + 1);
			model.setPinPos(p);
			b.setPinPos(model.getPlayerID(), p.getRow(), p.getCol());
			gui.movePin(b);
		}
	}

	public void moveRight() {
		if (b.getCard(model.getPinPos().getRow(), model.getPinPos().getCol()).getOpenings().isRight()
				&& b.getCard(model.getPinPos().getRow(), model.getPinPos().getCol() + 1).getOpenings().isLeft()) {
			Position p = new Position(b.findPlayer(model.getPlayerID()));
			p.setCol(p.getCol() + 1);
			model.setPinPos(p);
			b.setPinPos(model.getPlayerID(), p.getRow(), p.getCol());
			gui.movePin(b);
		}
	}

	public void moveLeft() {
		if (b.getCard(model.getPinPos().getRow(), model.getPinPos().getCol()).getOpenings().isLeft()
				&& b.getCard(model.getPinPos().getRow(), model.getPinPos().getCol() - 1).getOpenings().isRight()) {
			Position p = new Position(b.findPlayer(model.getPlayerID()));
			p.setCol(p.getCol() - 1);
			model.setPinPos(p);
			b.setPinPos(model.getPlayerID(), p.getRow(), p.getCol());
			gui.movePin(b);
		}
	}
}
