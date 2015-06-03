package control.AI;

import java.awt.event.ActionEvent;

import model.jaxb.WinMessageType.Winner;
import server.Game;
import server.userInterface.BetterUI;
import config.Settings;
import control.AI.Player;
import control.AI.RandomAIAdvanced;
import control.AI.ava.WriteIntoFile;
import control.network.Connection;

public class AiVsAI {
	private int p1 = 0, p2 = 0, p3 = 0, p4 = 0;
	private static int number = 0;
	private WriteIntoFile wif;
	private int count = 50;

	public static void main(String[] args) {
		AiVsAI a = new AiVsAI();
		a.init();
	}

	public void init() {
		wif = new WriteIntoFile("WinnerStat.txt");
		wif.clearFile();
		start();
	}

	public void start() {
		number++;
		Server server = new Server();
		server.start();
		server.startGame(4);
		startClients();
	}

	private void showResults() {
		wif.write("Player1: " + p1 + " wins");
		wif.write("Player2: " + p2 + " wins");
		wif.write("Player3: " + p3 + " wins");
		wif.write("Player4: " + p4 + " wins");
	}

	public void updateWinnerStat(Winner winner) {
		switch (winner.getId()) {
		case 1:
			p1++;
			break;
		case 2:
			p2++;
			break;
		case 3:
			p3++;
			break;
		case 4:
			p4++;
			break;
		}
		if (number < count) {
			start();
		} else {
			showResults();
		}
	}

	private void startClients() {
		Connection connection = new Connection(this);
		Client c1 = new Client(new RandomAIAdvanced(connection), connection);
		c1.start();

		connection = new Connection(this);
		Client c2 = new Client(new RandomAIAdvanced(connection), connection);
		c2.start();

		connection = new Connection(this);
		Client c3 = new Client(new RandomAIAdvanced(connection), connection);
		c3.start();

		connection = new Connection(this);
		Client c4 = new Client(new RandomAIAdvanced(connection), connection);
		c4.start();
	}

	private class Client extends Thread {
		private Player player;
		private Connection con;

		@Override
		public void start() {
			con.setPlayer(player);
			con.establishConnection("localhost", Settings.PORT);
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
