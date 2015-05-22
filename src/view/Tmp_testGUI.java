package view;

import jaxb.MazeCom;

public class Tmp_testGUI {

	public static void receiveServerMessage(MazeCom o) {
		System.out.println("receive Message: " + o.getId() + o.getMcType());

	}
}
