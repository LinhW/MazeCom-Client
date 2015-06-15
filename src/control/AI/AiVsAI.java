package control.AI;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import model.jaxb.ErrorType;
import model.jaxb.WinMessageType.Winner;
import server.Game;
import server.userInterface.BetterUI;
import config.Settings;
import control.AI.LAMB.LAMB;
import control.AI.ava.Ava;
import control.AI.ava.WriteIntoFile;
import control.network.Connection;

public class AiVsAI {
	private WriteIntoFile wif;
	private static int number = 0;
	private static AiVsAI a;
	private WriteIntoFile wif_error;
	private Server server;
	private Map<Integer, PlayerStat> map;

	// name of ai's. Value of the specified string can be changed by oneself
	private final String HAL9000 = "hal9000";
	private final String RANDOMSIMPLE = "randomSimple";
	private final String RANDOMADVANCED = "randomAdvanced";
	private final String TRYANDERROR = "TryAndError";
	private final String AVA = "Humpf";
	private final String LAMB = "Lamb";
	/**
	 * number of games
	 */
	private final int count = 10;
	/**
	 * file path for the statistics
	 */
	private final String FILEPATH = "WinnerStat" + WriteIntoFile.FILEEXTENSION;

	public static void main(String[] args) {
		a = new AiVsAI();
		a.init();
	}

	public void init() {
		wif = new WriteIntoFile(FILEPATH);
		wif.clearFile();
		wif_error = new WriteIntoFile(WriteIntoFile.FILEPATH + "_error" + WriteIntoFile.FILEEXTENSION);
		wif_error.clearFile();
		map = new HashMap<>();
		start();
	}

	public void start() {
		number++;
		server = new Server();
		server.start();
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		startClients(server, 0, 0, 0, 1, 1, 1);
	}

	private void showResults() {
		wif.writeNewLine(2);
		wif.write(System.nanoTime() + "");
		wif.write("-----------Stats--------------");
		for (Integer key : map.keySet()) {
			wif.write("Player1 (" + map.get(key).getName() + "): " + map.get(key).getWins() + " wins");
		}
	}

	public static void updateWinnerStat(Winner winner) {
		a.update(winner);
	}

	private void update(Winner winner) {
		map.get(winner.getId()).incWins();
		wif.write(System.nanoTime() + "\tPlayer" + winner.getId() + ": " + map.get(winner.getId()).getWins() + ". win");
		if (number < count) {
			config.Settings.PORT++;
			control.Settings.PORT = config.Settings.PORT;
			start();
		} else {
			showResults();
		}
	}

	// TODO an error occurred
	public void stop() {
		server.stopGame();
		start();
	}

	public static void disconnect(ErrorType error, int id) {
		a.dc(error, id);
	}

	private void dc(ErrorType error, int id) {
		wif.write(System.nanoTime() + "\tPlayer " + id + " has a disconnect. Reason: " + error.toString());
	}

	private void startClients(Server server, int randomSimple, int randomAdvanced, int tryAndError, int ava, int lamb, int hal9000) {
		int sum = randomSimple + randomAdvanced + tryAndError + ava + lamb + hal9000;
		if (sum > 4) {
			System.err.println("invalid number of players");
		} else {
			server.startGame(sum);
			int tmp = 0;
			for (int i = 0; i < hal9000; i++) {
				System.out.println("Starting HAL9000...");
				System.out.println(MonoStarter.startHAL9000(config.Settings.PORT));
				tmp++;
				map.put(tmp, new PlayerStat(this.HAL9000));
			}
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < randomSimple; i++) {
				Connection connection = new Connection(this);
				new Client(new RandomAISimple(connection), connection).start();
				tmp++;
				map.put(tmp, new PlayerStat(this.RANDOMSIMPLE));
			}
			for (int i = 0; i < randomAdvanced; i++) {
				Connection connection = new Connection(this);
				new Client(new RandomAIAdvanced(connection), connection).start();
				tmp++;
				map.put(tmp, new PlayerStat(this.RANDOMADVANCED));
			}
			for (int i = 0; i < tryAndError; i++) {
				Connection connection = new Connection(this);
				new Client(new TryAndError(connection), connection).start();
				tmp++;
				map.put(tmp, new PlayerStat(this.TRYANDERROR));
			}
			for (int i = 0; i < ava; i++) {
				Connection connection = new Connection(this);
				new Client(new Ava(connection), connection).start();
				tmp++;
				map.put(tmp, new PlayerStat(this.AVA));
			}
			for (int i = 0; i < lamb; i++) {
				Connection connection = new Connection(this);
				new Client(new LAMB(connection), connection).start();
				tmp++;
				map.put(tmp, new PlayerStat(this.LAMB));
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

	private class PlayerStat {
		private String name;
		private int wins;

		public PlayerStat(String name) {
			this.setName(name);
			this.setWins(0);
		}

		public String getName() {
			return name;
		}

		private void setName(String name) {
			this.name = name;
		}

		public int getWins() {
			return wins;
		}

		private void setWins(int wins) {
			this.wins = wins;
		}

		public void incWins() {
			this.wins++;
		}

	}

}
