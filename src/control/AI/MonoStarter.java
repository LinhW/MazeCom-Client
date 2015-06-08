package control.AI;

import java.io.IOException;

public class MonoStarter {
	public static void startHAL9000()
	{
		try {
			Process proc = Runtime.getRuntime().exec("mono MazeNetClient.exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
