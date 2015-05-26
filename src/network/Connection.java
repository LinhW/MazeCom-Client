package network;

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
import control.Controller.NotificationType;

public class Connection {
	private boolean isConnected;

	private Socket clientSocket;
	private UTFOutputStream outToServer;
	private ByteArrayOutputStream byteOutToServer;
	private UTFInputStream inFromServer;
	private ByteArrayInputStream byteInFromServer;
	private ServerListener serverListener;
	private Controller controller;

	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;
	private MazeComMessageFactory messageFactory;

	public Connection(Controller controller) {
		super();
		this.controller = controller;
	}

	private class ServerListener extends Thread {
		private boolean shutdown;

		public void shutdown() {
			shutdown = true;
		}

		public void processMessage(MazeCom message) {
			// TODO einfach auskommentieren wenn ich es vergessen habe
//			Tmp_testGUI.receiveServerMessage(message);
			
			String type = message.getMcType().name();
			Controller.NotificationType notification;
			if (type.equals("LOGIN")) {
				notification = NotificationType.NOTIFY_UNKNOWN;
			}
			else if (type.equals("LOGINREPLY")) {
				notification = NotificationType.NOTIFY_WAIT;
			}
			else if (type.equals("AWAITMOVE")) {
				notification = NotificationType.NOTIFY_YOUR_MOVE;
			}
			else if (type.equals("MOVE")) {
				notification = NotificationType.NOTIFY_MOVE;
			}
			else if (type.equals("ACCEPT")) {
				notification = NotificationType.NOTIFY_WAIT;
			}
			else if (type.equals("WIN")) {
				notification = NotificationType.NOTIFY_WIN;
			}
			else if (type.equals("DISCONNECT")) {
				notification = NotificationType.NOTIFY_DISCONNECT;
			}
			else {
				notification = NotificationType.NOTIFY_UNKNOWN;
			}
			controller.notifyWindow(notification);
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
