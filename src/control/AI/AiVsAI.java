package control.AI;

import java.awt.event.ActionEvent;

import server.Game;
import server.userInterface.BetterUI;
import config.Settings;
import control.AI.ava.Ava;
import control.network.Connection;

public class AiVsAI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AiVsAI a = new AiVsAI();
		a.start();
	}

	public void start() {
		Server server = new Server();
		server.start();
		server.startGame(4);

		startClients();
	}

	private void startClients() {
		Connection connection = new Connection();
		Client c1 = new Client(new TryAndError(connection), connection);
		c1.start();

		connection = new Connection();
		Client c2 = new Client(new TryAndError(connection), connection);
		c2.start();

		connection = new Connection();
		Client c3 = new Client(new TryAndError(connection), connection);
		c3.start();

		connection = new Connection();
		Client c4 = new Client(new TryAndError(connection), connection);
		c4.start();
	}

	private class Client extends Thread {
		private Player player;
		private Connection con;

		@Override
		public void start() {
			con.setPlayer(player);
			con.establishConnection("localhost", 5123);
		}

		public Client(Player player, Connection connection) {
			this.player = player;
			this.con = connection;
		}
	}

	private class Server extends Thread {
		private Game game;
		private BetterUI userinterface;

		@Override
		public void start() {
			game = new Game();
			userinterface = (BetterUI) game.main();
		}

		public void startGame(int player) {
			Settings.DEFAULT_PLAYERS = player;
			userinterface.MIStartActionPerformed(new ActionEvent(this, 1, "ai"));
		}

		public void stopGame() {
			userinterface.MIStartActionPerformed(new ActionEvent(this, 1, "ai"));
		}
	}

}
