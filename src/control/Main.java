package control;

public class Main {
	private static final boolean debug = true;
	
	private Controller controller;
	
	private void run() {
		// Create controller
		controller = new Controller();
		// der controller sollte wahrscheinlich ganz oben sitzen, dann kann man �ber den die
		// notifications zwischen fenster und verbindung schicken
		controller.createWindow();
		
		// Debug code begin -----------------------------------------------------------------------
		if (debug) {
			controller.setName("Test");
			System.out.println(controller.connect("localhost", 5123));
		}
		// Debug code end -------------------------------------------------------------------------
	}
	
	public static void main(String[] args) {
		Main m = new Main();
		m.run();
	}
}
