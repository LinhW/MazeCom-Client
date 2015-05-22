package view.data;

import java.util.Map;
import java.util.HashMap;

public class Context {
	// define keys for map
	public static final String USER = "user";
	public static final String BOARD = "board";

	private static Context instance;
	private Map<String, Object> map;

	private Context() {
		map = new HashMap<String, Object>();
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
