package control.AI;

import java.awt.event.ActionEvent;

import model.jaxb.WinMessageType.Winner;
import server.Game;
import server.userInterface.BetterUI;
import config.Settings;
import control.AI.LAMB.LAMB;
import control.AI.ava.Ava;
import control.AI.ava.WriteIntoFile;
import control.network.Connection;

public class AiVsAI {
	private int p1 = 0, p2 = 0, p3 = 0, p4 = 0;
	private static int number = 0;
	private WriteIntoFile wif;
	private int count = 4;

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
		startClients(server, 0, 1, 1, 1, 0);
	}

	private void showResults() {
		wif.writeNewLine(2);
		wif.write("-----------Stats--------------");
		wif.write("Player1: " + p1 + " wins");
		wif.write("Player2: " + p2 + " wins");
		wif.write("Player3: " + p3 + " wins");
		wif.write("Player4: " + p4 + " wins");
	}

	public void updateWinnerStat(Winner winner) {
		switch (winner.getId()) {
		case 1:
			p1++;
			wif.write("Player1: " + p1 + " win");
			break;
		case 2:
			p2++;
			wif.write("Player2: " + p2 + " win");
			break;
		case 3:
			p3++;
			wif.write("Player3: " + p3 + " win");
			break;
		case 4:
			p4++;
			wif.write("Player4: " + p4 + " win");
			break;
		}
		if (number < count) {
			config.Settings.PORT++;
			control.Settings.PORT = config.Settings.PORT;
			start();
		} else {
			showResults();
		}
	}

	private void startClients(Server server, int randomSimple, int randomAdvanced, int tryAndError, int ava, int lamb) {
		int sum = randomSimple + randomAdvanced + tryAndError + ava + lamb;
		if (sum > 4) {
			System.out.println("invalid number of players");
		} else {
			server.startGame(sum);
			for (int i = 0; i < randomSimple; i++) {
				Connection connection = new Connection(this);
				new Client(new RandomAISimple(connection), connection).start();
			}
			for (int i = 0; i < randomAdvanced; i++) {
				Connection connection = new Connection(this);
				new Client(new RandomAIAdvanced(connection), connection).start();
			}
			for (int i = 0; i < tryAndError; i++) {
				Connection connection = new Connection(this);
				new Client(new TryAndError(connection), connection).start();
			}
			for (int i = 0; i < ava; i++) {
				Connection connection = new Connection(this);
				new Client(new Ava(connection), connection).start();
			}
			for (int i = 0; i < lamb; i++) {
				Connection connection = new Connection(this);
				new Client(new LAMB(connection), connection).start();
			}
		}
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
