package control;

import tools.DebugLevel;

public class Settings {
	private Settings() {
	}

	public static int PORT = 5123;

	/**
	 * Die Zeit in Milisekunden, die die Animation eines Zug (die Bewegung des Pins) benoetigen soll
	 */
	public final static int MOVEDELAY = 1000;
	/**
	 * Die Zeit in Milisekunden, die das Einschieben der Shiftcard dauern soll
	 */

	public final static int SHIFTDELAY = 1000;
	/**
	 * Wenn TESTBOARD = true ist, dann ist das Spielbrett bei jedem Start identisch (zum Debugging)
	 */
	public final static boolean TESTBOARD = true;
	/**
	 * Hiermit lassen sich die Testfaelle anpassen (Pseudozufallszahlen)
	 */
	public final static long TESTBOARD_SEED = 0;
	/**
	 * Auf das angehaengte / achten
	 */
	public final static String IMAGEPATH = "/gui/resources/"; //$NON-NLS-1$
	public final static String IMAGEFILEEXTENSION = ".png"; //$NON-NLS-1$
	//	public final static Locale LOCALE = new Locale("de"); //$NON-NLS-1$
	/**
	 * Den Detailgrad der Ausgaben festlegen
	 */
	final static DebugLevel DEBUGLEVEL = DebugLevel.DEFAULT;

	public final static String[] AIList = { "Random AI", "Ava", "Random AI advanced", "Try And Error", "LAMB", "Fridolin", "MNA_S"};
}
