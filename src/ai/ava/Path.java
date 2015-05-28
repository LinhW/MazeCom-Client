package ai.ava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Path {
	private List<List<Integer>> path = new ArrayList<List<Integer>>();

	public Path(int[][] path) {
		for (int i = 0; i < path.length; i++) {
			List l = Arrays.asList(path[i]);
			this.path.add(l);
		}
	}

	public String toString() {
		String s = "";
		for (List<Integer> l : path) {
			
		}
		return s;
	}
}
