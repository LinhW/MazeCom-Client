package control;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import jaxb.LoginMessageType;
import jaxb.ObjectFactory;

public class Main {
	private void run() {
		DataOutputStream outToServer;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataInputStream inFromServer;
		ObjectFactory objectFactory = new ObjectFactory();
		JAXBContext jaxbContext;
		Marshaller marshaller;
		try {
			jaxbContext = JAXBContext.newInstance(LoginMessageType.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		} catch (JAXBException e1) {
			e1.printStackTrace();
			return;
		}
		LoginMessageType message = objectFactory.createLoginMessageType();
		message.setName("TestName");
		Socket clientSocket;
		try {
			clientSocket = new Socket("localhost", 5123);
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			inFromServer = new DataInputStream(clientSocket.getInputStream());
			try {
				byteArrayOutputStream.reset();
				marshaller.marshal(message, byteArrayOutputStream);
				outToServer.writeUTF(new String(byteArrayOutputStream.toByteArray()));
			} catch (JAXBException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(inFromServer.readUTF());
			clientSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Main m = new Main();
		m.run();
	}
}
