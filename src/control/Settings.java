package control;

import java.util.Locale;

import tools.DebugLevel;

public class Settings {
	private Settings() {
	}

	/**
	 * Die Zeit in Milisekunden, die die Animation eines Zug (die Bewegung des
	 * Pins) benoetigen soll
	 */
	public final static int MOVEDELAY = 10;
	/**
	 * Die Zeit in Milisekunden, die das Einschieben der Shiftcard dauern soll
	 */
	public final static int SHIFTDELAY = 10;
	/**
	 * Wenn TESTBOARD = true ist, dann ist das Spielbrett bei jedem Start
	 * identisch (zum Debugging)
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
	public final static Locale LOCALE = new Locale("de"); //$NON-NLS-1$
	/**
	 * Den Detailgrad der Ausgaben festlegen
	 */
	public final static DebugLevel DEBUGLEVEL = DebugLevel.DEFAULT;
	
	public final static String[] AIList = {"Random AI", "Ava", "Random AI advanced", "Try And Error"};
}
