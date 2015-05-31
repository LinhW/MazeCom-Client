package control.AI.ava;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteIntoFile {
	private File file;
	private BufferedWriter bwriter;
	public static final String FILEPATH = "src/control/AI/ava/tmp.txt";

	public WriteIntoFile(String filepath) {
		file = new File(filepath);
	}

	public boolean clearFile() {
		try {
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean write(String content) {
		try {
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			bwriter = new BufferedWriter(fw);
			bwriter.write(content);
			bwriter.newLine();
			bwriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
