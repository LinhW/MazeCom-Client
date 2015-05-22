package view.testClasses.userInterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import jaxb.TreasureType;
import tools.Debug;
import tools.DebugLevel;
import view.testClasses.Card;
import view.testClasses.Card.CardShape;
import view.testClasses.Card.Orientation;
import view.testClasses.Messages;
import config.Settings;

public class GraphicalCardBuffered extends JPanel implements ComponentListener {

	private static final long serialVersionUID = 7583185643671311612L;
	private Image shape;
	private Image treasure;
	private List<Integer> pin;
	private TexturePaint paintBuffer = null;
	private CardShape cardShape;
	private Orientation cardOrientation;
	private TreasureType cardTreasure;
	private int maxSize;
	private int minSize;

	public GraphicalCardBuffered() {
		super();
		// Debuging mit Hintergrundfarbe um Framegr????e besser erkennen zu
		// k??nnen
		// setBackground(Color.blue);
		loadShape(CardShape.T, Orientation.D0);
		loadTreasure(null);
		loadPins(null);
		addComponentListener(this);
		maxSize = 150;
		minSize = 50;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public void setCard(Card c) {
		loadShape(c.getShape(), c.getOrientation());
		loadTreasure(c.getTreasure());
		loadPins(c.getPin().getPlayerID());
		componentResized(new ComponentEvent(this, -1));
	}

	public void loadShape(CardShape cs, Orientation co) {
		if (cs == this.cardShape && co == this.cardOrientation) {
			return;
		}
		this.cardShape = cs;
		this.cardOrientation = co;
		try {
			URL url = GraphicalCardBuffered.class.getResource(Settings.IMAGEPATH + cs.toString() + co.value() + Settings.IMAGEFILEEXTENSION);
			System.out.println(url);
			Debug.print(Messages.getInstance().getString("GraphicalCardBuffered.Load") + url.toString(), DebugLevel.DEBUG); //$NON-NLS-1$
			shape = ImageIO.read(url);

		} catch (IOException e) {
		}
		updatePaint();
	}

	// FIXME
	/**
	 * show a JOptionPane with an image for a specified time
	 * 
	 * @param title
	 * @param filepath
	 * @param scaling
	 * @param timer
	 * @param smaller
	 * @throws Exception
	 */
	public static void showMessageWithTimer(String title, String filepath, int scaling, int timer, boolean smaller) throws Exception {
		ImageIcon icon = new ImageIcon(filepath);
		Image img = icon.getImage();
		if (img.getWidth(null) < 0 || img.getHeight(null) < 0)
			throw new Exception("invalid adress. Cannot find image");
		Image newimg;
		if (smaller) {
			newimg = img.getScaledInstance(img.getWidth(null) / scaling, img.getHeight(null) / scaling, java.awt.Image.SCALE_SMOOTH);
		} else {
			newimg = img.getScaledInstance(img.getWidth(null) * scaling, img.getHeight(null) * scaling, java.awt.Image.SCALE_SMOOTH);
		}
		icon = new ImageIcon(newimg);
		JLabel lb = new JLabel(icon, JLabel.CENTER);
		JOptionPane pane = new JOptionPane(lb);
		final JDialog dialog;
		dialog = pane.createDialog(null, title);
		dialog.setAlwaysOnTop(true);
		dialog.setModal(false);
		dialog.setVisible(true);

		new Timer(timer, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		}).start();
	}

	public void loadTreasure(TreasureType t) {
		if (t == this.cardTreasure) {
			return;
		}
		this.cardTreasure = t;
		try {
			if (t != null) {
				URL url = GraphicalCardBuffered.class.getResource(Settings.IMAGEPATH + t.value() + Settings.IMAGEFILEEXTENSION);
				Debug.print(Messages.getInstance().getString("GraphicalCardBuffered.Load") + url.toString(), DebugLevel.DEBUG); //$NON-NLS-1$
				treasure = ImageIO.read(url);
			} else {
				treasure = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		updatePaint();
	}

	public void loadPins(List<Integer> list) {
		if (list != null && list.size() != 0)
			pin = list;
		else
			pin = null;
		updatePaint();
	}

	private void updatePaint() {
		int w = 0, h = 0;
		if (shape == null) {
			paintBuffer = null;
			return;
		}
		w = h = Math.min(shape.getWidth(null), shape.getHeight(null));

		if (w <= 0 || h <= 0) {
			paintBuffer = null;
			return;
		}

		BufferedImage buff = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);

		Graphics2D g2 = buff.createGraphics();
		if (shape != null) {
			g2.drawImage(shape, 0, 0, null);
		}
		if (treasure != null) {
			int zentrum = h / 2 - treasure.getHeight(null) / 2;
			g2.drawImage(treasure, zentrum, zentrum, null);
		}
		if (pin != null) {
			g2.setColor(Color.BLACK);
			int height = 15;
			int width = 20;
			int zentrum = h / 2 - height / 2;
			g2.fillOval(zentrum, zentrum, height, width);
			char[] number = { (pin.get(0).toString()).charAt(0) };
			g2.drawChars(number, 0, 1, zentrum, zentrum);
		}
		paintBuffer = new TexturePaint(buff, new Rectangle(0, 0, w, h));
		g2.dispose();
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (paintBuffer != null) {
			int w = shape.getWidth(null);
			int h = shape.getHeight(null);
			Insets in = getInsets();

			int x = in.left;
			int y = in.top;
			w = w - in.left - in.right;
			h = h - in.top - in.bottom;

			if (w >= 0 && h >= 0) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setPaint(paintBuffer);
				g2.fillRect(x, y, w, h);
			}
		}
	}

	public void blinkCard(long millis, int n) {
		Image save = shape;
		for (int i = 0; i < n; i++) {
			shape = null;
			updatePaint();
			try {
				Thread.sleep(millis / n);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			shape = save;
			updatePaint();
		}
	}

	public void blinkPlayer(long millis, int n) {
		List<Integer> save = pin;
		for (int i = 0; i < n; i++) {
			pin = null;
			updatePaint();
			try {
				Thread.sleep(millis / n);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			pin = save;
			updatePaint();
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentResized(ComponentEvent e) {
		// System.out.println("h"+getSize().height + " w" + getSize().width);
		Dimension d = getSize();
		int size = Math.min(maxSize, Math.min(d.height, d.width));
		size = Math.max(minSize, size);
		if (shape != null) {
			Image temp = shape;
			shape = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			shape = temp.getScaledInstance(size, size, Image.SCALE_DEFAULT);
		}
		setSize(size, size);
		updatePaint();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		componentResized(e);
	}

}