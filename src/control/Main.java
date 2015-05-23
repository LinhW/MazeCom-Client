package control;

import view.testClasses.Tmp_testGUI;

public class Main {
	private static final boolean debug = true;
	
	private Controller controller;
	
	private void run() {
		//erzeugt ein Messagedialog und gibt den eingegeben namen zurueck
		Tmp_testGUI.first();

		// Create controller
		controller = new Controller();
		// der controller sollte wahrscheinlich ganz oben sitzen, dann kann man über den die
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
