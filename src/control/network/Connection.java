package control.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import model.jaxb.CardType;
import model.jaxb.MazeCom;
import model.jaxb.PositionType;
import model.jaxb.WinMessageType.Winner;
import control.AI.AiVsAI;
import control.AI.Player;

public class Connection {
	private boolean isConnected;

	private Socket clientSocket;
	private UTFOutputStream outToServer;
	private ByteArrayOutputStream byteOutToServer;
	private UTFInputStream inFromServer;
	private ByteArrayInputStream byteInFromServer;
	private ServerListener serverListener;

	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;
	private MazeComMessageFactory messageFactory;

	private Player ctrl_event;

	private boolean hasPlayer;
	private AiVsAI aiai;

	public Connection() {
		super();
		hasPlayer = false;
	}

	public Connection(AiVsAI aiai) {
		super();
		this.aiai = aiai;
		hasPlayer = false;
	}

	public void setPlayer(Player p) {
		hasPlayer = true;
		this.ctrl_event = p;
	}

	private class ServerListener extends Thread {
		private boolean shutdown;

		public void shutdown() {
			shutdown = true;
		}

		public void processMessage(MazeCom message) {
			switch (message.getMcType()) {
			case ACCEPT:
				ctrl_event.receiveAcceptMessage(message.getAcceptMessage());
				break;
			case AWAITMOVE:
				ctrl_event.receiveAwaitMoveMessage(message.getAwaitMoveMessage());
				break;
			case DISCONNECT:
				ctrl_event.receiveDisconnectMessage(message.getDisconnectMessage());
				shutdown();
				break;
			case LOGINREPLY:
				ctrl_event.receiveLoginReply(message.getLoginReplyMessage());
				break;
			case WIN:
				ctrl_event.receiveWinMessage(message.getWinMessage());
				break;
			default:
				break;
			}
		}

		public void run() {
			shutdown = !hasPlayer;
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
		if (!hasPlayer) {
			return false;
		}
		try {
			clientSocket = new Socket(host, port);
			outToServer = new UTFOutputStream(clientSocket.getOutputStream());
			byteOutToServer = new ByteArrayOutputStream();
			inFromServer = new UTFInputStream(clientSocket.getInputStream());
			serverListener = new ServerListener();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(null, "There is currently no server running.", "Server error!", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void sendMoveMessage(int PlayerID, CardType c, PositionType shift, PositionType pin) {
		if (!hasPlayer) {
			return;
		}
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

	private boolean loginOnServer() {
		if (!hasPlayer) {
			return false;
		}
		messageFactory = new MazeComMessageFactory();
		MazeCom message = messageFactory.createLoginMessage(ctrl_event.login());
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

	public boolean establishConnection(String host, int port) {
		if (!hasPlayer) {
			return false;
		}
		boolean success = true;
		if (isConnected) {
			success = false;
		} else if (!createSocket(host, port)) {
			success = false;
		} else if (!loginOnServer()) {
			success = false;
		} else {
			isConnected = true;
		}
		if (success) {
			serverListener.start();
		}
		return success;
	}

	public void sendWin(Winner winner) {
		if (aiai != null) {
			AiVsAI.updateWinnerStat(winner);
		}
	}

	// public boolean closeConnection() {
	// if (isConnected) {
	// try {
	// clientSocket.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// return false;
	// }
	// }
	// try {
	// serverListener.join();
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// isConnected = false;
	// return true;
	// }
}
