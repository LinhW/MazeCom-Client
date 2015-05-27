package network;

import gui.data.Card;
import gui.data.Position;
import gui.testClasses.Tmp_testGUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import jaxb.MazeCom;
import control.Controller;
import control.EventController;

public class Connection {
	private boolean isConnected;

	private Socket clientSocket;
	private UTFOutputStream outToServer;
	private ByteArrayOutputStream byteOutToServer;
	private UTFInputStream inFromServer;
	private ByteArrayInputStream byteInFromServer;
	private ServerListener serverListener;
//	private Controller controller;

	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;
	private MazeComMessageFactory messageFactory;

	private EventController ctrl_event;

	public Connection(Controller controller) {
		super();
//		this.controller = controller;
		ctrl_event = new EventController(this);
		ctrl_event.login();
	}

	private class ServerListener extends Thread {
		private boolean shutdown;

		public void shutdown() {
			shutdown = true;
		}

		public void processMessage(MazeCom message) {
			// TODO einfach auskommentieren wenn ich es vergessen habe
			 Tmp_testGUI.receiveServerMessage(message);
//
//			String type = message.getMcType().name();
//			Controller.NotificationType notification;
//			if (type.equals("LOGIN")) {
//				notification = NotificationType.NOTIFY_UNKNOWN;
//			} else if (type.equals("LOGINREPLY")) {
//				notification = NotificationType.NOTIFY_WAIT;
//			} else if (type.equals("AWAITMOVE")) {
//				notification = NotificationType.NOTIFY_YOUR_MOVE;
//			} else if (type.equals("MOVE")) {
//				notification = NotificationType.NOTIFY_MOVE;
//			} else if (type.equals("ACCEPT")) {
//				notification = NotificationType.NOTIFY_WAIT;
//			} else if (type.equals("WIN")) {
//				notification = NotificationType.NOTIFY_WIN;
//			} else if (type.equals("DISCONNECT")) {
//				notification = NotificationType.NOTIFY_DISCONNECT;
//			} else {
//				notification = NotificationType.NOTIFY_UNKNOWN;
//			}
//			controller.notifyWindow(notification);

			switch (message.getMcType()) {
			case ACCEPT:
				ctrl_event.receiveAcceptMessage(message.getAcceptMessage());
				break;
			case AWAITMOVE:
				ctrl_event.receiveAwaitMoveMessage(message.getAwaitMoveMessage());
				break;
			case DISCONNECT:
				ctrl_event.receiveDisconnectMessage(message.getDisconnectMessage());
				break;
			case LOGINREPLY:
				ctrl_event.receiveLoginReply(message.getLoginReplyMessage());
				break;
			case MOVE:
				ctrl_event.receiveMoveMessage(message.getMoveMessage());
				break;
			case WIN:
				ctrl_event.receiveWinMessage(message.getWinMessage());
				break;
			default:
				break;
			}
		}

		public void run() {
			shutdown = false;
			while (!shutdown) {
				try {
					byteInFromServer = new ByteArrayInputStream(inFromServer.readUTF8().getBytes());
					processMessage((MazeCom) unmarshaller.unmarshal(byteInFromServer));
				} catch (IOException e) {
					e.printStackTrace();
					shutdown();
				} catch (JAXBException e) {
					e.printStackTrace();
					shutdown();
				}
			}
		}
	}

	private boolean createSocket(String host, int port) {
		try {
			clientSocket = new Socket(host, port);
			outToServer = new UTFOutputStream(clientSocket.getOutputStream());
			byteOutToServer = new ByteArrayOutputStream();
			inFromServer = new UTFInputStream(clientSocket.getInputStream());
			serverListener = new ServerListener();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void sendMoveMessage(int PlayerID, Card c, Position shift, Position pin) {
		MazeCom message = messageFactory.createMoveMessage(PlayerID, c, shift, pin);
		try {
			byteOutToServer.reset();
			marshaller.marshal(message, byteOutToServer);
			outToServer.writeUTF8(new String(byteOutToServer.toByteArray()));
		} catch (JAXBException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean loginOnServer(String name) {
		messageFactory = new MazeComMessageFactory();
		MazeCom message = messageFactory.createLoginMessage(name);
		try {
			jaxbContext = JAXBContext.newInstance(MazeCom.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			unmarshaller = jaxbContext.createUnmarshaller();

			byteOutToServer.reset();
			marshaller.marshal(message, byteOutToServer);
			outToServer.writeUTF8(new String(byteOutToServer.toByteArray()));
		} catch (JAXBException e1) {
			e1.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean establishConnection(String name, String host, int port) {
		boolean success = true;
		if (isConnected) {
			success = false;
		} else if (!createSocket(host, port)) {
			success = false;
		} else if (!loginOnServer(name)) {
			success = false;
		} else {
			isConnected = true;
		}
		if (success) {
			serverListener.start();
		}
		return success;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean closeConnection() {
		if (isConnected) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		try {
			serverListener.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		isConnected = false;
		return true;
	}
}
