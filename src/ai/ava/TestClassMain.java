package ai.ava;

import gui.data.Board;
import gui.data.Position;

public class TestClassMain {
	private static final int playerID = 1;
	private static int[][] path;

	public static void main(String[] args) {
		System.out.println("Test for Ava");
		Board b = new Board();
		Position myPinPos = new Position(b.findPlayer(playerID));
		Position myTreasurePos = new Position(6, 4);

		System.out.println(b.getRow().size() + "/" + b.getRow().get(0).getCol().size());

		// int[][] weg = Pathfinding.findShortestPath(b, myPinPos,
		// myTreasurePos);
		//
		// for (int i = 0; i < weg.length; i++) {
		// for (int j = 0; j < weg[0].length; j++) {
		// System.out.print(weg[i][j]);
		// }
		// System.out.println();
		// }

		System.out.println("PinPos: " + myPinPos + "  TreasurePos: " + myTreasurePos);
		path = Pathfinding.findPath(b, myPinPos, myTreasurePos);
		for (int i = 0; i < path.length; i++) {
			for (int j = 0; j < path[0].length; j++) {
				System.out.print(path[i][j]);
			}
			System.out.println();
		}

		myPinPos = new Position(6, 6);
		myTreasurePos = new Position(3, 3);
		System.out.println("PinPos: " + myPinPos + "  TreasurePos: " + myTreasurePos);
		path = Pathfinding.findPath(b, myPinPos, myTreasurePos);
		for (int i = 0; i < path.length; i++) {
			for (int j = 0; j < path[0].length; j++) {
				System.out.print(path[i][j]);
			}
			System.out.println();
		}
	}

}
