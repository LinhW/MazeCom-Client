package control.AI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MonoStarter {
	
	//Hier Pfad zu der exe eintragen
	public static String path = "/src/C#/MazeNetClient/MazeNetClient/bin/Debug/";
	
	public static String startHAL9000(int port)
	{
		String res = "";
		try {
			String rpath = new File(".").getCanonicalPath();
			
			ProcessBuilder pb = new ProcessBuilder("mono",
					"MazeNetClient.exe",
					"" + port
					);
			pb.directory(new File(rpath + path));
			Process proc = pb.start();
		
			 StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "HAL 9000 ERROR");

			// any output?
			 StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "HAL 9000 OUTPUT");

			// start gobblers
			 outputGobbler.start();
			 errorGobbler.start();
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Bitte Pfad in \"control.AI.MonoStarter.java\" kontrollieren");
			res += e.getMessage();
		}
		return res;
	}
}

class StreamGobbler extends Thread {
    InputStream is;
    String type;

    StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
                System.out.println(type + "> " + line);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}