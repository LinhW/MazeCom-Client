package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import jaxb.LoginMessageType;
import jaxb.ObjectFactory;

public class Connection {
	private boolean isConnected;
	
	private Socket clientSocket;
	private DataOutputStream outToServer;
	private ByteArrayOutputStream byteOutToServer;
	private DataInputStream inFromServer;
	private ByteArrayInputStream byteInFromServer;
	private ServerListener serverListener;
	
	private ObjectFactory objectFactory;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;
	
	public Connection() {
		super();
	}
	
	private class ServerListener extends Thread {
		private boolean shutdown;
		
		public void shutdown() {
			shutdown = true;
		}
		
		public void processMessage(Object Message) {
			
		}
		
		public void run() {
			shutdown = false;
			while (!shutdown) {
				try {
					byteInFromServer = new ByteArrayInputStream(inFromServer.readUTF().getBytes());
					processMessage(unmarshaller.unmarshal(byteInFromServer));
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
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			byteOutToServer = new ByteArrayOutputStream();
			inFromServer = new DataInputStream(clientSocket.getInputStream());
			serverListener = new ServerListener();
			serverListener.start();
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
		objectFactory = new ObjectFactory();
		LoginMessageType message = objectFactory.createLoginMessageType();
		message.setName(name);
		try {
			jaxbContext = JAXBContext.newInstance(LoginMessageType.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			unmarshaller = jaxbContext.createUnmarshaller();

			byteOutToServer.reset();
			marshaller.marshal(message, byteOutToServer);
			outToServer.writeUTF(new String(byteOutToServer.toByteArray()));
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
		if (!createSocket(host, port)) {
			return false;
		}
		else if (!loginOnServer(name)) {
			return false;
		}
		else {
			isConnected = true;
			return true;
		}
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
		isConnected = false;
		return true;
	}
}
