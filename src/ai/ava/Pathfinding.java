package ai.ava;

import gui.data.Board;
import gui.data.Position;

public class Pathfinding {
	public static int[][] findShortestPath(Board b, Position startPos, Position endPos) {

		// Dijkstra
		boolean[][] visited = new boolean[7][7];
		int[][] weglen = new int[7][7];
		int[][] pfad = new int[7][7];
		for (int y = 0; y < 7; ++y) {
			for (int x = 0; x < 7; ++x) {
				weglen[y][x] = Integer.MAX_VALUE;
			}
		}

		int currentX = startPos.getCol();
		int currentY = startPos.getRow();
		weglen[currentY][currentX] = 0;
		while (true) {
			visited[currentY][currentX] = true;
			if (currentX > 0 && b.getCard(currentY, currentX).getOpenings().isLeft() && b.getCard(currentY, currentX - 1).getOpenings().isRight()) {
				if (weglen[currentY][currentX - 1] > weglen[currentY][currentX] + 1) {
					weglen[currentY][currentX - 1] = weglen[currentY][currentX] + 1;
					pfad[currentY][currentX - 1] = currentY * 7 + currentX;
				}
			}
			if (currentY > 0 && b.getCard(currentY, currentX).getOpenings().isTop() && b.getCard(currentY - 1, currentX).getOpenings().isBottom()) {
				if (weglen[currentY - 1][currentX] > weglen[currentY][currentX] + 1) {
					weglen[currentY - 1][currentX] = weglen[currentY][currentX] + 1;
					pfad[currentY - 1][currentX] = currentY * 7 + currentX;
				}
			}

			if (currentX < 6 && b.getCard(currentY, currentX).getOpenings().isRight() && b.getCard(currentY, currentX + 1).getOpenings().isLeft()) {
				if (weglen[currentY][currentX + 1] > weglen[currentY][currentX] + 1) {
					weglen[currentY][currentX + 1] = weglen[currentY][currentX] + 1;
					pfad[currentY][currentX + 1] = currentY * 7 + currentX;
				}
			}
			if (currentY < 6 && b.getCard(currentY, currentX).getOpenings().isBottom() && b.getCard(currentY + 1, currentX).getOpenings().isTop()) {
				if (weglen[currentY + 1][currentX] > weglen[currentY][currentX] + 1) {
					weglen[currentY + 1][currentX] = weglen[currentY][currentX] + 1;
					pfad[currentY + 1][currentX] = currentY * 7 + currentX;
				}
			}

			{
				int currentMinWegLen = Integer.MAX_VALUE;
				for (int y = 6; y >= 0; --y) {
					for (int x = 6; x >= 0; --x) {
						if (!visited[y][x] && weglen[y][x] < currentMinWegLen) {
							currentMinWegLen = weglen[y][x];
							currentX = x;
							currentY = y;
						}
					}
				}
				if (currentMinWegLen == Integer.MAX_VALUE)
					break;
			}
		}
		currentX = endPos.getCol();
		currentY = endPos.getRow();
		int anzahlWegpunkte = weglen[currentY][currentX] + 1;
		// Weg ist ein Array von x und y werten
		int weg[][] = new int[anzahlWegpunkte][2];
		int i = anzahlWegpunkte - 1;
		while (i > 0) {
			weg[i--] = new int[] { currentX, currentY };
			int buf = pfad[currentY][currentX];
			currentX = buf % 7;
			currentY = buf / 7;
		}
		weg[0] = new int[] { currentX, currentY };
		return weg;
	}

}
