package control.AI;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import model.jaxb.ErrorType;
import model.jaxb.WinMessageType.Winner;
import server.Game;
import server.userInterface.BetterUI;
import tools.LoggedPrintStream;
import tools.WriteIntoFile;
import config.Settings;
import control.AI.Fridolin.Fridolin;
import control.AI.LAMB.LAMB;
import control.AI.MNA.MNA;
import control.AI.MNA_S.MNA_S;
import control.AI.ava.Ava;
import control.AI.ava.Ava2;
import control.network.Connection;

public class AiVsAI {
	// ================ settings ====================
	// number of games
	private final int count = 30;
	// count how many instances the specified ai shall start
	private int hal9000;
	private int randomSimple = 0;
	private int randomAdvanced = 0;
	private int tryAndError = 0;
	private int ava = 0;
	private int lamb = 0;
	private int mna_s = 0;
	private int fridolin = 1;
	private int ava2 = 0;
	private int mna = 1;
	// name of ai's. Value of the specified string can be changed by oneself
	private final String RANDOMSIMPLE = "randomSimple";
	private final String RANDOMADVANCED = "randomAdvanced";
	private final String TRYANDERROR = "tryAndError";
	private final String AVA = "Altbacken";
	private final String LAMB = "Lamb";
	private final String HAL9000 = "hal9000";
	private final String MNA_S = "MNA_S";
	private final String FRIDOLIN = "Fridolin!";
	private final String AVA2 = "Ava 2.0";
	private final String MNA = "MNA";
	// case true: testseed++ each game. so it is possible to find the game where an error occurred
	private final boolean debug = false;
	// =================== end ======================

	private WriteIntoFile wif;
	private WriteIntoFile wif_err;
	private WriteIntoFile wif_out;
	private LoggedPrintStream lps_out;
	private LoggedPrintStream lps_err;
	private static int number = 1;
	private static AiVsAI a;
	private Server server;
	private Map<Integer, PlayerStat> map;
	private List<String> order;
	private int sum;
	private int testseed = 0;
	private int inc = 7;

	private final int H9 = 0;
	private final int RS = 1;
	private final int RA = 2;
	private final int TE = 3;
	private final int AV = 4;
	private final int LA = 5;
	private final int MS = 6;
	private final int FR = 7;
	private final int AV2 = 8;
	private final int MN = 9;
	/**
	 * case false: just one constellation. case true: sum of all ai's which should fight factorial multiply with count
	 */
	private boolean allCombination = true;
	/**
	 * file path for the statistics. port will automatically attached
	 */
	private final String FILENAME = "WinnerStat";

	public static void main(String[] args) {
		a = new AiVsAI();
		a.init();
	}

	public void init() {
		if (debug) {
			config.Settings.TESTBOARD = true;
			config.Settings.TESTBOARD_SEED = testseed;
		} else {
			config.Settings.TESTBOARD = false;
		}
		config.Settings.PORT--;
		lps_out = LoggedPrintStream.create(System.out);
		lps_err = LoggedPrintStream.create(System.err);
		System.setOut(lps_out);
		System.setErr(lps_err);
		refresh();
		wif.writeln("debug Modus: " + debug);
		sum = randomSimple + randomAdvanced + tryAndError + ava + lamb + hal9000 + mna_s + fridolin + ava2;
		if (sum > 0) {
			if (sum > 4) {
				System.err.println("invalid number of players");
			} else {
				order = new ArrayList<>();
				if (hal9000 > 0) {
					if (hal9000 > 1) {
						addMultiply(hal9000, H9);
					} else {
						order.add(H9 + "");
					}
				}
				if (randomSimple > 0) {
					if (randomSimple > 1) {
						addMultiply(randomSimple, RS);
					} else {
						order.add(RS + "");
					}
				}
				if (randomAdvanced > 0) {
					if (randomAdvanced > 1) {
						addMultiply(randomAdvanced, RA);
					} else {
						order.add(RA + "");
					}
				}
				if (tryAndError > 0) {
					if (tryAndError > 1) {
						addMultiply(tryAndError, TE);
					} else {
						order.add(TE + "");
					}
				}
				if (ava > 0) {
					if (ava > 1) {
						addMultiply(ava, AV);
					} else {
						order.add(AV + "");
					}
				}
				if (lamb > 0) {
					if (lamb > 1) {
						addMultiply(lamb, LA);
					} else {
						order.add(LA + "");
					}
				}
				if (mna_s > 0) {
					if (mna_s > 1) {
						addMultiply(mna_s, MS);
					} else {
						order.add(MS + "");
					}
				}
				if (fridolin > 0) {
					if (fridolin > 1) {
						addMultiply(fridolin, FR);
					} else {
						order.add(FR + "");
					}
				}
				if (ava2 > 0) {
					if (ava2 > 1) {
						addMultiply(ava2, AV2);
					} else {
						order.add(AV2 + "");
					}
				}
				if (mna > 0) {
					if (mna > 1) {
						addMultiply(mna, MN);
					} else {
						order.add(MN + "");
					}
				}
				initAllComb(order);
				start();
			}

		}
	}

	/**
	 * starts all possible combinations with the given players. Is just comparable with unique player. So you cannot start an AI multiple.
	 */
	private void initAllComb(List<String> ai) {
		order = new ArrayList<>();
		order = perm(order, ai, "");
	}

	private List<String> perm(List<String> list, List<String> digit, String s) {
		if (s.length() == sum && !list.contains(s)) {
			list.add(s);
			return list;
		}
		for (int i = 0; i < digit.size(); i++) {
			String f = s;
			List<String> copy_digit = new ArrayList<String>(digit);
			f += copy_digit.get(i);
			copy_digit.remove(i);
			list = perm(list, copy_digit, f);
		}
		return list;
	}

	private void start() {
		number++;
		server = new Server();
		server.start();
		System.err.println("\n");
		System.err.println("Game No. " + number + "\t(" + Settings.PORT + " " + config.Settings.TESTBOARD_SEED + ")");
		System.out.println("\n");
		System.out.println("Game No. " + number + " (" + Settings.PORT + " " + config.Settings.TESTBOARD_SEED + ")");
		startClients(server);
	}

	private void showResults() {
		wif.writeNewLine(2);
		wif.writeln(System.nanoTime() + "");
		wif.writeln("-----------Stats--------------");
		for (Integer key : map.keySet()) {
			wif.writeln("Player" + key + " (" + map.get(key).getName() + "): " + map.get(key).getWins() + " wins");
		}
	}

	public static void updateWinnerStat(Winner winner) {
		a.update(winner);
	}

	private void update(Winner winner) {
		PlayerStat ps = map.get(winner.getId()).incWins();
		map.put(winner.getId(), ps);
		wif.writeln("no." + number + "\tTS:" + config.Settings.TESTBOARD_SEED + "\tPlayer" + winner.getId() + "(" + winner.getValue() + "): " + map.get(winner.getId()).getWins()
				+ ". win");
		wif_err.clearFile();
		wif_out.clearFile();
		wif_err.writeln(lps_err.getBuf().toString());
		wif_out.writeln(lps_out.getBuf().toString());

		if (number < count) {
			if (debug) {
				config.Settings.TESTBOARD_SEED += inc;
			}
			config.Settings.PORT++;
			control.Settings.PORT = config.Settings.PORT;
			start();
		} else {
			if (debug) {
				config.Settings.TESTBOARD_SEED = testseed;
			}
			showResults();
			order.remove(0);
			if (order.size() == 0) {
				allCombination = false;
			}
			if (allCombination) {
				refresh();
				start();
			}
		}
	}

	private void refresh() {
		number = 0;
		config.Settings.PORT++;
		control.Settings.PORT = config.Settings.PORT;
		wif = new WriteIntoFile(FILENAME + "_" + config.Settings.PORT + WriteIntoFile.FILEEXTENSION);
		wif.clearFile();
		wif_err = new WriteIntoFile("Output_err_" + config.Settings.PORT + WriteIntoFile.FILEEXTENSION);
		wif_err.clearFile();
		wif_out = new WriteIntoFile("Output_out_" + config.Settings.PORT + WriteIntoFile.FILEEXTENSION);
		wif_out.clearFile();
		map = new HashMap<>();
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
		wif.writeln(System.nanoTime() + "\tPlayer " + id + " has a disconnect. Reason: " + error.toString());
	}

	private void startClients(Server server) {
		server.startGame(sum);
		try {
			TimeUnit.SECONDS.sleep(7);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String s = order.get(0);
		int tmp = 0;
		for (int j = 0; j < s.length(); j++) {
			Connection connection;
			switch (Integer.parseInt(s.charAt(j) + "")) {
			case H9:
				System.out.println("Starting HAL9000...");
				System.out.println(MonoStarter.startHAL9000(config.Settings.PORT));
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.HAL9000));
				}
				break;
			case RS:
				connection = new Connection(this);
				new Client(new RandomAISimple(connection), connection).start();
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.RANDOMSIMPLE));
				}
				break;
			case RA:
				connection = new Connection(this);
				new Client(new RandomAIAdvanced(connection), connection).start();
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.RANDOMADVANCED));
				}
				break;
			case TE:
				connection = new Connection(this);
				new Client(new TryAndError(connection), connection).start();
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.TRYANDERROR));
				}
				break;
			case AV:
				connection = new Connection(this);
				new Client(new Ava(connection), connection).start();
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.AVA));
				}
				break;
			case LA:
				connection = new Connection(this);
				new Client(new LAMB(connection), connection).start();
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.LAMB));
				}
				break;
			case MS:
				connection = new Connection(this);
				new Client(new MNA_S(connection), connection).start();
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.MNA_S));
				}
				break;
			case FR:
				connection = new Connection(this);
				new Client(new Fridolin(connection), connection).start();
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.FRIDOLIN));
				}
				break;
			case AV2:
				connection = new Connection(this);
				new Client(new Ava2(connection), connection).start();
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.AVA2));
				}
				break;
			case MN:
				connection = new Connection(this);
				new Client(new MNA(connection), connection).start();
				if (number == 1) {
					tmp++;
					map.put(tmp, new PlayerStat(this.MNA));
				}
				break;
			}
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
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

	private void addMultiply(int count, int ai) {
		for (int i = 0; i < count; i++) {
			order.add(ai + "");
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

		public PlayerStat incWins() {
			++wins;
			return this;
		}

		public String toString() {
			return name + ": " + wins;
		}

	}

}
