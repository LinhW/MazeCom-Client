package gui;

import java.util.Map;
import java.util.HashMap;

public class Context {
	// define keys for map
	public static final String USER = "user";
	public static final String BOARD = "board";
	public static final String TREASURELIST = "treasurelist";
	public static final String KEYEVENTS = "keyevents";
	public static final String ROTATE_LEFT = "Rotate counterclockwise";
	public static final String ROTATE_RIGHT = "Rotate clockwise";
	public static final String UP = "up";
	public static final String DOWN = "down";
	public static final String LEFT = "left";
	public static final String RIGHT = "right";

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
