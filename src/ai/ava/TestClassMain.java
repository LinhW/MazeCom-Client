package ai.ava;

import gui.data.Board;
import gui.data.Position;
import ai.ava.Path.Neighbour;

public class TestClassMain {
	private static final int playerID = 1;
	private static int[][] path;

	public static void main(String[] args) {
		System.out.println("Test for Ava");
		Board b = new Board();
		Position myPinPos = new Position(b.findPlayer(playerID));
		Position myTreasurePos = new Position(6, 4);

		System.out.println("PinPos: " + myPinPos + "  TreasurePos: " + myTreasurePos);
		path = Pathfinding.findPath(b, myPinPos, myTreasurePos);

		Path p = new Path(path);
		System.out.println(p);
		for (Neighbour n : p.getNeighbours(1, 2)) {
			System.out.println(n);
		}
		System.out.println();
		
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				System.out.print(p.get(i, j));
			}
			System.out.println();
		}

		myPinPos = new Position(6, 6);
		myTreasurePos = new Position(3, 3);
		System.out.println("PinPos: " + myPinPos + "  TreasurePos: " + myTreasurePos);
		path = Pathfinding.findPath(b, myPinPos, myTreasurePos);

		p = new Path(path);
		System.out.println(p);
		for (Neighbour n : p.getNeighbours(1, 2)) {
			System.out.println(n);
		}
	}

}
