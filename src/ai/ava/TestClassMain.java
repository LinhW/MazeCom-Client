package ai.ava;

import gui.data.Board;
import gui.data.Position;
import jaxb.PositionType;
import ai.ava.Pathfinding.CardHelp;

public class TestClassMain {
	private static final int playerID = 1;
	private static int[][] path;

	public static void main(String[] args) {
		System.out.println("Test for Ava");

		Board b = new Board();
		Position myPinPos = new Position(b.findPlayer(playerID));
		Position myTreasurePos = new Position(6, 4);
		
		System.out.println(b.getShiftCard());
		System.out.println(b);

		System.out.println("PinPos: " + myPinPos + "  TreasurePos: " + myTreasurePos);
		CardHelp ch = Pathfinding.calcMove(b, myPinPos, b.getTreasure(), playerID);
		if (ch == null) {
			System.out.println(ch);
		} else {
			System.out.println(ch.getC()+ " " + ch.getP());
		}
		
		for (PositionType p: b.getAllReachablePositions(myPinPos)){
			System.out.println("pos " + new Position(p));
		}
		// path = Pathfinding.findPath(b, myPinPos, myTreasurePos);
		//
		// Path p = new Path(path);
		// System.out.println(p);
		// for (Neighbour n : p.getNeighbours(1, 2)) {
		// System.out.println(n);
		// }
		// System.out.println();

		myPinPos = new Position(6, 6);
		myTreasurePos = new Position(3, 3);
		System.out.println("\nPinPos: " + myPinPos + "  TreasurePos: " + myTreasurePos);
		ch = Pathfinding.calcMove(b, myPinPos, b.getTreasure(), playerID);
		if (ch == null) {
			System.out.println(ch);
		} else {
			System.out.println(ch.getC()+ " " + ch.getP());
		}
		
		for (PositionType p: b.getAllReachablePositions(myPinPos)){
			System.out.println("pos " + new Position(p));
		}
		// path = Pathfinding.findPath(b, myPinPos, myTreasurePos);
		//
		// p = new Path(path);
		// System.out.println(p);
		// for (Neighbour n : p.getNeighbours(1, 2)) {
		// System.out.println(n);
		// }
	}

}
