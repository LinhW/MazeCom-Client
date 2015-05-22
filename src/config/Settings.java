package config;

import java.util.Locale;

import tools.DebugLevel;
import view.testClasses.userInterface.GUI;
import view.testClasses.userInterface.UI;

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
	 * USERINTERFACE definiert die zu verwendende GUI Gueltige Werte:
	 * BetterUI(), GraphicalUI()
	 */
	public final static UI USERINTERFACE = new GUI();
	/**
	 * Auf das angehaengte / achten
	 */
	public final static String IMAGEPATH = "/view/resources/"; //$NON-NLS-1$
	public final static String IMAGEFILEEXTENSION = ".png"; //$NON-NLS-1$
	public final static Locale LOCALE = new Locale("de"); //$NON-NLS-1$
	/**
	 * Den Detailgrad der Ausgaben festlegen
	 */
	public final static DebugLevel DEBUGLEVEL = DebugLevel.DEFAULT;
}