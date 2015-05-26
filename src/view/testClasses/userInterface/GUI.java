package view.testClasses.userInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import jaxb.BoardType.Row;
import jaxb.CardType;
import jaxb.MoveMessageType;
import view.GUIController;
import view.data.Context;
import view.data.GUIModel;
import view.data.PersData;
import view.interfaces.IView;
import view.testClasses.Board;
import view.testClasses.Card;
import view.testClasses.Messages;
import view.testClasses.Position;
import config.Settings;

@SuppressWarnings("serial")
public class GUI extends JFrame implements IView {

	UIBoard uiboard = new UIBoard();
	private static final boolean animateMove = true;
	private static final boolean animateShift = true;
	private static final int animationFrames = 10;
	private int animationState = 0;
	Object animationFinished = new Object();
	Timer animationTimer;
	AnimationProperties animationProperties = null;
	public GraphicalCardBuffered shiftCard;
	private JLabel lb_shiftCard;
	private JLabel lb_treasure_pic;
	private JLabel lb_statistic;
	private boolean hasFocus;
	private KeyboardFocusManager manager;
	private MyDispatcher dispatcher;
	private GUIController myController;
	private GUIModel model;

	private static class ImageRessources {
		private static HashMap<String, Image> images = new HashMap<String, Image>();

		public static Image getImage(String name) {
			if (images.containsKey(name)) {
				return images.get(name);
			}
			URL u = ImageRessources.class.getResource(Settings.IMAGEPATH + name + Settings.IMAGEFILEEXTENSION);
			Image img = null;
			try {
				img = ImageIO.read(u);
				images.put(name, img);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return img;
		}
	}

	private class UIBoard extends JPanel {
		Board board;
		Image images[][] = new Image[7][7];
		Card c[][] = new Card[7][7];
		private int pixelsPerField;

		public void setBoard(Board b) {
			if (b == null) {
				this.board = null;
				return;
			}
			this.board = (Board) b.clone();
			int y = 0, x = 0;
			for (Row r : b.getRow()) {
				x = 0;
				for (CardType ct : r.getCol()) {
					Card card = new Card(ct);
					c[y][x] = card;
					images[y][x] = ImageRessources.getImage(card.getShape().toString() + card.getOrientation().value());
					if (c[y][x].getTreasure() != null) {
						ImageRessources.getImage(c[y][x].getTreasure().value());
					}
					x++;
				}
				y++;
			}
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (board == null)
				return;
			int width = this.getWidth();
			int height = this.getHeight();
			width = height = Math.min(width, height);
			width = height -= width % 7;
			pixelsPerField = width / 7;

			for (int y = 0; y < 7; y++) {
				for (int x = 0; x < 7; x++) {
					int topLeftY = pixelsPerField * y;
					int topLeftX = pixelsPerField * x;
					if (animationProperties != null) {
						if (animationProperties.vertikal && x == animationProperties.shiftPosition.getCol()) {
							topLeftY += animationProperties.direction * (pixelsPerField * animationState / animationFrames);
						} else if (!animationProperties.vertikal && y == animationProperties.shiftPosition.getRow()) {
							topLeftX += animationProperties.direction * (pixelsPerField * animationState / animationFrames);
						}
					}

					g.drawImage(images[y][x], topLeftX, topLeftY, pixelsPerField, pixelsPerField, null);
					if (c[y][x] != null) {

						if (c[y][x].getTreasure() != null) {
							g.drawImage(ImageRessources.getImage(c[y][x].getTreasure().value()), topLeftX + pixelsPerField / 4, topLeftY + pixelsPerField / 4, pixelsPerField / 2,
									pixelsPerField / 2, null);
						}
						// Zeichnen der SpielerPins
						for (Integer playerID : c[y][x].getPin().getPlayerID()) {
							g.setColor(colorForPlayer(playerID));
							g.fillOval(topLeftX + pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) / 2), topLeftY + pixelsPerField / 4 + pixelsPerField / 4
									* ((playerID - 1) % 2), pixelsPerField / 4, pixelsPerField / 4);

							g.setColor(Color.WHITE);
							g.drawOval(topLeftX + pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) / 2), topLeftY + pixelsPerField / 4 + pixelsPerField / 4
									* ((playerID - 1) % 2), pixelsPerField / 4, pixelsPerField / 4);
							centerStringInRect((Graphics2D) g, playerID.toString(), topLeftX + pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) / 2), topLeftY
									+ pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) % 2), pixelsPerField / 4, pixelsPerField / 4);
						}
					} else {
						System.out.println(String.format(Messages.getInstance().getString("BetterUI.cardIsNull"), x, y)); //$NON-NLS-1$
					}
				}
			}
			// Zeichnen der eingeschobenen karte in der animation
			if (animationProperties != null) {
				int topLeftY = pixelsPerField * (animationProperties.shiftPosition.getRow() - (animationProperties.vertikal ? animationProperties.direction : 0));
				int topLeftX = pixelsPerField * (animationProperties.shiftPosition.getCol() - (!animationProperties.vertikal ? animationProperties.direction : 0));
				if (animationProperties.vertikal) {
					topLeftY += animationProperties.direction * (pixelsPerField * animationState / animationFrames);
				} else {
					topLeftX += animationProperties.direction * (pixelsPerField * animationState / animationFrames);
				}
				Card card = new Card(board.getShiftCard());
				g.drawImage(ImageRessources.getImage(card.getShape().toString() + card.getOrientation().value()), topLeftX, topLeftY, pixelsPerField, pixelsPerField, null);
				if (card.getTreasure() != null) {
					g.drawImage(ImageRessources.getImage(card.getTreasure().value()), topLeftX + pixelsPerField / 4, topLeftY + pixelsPerField / 4, pixelsPerField / 2,
							pixelsPerField / 2, null);
				}
				g.setColor(Color.YELLOW);
				g.drawRect(topLeftX, topLeftY, pixelsPerField, pixelsPerField);
			}
		}

		public int getPixelsPerField() {
			return pixelsPerField;
		}

		private void centerStringInRect(Graphics2D g2d, String s, int x, int y, int height, int width) {
			Rectangle size = g2d.getFontMetrics().getStringBounds(s, g2d).getBounds();
			float startX = (float) (width / 2 - size.getWidth() / 2);
			float startY = (float) (height / 2 - size.getHeight() / 2);
			g2d.drawString(s, startX + x - size.x, startY + y - size.y);
		}

	}

	private void createView() {
		getContentPane().setLayout(new BorderLayout());
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel splitPanel_right = new JPanel();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, uiboard, splitPanel_right);
		splitPane.setDividerLocation(470);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		{
			JPanel sp_top = new JPanel();
			{
				sp_top.setLayout(new BorderLayout());
				lb_shiftCard = new JLabel();
				sp_top.add(lb_shiftCard, BorderLayout.CENTER);
			}

			JPanel sp_bottom = new JPanel();
			{
				sp_bottom.setLayout(new BorderLayout());

				lb_treasure_pic = new JLabel();
				sp_bottom.add(lb_treasure_pic, BorderLayout.CENTER);

				JLabel lb_treasure = new JLabel("Treasure");
				lb_treasure.setBorder(new EmptyBorder(5, 5, 5, 5));
				sp_bottom.add(lb_treasure, BorderLayout.WEST);

				JLabel lb_player = new JLabel(((PersData) Context.getInstance().getValue(Context.USER)).getName());
				lb_player.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new EmptyBorder(7, 10, 7, 5)));
				sp_bottom.add(lb_player, BorderLayout.NORTH);

				lb_statistic = new JLabel();
				lb_statistic.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(5, 5, 5, 5)));
				sp_bottom.add(lb_statistic, BorderLayout.SOUTH);
			}

			JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			{
				sp.setDividerLocation(250);
				sp.setLeftComponent(sp_top);
				sp.setRightComponent(sp_bottom);
			}

			splitPanel_right.setLayout(new BorderLayout(0, 0));
			splitPanel_right.add(sp);
		}

	}

	private void update_own() {
		PersData p = (PersData) Context.getInstance().getValue(Context.USER);
		Card c = new Card(((Board) Context.getInstance().getValue(Context.BOARD)).getShiftCard());
		lb_shiftCard.setIcon(new ImageIcon(ImageRessources.getImage(c.value())));
		lb_treasure_pic.setIcon(new ImageIcon(ImageRessources.getImage(p.getCurrentTreasure().value())));
		lb_statistic.setText("Treasures to go: " + p.getTreasuresToFind());
	}

	public GUI(GUIController ctrl_gui, GUIModel model) {
		// Eigenname
		super("Das verrückte Labyrinth"); //$NON-NLS-1$
		myController = ctrl_gui;
		this.model = model;
		setSize(new Dimension(750, 500));
		setPreferredSize(new Dimension(2000, 1000));
		addListener();
		createView();
	}

	public void addListener() {
		manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		dispatcher = new MyDispatcher();
		manager.addKeyEventDispatcher(dispatcher);

		this.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent e) {
				hasFocus = false;
				System.out.println("LostFocus");
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				hasFocus = true;
				System.out.println("hasFocus");
			}
		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				myController.onClose();
			}
		});
	}

	public void removeListener() {
		manager.removeKeyEventDispatcher(dispatcher);
	}

	protected static String[] arguments;

	private class AnimationProperties {
		public final boolean vertikal;
		public final Position shiftPosition;
		public final int direction;

		public AnimationProperties(Position shiftPosition) {
			this.shiftPosition = shiftPosition;
			if (shiftPosition.getCol() == 6 || shiftPosition.getCol() == 0) {
				vertikal = false;
				direction = shiftPosition.getCol() == 0 ? 1 : -1;
			} else if (shiftPosition.getRow() == 6 || shiftPosition.getRow() == 0) {
				vertikal = true;
				direction = shiftPosition.getRow() == 0 ? 1 : -1;
			} else {
				throw new IllegalArgumentException(Messages.getInstance().getString("BetterUI.cantShift")); //$NON-NLS-1$
			}
		}
	}

	private class ShiftAnimationTimerOperation implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			animationState++;
			uiboard.repaint();
			if (animationState == animationFrames) {
				animationState = 0;
				animationTimer.stop();
				animationTimer = null;
				animationProperties = null;
				synchronized (animationFinished) {
					animationFinished.notify();
				}
			}
		}
	}

	private static class Pathfinding {
		public static int[][] findShortestPath(Board b, Position startPos, Position endPos) {

			// Dijkstra
			boolean[][] visited = new boolean[7][7];
			int[][] weglen = new int[7][7];
			int[][] pfad = new int[7][7];
			for (int y = 0; y < 7; ++y) {
				for (int x = 0; x < 7; ++x) {
					weglen[y][x] = Integer.MAX_VALUE;
				}
			}

			int currentX = startPos.getCol();
			int currentY = startPos.getRow();
			weglen[currentY][currentX] = 0;
			while (true) {
				visited[currentY][currentX] = true;
				if (currentX > 0 && b.getCard(currentY, currentX).getOpenings().isLeft() && b.getCard(currentY, currentX - 1).getOpenings().isRight()) {
					if (weglen[currentY][currentX - 1] > weglen[currentY][currentX] + 1) {
						weglen[currentY][currentX - 1] = weglen[currentY][currentX] + 1;
						pfad[currentY][currentX - 1] = currentY * 7 + currentX;
					}
				}
				if (currentY > 0 && b.getCard(currentY, currentX).getOpenings().isTop() && b.getCard(currentY - 1, currentX).getOpenings().isBottom()) {
					if (weglen[currentY - 1][currentX] > weglen[currentY][currentX] + 1) {
						weglen[currentY - 1][currentX] = weglen[currentY][currentX] + 1;
						pfad[currentY - 1][currentX] = currentY * 7 + currentX;
					}
				}

				if (currentX < 6 && b.getCard(currentY, currentX).getOpenings().isRight() && b.getCard(currentY, currentX + 1).getOpenings().isLeft()) {
					if (weglen[currentY][currentX + 1] > weglen[currentY][currentX] + 1) {
						weglen[currentY][currentX + 1] = weglen[currentY][currentX] + 1;
						pfad[currentY][currentX + 1] = currentY * 7 + currentX;
					}
				}
				if (currentY < 6 && b.getCard(currentY, currentX).getOpenings().isBottom() && b.getCard(currentY + 1, currentX).getOpenings().isTop()) {
					if (weglen[currentY + 1][currentX] > weglen[currentY][currentX] + 1) {
						weglen[currentY + 1][currentX] = weglen[currentY][currentX] + 1;
						pfad[currentY + 1][currentX] = currentY * 7 + currentX;
					}
				}

				{
					int currentMinWegLen = Integer.MAX_VALUE;
					for (int y = 6; y >= 0; --y) {
						for (int x = 6; x >= 0; --x) {
							if (!visited[y][x] && weglen[y][x] < currentMinWegLen) {
								currentMinWegLen = weglen[y][x];
								currentX = x;
								currentY = y;
							}
						}
					}
					if (currentMinWegLen == Integer.MAX_VALUE)
						break;
				}
			}
			currentX = endPos.getCol();
			currentY = endPos.getRow();
			int anzahlWegpunkte = weglen[currentY][currentX] + 1;
			// Weg ist ein Array von x und y werten
			int weg[][] = new int[anzahlWegpunkte][2];
			int i = anzahlWegpunkte - 1;
			while (i > 0) {
				weg[i--] = new int[] { currentX, currentY };
				int buf = pfad[currentY][currentX];
				currentX = buf % 7;
				currentY = buf / 7;
			}
			weg[0] = new int[] { currentX, currentY };
			return weg;
		}
	}

	private class MoveAnimationTimerOperation implements ActionListener {
		int[][] points;

		public MoveAnimationTimerOperation(Board b, Position startPos, Position endPos) {
			points = Pathfinding.findShortestPath(b, startPos, endPos);
			uiboard.c[endPos.getRow()][endPos.getCol()].getPin().getPlayerID().remove(new Integer(1));
			uiboard.c[startPos.getRow()][startPos.getCol()].getPin().getPlayerID().add(new Integer(1));
		}

		int i = 0;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (i + 1 == points.length) {
				synchronized (animationFinished) {
					animationTimer.stop();
					animationTimer = null;
					animationFinished.notify();
				}
				return;
			}
			uiboard.c[points[i][1]][points[i][0]].getPin().getPlayerID().remove(new Integer(1));
			i++;
			uiboard.c[points[i][1]][points[i][0]].getPin().getPlayerID().add(new Integer(1));
			uiboard.repaint();

		}
	}

	public void displayMove(MoveMessageType mm, Board b, long moveDelay, long shiftDelay) {
		// Die Dauer von shiftDelay bezieht sich auf den kompletten Shift und
		// nicht auf einen einzelnen Frame
		shiftDelay /= animationFrames;
		shiftCard.setCard(new Card(mm.getShiftCard()));
		if (animateShift) {
			uiboard.board.setShiftCard(mm.getShiftCard());
			animationTimer = new Timer((int) shiftDelay, new ShiftAnimationTimerOperation());
			animationProperties = new AnimationProperties(new Position(mm.getShiftPosition()));
			synchronized (animationFinished) {
				animationTimer.start();
				try {
					animationFinished.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Position oldPlayerPos = new Position(uiboard.board.findPlayer(1));
		uiboard.setBoard(b);
		// XXX: Von Matthias (alte Karten waren vorher noch sichtbar)
		uiboard.repaint();
		if (animateMove) {
			// Falls unser Spieler sich selbst verschoben hat.
			AnimationProperties props = new AnimationProperties(new Position(mm.getShiftPosition()));
			if (props.vertikal) {
				if (oldPlayerPos.getCol() == props.shiftPosition.getCol()) {
					oldPlayerPos.setRow((7 + oldPlayerPos.getRow() + props.direction) % 7);
				}
			} else {
				if (oldPlayerPos.getRow() == props.shiftPosition.getRow()) {
					oldPlayerPos.setCol((7 + oldPlayerPos.getCol() + props.direction) % 7);
				}
			}
			animationTimer = new Timer((int) moveDelay, new MoveAnimationTimerOperation(uiboard.board, oldPlayerPos, new Position(mm.getNewPinPos())));
			synchronized (animationFinished) {
				animationTimer.start();
				try {
					animationFinished.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			uiboard.repaint();
		}
	}

	public void update() {
		Board b = (Board) Context.getInstance().getValue(Context.BOARD);
		uiboard.setBoard(b);
		uiboard.repaint();
		update_own();
	}

	public void close() {
		// TODO
		removeListener();
		this.dispose();
	}

	private static Color colorForPlayer(int playerID) {
		switch (playerID) {
		case 0:
			return Color.yellow;
		case 1:
			return Color.GREEN;
		case 2:
			return Color.BLACK;
		case 3:
			return Color.RED;
		case 4:
			return Color.BLUE;
		default:
			throw new IllegalArgumentException(Messages.getInstance().getString("BetterUI.UInotPreparedForPlayerID")); //$NON-NLS-1$
		}
	}

	public void rotate(boolean rotateLeft) {
		// TODO
		int rot = 90;
		if (rotateLeft) {
			rot += 180;
		}
		int orient = (model.getCardOrientation() + rot) % 360;
		System.out.println("" + model.getCardType() + orient);
		lb_shiftCard.setIcon(new ImageIcon(ImageRessources.getImage("" + model.getCardType() + orient)));
		myController.rotated(orient);
	}

	// public void setGame(Game g) {
	// this.g = g;
	// }
	public void gameEnded(int winner) {
		System.out.println("game ended");
		// if (winner != null) {
		//				JOptionPane.showMessageDialog(this, String.format(Messages.getInstance().getString("BetterUI.playerIDwon"), winner.getName() //$NON-NLS-1$
		// , winner.getID()));
		// }
		// // MIStart.setEnabled(true);
		// // MIStop.setEnabled(false);
	}

	/**
	 * Handle KeyEvents (independent from the focused object)
	 * 
	 * @author lgewuerz
	 *
	 */
	private class MyDispatcher implements KeyEventDispatcher {

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (hasFocus) {
				switch (e.getID()) {
				case KeyEvent.KEY_PRESSED:
					key_pressed(e);
					break;
				case KeyEvent.KEY_TYPED:
					key_typed(e);
					break;
				case KeyEvent.KEY_RELEASED:
					key_released(e);
					break;
				}
			}
			return false;
		}

		/**
		 * handling when a key is released
		 * 
		 * @param e
		 *            (KeyEvent)
		 */
		private void key_released(KeyEvent e) {
		}

		/**
		 * handling when a key is typed
		 * 
		 * @param e
		 *            (KeyEvent)
		 */
		private void key_typed(KeyEvent e) {
		}

		/**
		 * handling when a key is pressed
		 * 
		 * @param e
		 *            (KeyEvent)
		 */
		private void key_pressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_L:
				System.out.println("press L");
				rotate(true);
				break;
			case KeyEvent.VK_R:
				System.out.println("press R");
				rotate(false);
				break;
			case KeyEvent.VK_ENTER:
				// TODO
				break;
			case KeyEvent.VK_DOWN:
				// TODO
				break;
			case KeyEvent.VK_UP:
				// TODO
				break;
			case KeyEvent.VK_LEFT:
				// TODO
				break;
			case KeyEvent.VK_RIGHT:
				// TODO
				break;
			}
		}
	}
}