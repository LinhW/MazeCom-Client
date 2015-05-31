package control.AI.ava;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Context {
	public static final String FILEPATH = "src/control/AI/ava/tmp.txt";
	public static final String FILE = "file";
	private static Context instance;
	private Map<String, Object> map;

	private Context() {
		map = new HashMap<String, Object>();
		map.put(FILE, new File(FILEPATH));
	}

	public static Context getInstance() {
		if (instance == null) {
			instance = new Context();
		}
		return instance;
	}

	public Object getValue(String key) {
		return map.get(key);
	}

	public Object setValue(String key, Object value) {
		return map.put(key, value);
	}

}
