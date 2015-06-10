package control.AI.LAMB;

import java.util.List;

import model.Board;
import model.Position;
import model.jaxb.TreasuresToGoType;

public class Assist {
	public enum Side {UP, RIGHT, DOWN, LEFT}
	public enum Points {
		OWN_START(Integer.MAX_VALUE),
		OWN_TARGET(100),
		TARGET_MISSING(-10),
		OTHER_START_OPEN(-50),
		OTHER_TREASURE_REACHABLE(-25);
		
		private final int value;
		
		private Points(int v) {
			this.value = v;
		}
		
		public int value() {
			return value;
		}
	}
	
	public static int calculateBoardValue(int playerID, Board board, List<TreasuresToGoType> ttgo) {
		int boardValue = 0;
		
		return boardValue;
	}
	
	public static int calculatePositionValue(int playerID, Board board, Position position) {
		int positionValue = 0;
		
		return positionValue;
	}
}
