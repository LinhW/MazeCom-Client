using System;
using System.Threading;
using System.Threading.Tasks;
using System.Collections.Generic;
using System.Collections.Concurrent;
using System.Linq;
using System.Text;

namespace MazeNetClient
{
	public static class MazeNetUtil
	{
		public static MazeNetPosition[][] boardToMatrix(boardType board)
		{
			MazeNetPosition[][] res = new MazeNetPosition[board.row.Length][];
			for (int i = 0; i < board.row.Length; i++) {
				for (int j = 0; j < board.row[i].col.Length; j++) {
					// i ist x
					// j ist y
					if (res [j] == null) {
						res [j] = new MazeNetPosition[board.row.Length];
					}
					res [j][i] = new MazeNetPosition(j,i,board.row [i].col [j]);
					if (res[j][i].Card.pin.Length > 0 && res[j][i].Card.pin[0] == 0) {
						
					}
				}
			}
			return res;
		}

		public static String MatrixToString(MazeNetPosition[][] matrix)
		{
			String[][] stringMat = new string[matrix.Length * 3][];
			for (int i = 0; i < stringMat.Length; i++) {
				stringMat[i] = new string[matrix.Length * 3];
			}
			for (int i = 0; i < matrix.Length; i++) {
				for (int j = 0; j < matrix.Length; j++) {
					int x = i * 3;
					int y = j * 3;
					stringMat [x] [y] = "# ";
					stringMat [x + 2] [y] = "# ";
					stringMat [x] [y + 2] = "# ";
					stringMat [x + 2] [y + 2] = "# ";
					if (matrix [i] [j].Card.pin.Length > 0) {
						stringMat [x + 1] [y + 1] = matrix [i] [j].Card.pin[0].ToString();
					} else {
						stringMat [x + 1] [y + 1] = " ";
					}
					if (matrix [i] [j].Card.treasureSpecified) {
						stringMat [x + 1] [y + 1] += "T";
					} else {
						stringMat [x + 1] [y + 1] += " ";
					}

					if (matrix [i] [j].Card.openings.top) {
						stringMat [x + 1] [y] = "  ";
					} else {
						stringMat [x + 1] [y] = "# ";
					}
					if (matrix [i] [j].Card.openings.right) {
						stringMat [x + 2] [y + 1] = "  ";
					} else {
						stringMat [x + 2] [y + 1] = "# ";
					}
					if (matrix [i] [j].Card.openings.left) {
						stringMat [x] [y + 1] = "  ";
					} else {
						stringMat [x] [y + 1] = "# ";
					}
					if (matrix [i] [j].Card.openings.bottom) {
						stringMat [x + 1] [y + 2] = "  ";
					} else {
						stringMat [x + 1] [y + 2] = "# ";
					}
				}
			}
			StringBuilder sb = new StringBuilder ();
			for (int i = 0; i < stringMat.Length; i++) {
				for (int j = 0; j < stringMat.Length; j++) {
					sb.Append (stringMat [j] [i]);
				}
				sb.Append ("\n");
			}
			return sb.ToString ();
		}

		public static String posToString(MazeNetPosition pos)
		{
			String[][] res = new string[3][];
			for (int i = 0; i < res.Length; i++) {
				res [i] = new string[3];
			}
			int x = 0;
			int y = 0;
			res [x] [y] = "# ";
			res [x + 2] [y] = "# ";
			res [x] [y + 2] = "# ";
			res [x + 2] [y + 2] = "# ";
			if (pos.Card.pin != null && pos.Card.pin.Contains (1)) {
				res [x + 1] [y + 1] = "1 ";
			} else {
				res [x + 1] [y + 1] = "  ";
			}
			if (pos.Card.openings.top) {
				res [x + 1] [y] = "  ";
			} else {
				res [x + 1] [y] = "# ";
			}
			if (pos.Card.openings.right) {
				res [x + 2] [y + 1] = "  ";
			} else {
				res [x + 2] [y + 1] = "# ";
			}
			if (pos.Card.openings.left) {
				res [x] [y + 1] = "  ";
			} else {
				res [x] [y + 1] = "# ";
			}
			if (pos.Card.openings.bottom) {
				res [x + 1] [y + 2] = "  ";
			} else {
				res [x + 1] [y + 2] = "# ";
			}
			StringBuilder sb = new StringBuilder ();
			for (int i = 0; i < res.Length; i++) {
				for (int j = 0; j < res.Length; j++) {
					sb.Append (res [j] [i]);
				}
				sb.Append ("\n");
			}
			return sb.ToString ();
		}

		public static MazeNetPosition getPlayerPosition(int playerId, MazeNetPosition[][] board)
		{
			foreach (var cards in board) {
				foreach (var card in cards) {
					if (card.Card.pin != null) {
						foreach (var pins in card.Card.pin) {
							if (pins == playerId) {
								return card;
							}
						}
					}
				}
			}
			return null;
		}

		public static MazeNetPosition getTreasurePosition(treasureType treasure, MazeNetPosition[][] board)
		{
			foreach (var cards in board) {
				foreach (var card in cards) {
					if (card.Card.treasure == treasure) {
						return card;
					}
				}
			}
			return null;
		}

		public static double getDistance(MazeNetPosition p1, MazeNetPosition p2)
		{
			return Math.Sqrt (Math.Pow (p2.X - p1.X, 2) + Math.Pow (p2.Y - p1.Y, 2));
		}

		public static MazeNetPosition[][] Shift(int playerID, MazeNetPosition[][] board, cardType card, MazeNetPosition pos)
		{
			//MatrixLayout
			//	 |0|1|2|3|x
			//	0|_|_|_|_| 
			//	1|_|_|_|_|
			//	2|_|_|_|_|
			//	3|_|_|_|_|
			//	y

			if ((pos.X + 1) % 2 == 0 || (pos.Y + 1) % 2 == 0) {
				if (pos.X == 0) {
					for (int i = board.Length - 1; i > 0; i--) {
						board [i] [pos.Y] = board [i - 1] [pos.Y];
						board [i] [pos.Y].X = i;
					}
					board [0] [pos.Y] = new MazeNetPosition (pos.X, pos.Y, card);
				} else if (pos.Y == 0) {
					for (int i = board.Length - 1; i > 0; i--) {
						board [pos.X] [i] = board [pos.X] [i - 1];
						board [pos.X] [i].Y = i;
					}
					board [pos.X] [0] = new MazeNetPosition (pos.X, pos.Y, card);
				} else if (pos.X == board.Length - 1) {
					for (int i = 0; i < board.Length - 1; i++) {
						board [i] [pos.Y] = board [i + 1] [pos.Y];
						board [i] [pos.Y].X = i;
					}
					board [pos.X] [pos.Y] = new MazeNetPosition (pos.X, pos.Y, card);
				} else if (pos.Y == board.Length - 1) {
					for (int i = 0; i < board.Length - 1; i++) {
						board [pos.X] [i] = board [pos.X] [i + 1];
						board [pos.X] [i].Y = i;
					}
					board [pos.X] [pos.Y] = new MazeNetPosition (pos.X, pos.Y, card);
				} else {
					return null;
				}
			} else {
				return null;
			}
			for (int i = 1; i < 5; i++) {
				if (MazeNetUtil.getPlayerPosition(i, board) == null) {
					board [pos.X] [pos.Y].addPin (i);
				}
			}
			return board;
		}

		public static MazeNetPosition rotateRigth(MazeNetPosition pos)
		{
			bool tmpBot = false;
			bool tmpTop = false;
			bool tmpLeft = false;
			bool tmpRight = false;

			tmpBot = pos.Card.openings.right;
			tmpRight = pos.Card.openings.top;
			tmpTop = pos.Card.openings.left;
			tmpLeft = pos.Card.openings.bottom;

			pos.Card.openings.right = tmpRight;
			pos.Card.openings.left = tmpLeft;
			pos.Card.openings.top = tmpTop;
			pos.Card.openings.bottom = tmpBot;

			return pos;
		}

		public static MazeNetMessage getMoveMessage(MazeNetPosition pinPosition, MazeNetPosition insertionCard)
		{
			MoveMessageType move = new MoveMessageType ();
			positionType pinPos = pinPosition.getPositionType ();
			positionType insertionPos = insertionCard.getPositionType ();
			move.newPinPos = pinPos;
			move.shiftCard = insertionCard.Card;
			move.shiftPosition = insertionPos;
			MazeNetMessage msg = new MazeNetMessage ();
			MazeCom com = new MazeCom ();
			com.Item = move;
			com.mcType = MazeComType.MOVE;
			msg.mazeCom = com;
			return msg;
		}

		public static bool CalculateAnnoyingness(MazeNetPosition[][] board, MazeNetPosition shiftCard)
		{
			if (shiftCard.X == 0) {
				for (int i = board.Length - 1; i > 0; i--) {
					if (board [i] [shiftCard.Y].Card.pin.Length > 0) {
						return true;
					}
				}
			} else if (shiftCard.Y == 0) {
				for (int i = board.Length - 1; i > 0; i--) {
					if (board [shiftCard.X] [i].Card.pin.Length > 0) {
						return true;
					}
				}
			} else if (shiftCard.X == board.Length - 1) {
				for (int i = 0; i < board.Length - 1; i++) {
					if (board [i] [shiftCard.Y].Card.pin.Length > 0) {
						return true;
					}
				}
			} else if (shiftCard.Y == board.Length - 1) {
				for (int i = 0; i < board.Length - 1; i++) {
					if (board [shiftCard.X] [i].Card.pin.Length > 0) {
						return true;
					}
				}
			}
			return false;
		}

		public static MazeNetMessage getMoveMessage(MazeNetPosition pinPosition, MazeNetPosition insertionCard, MazeNetPosition[][] board, double d)
		{
			MazeNetMessage move = getMoveMessage (pinPosition, insertionCard);
			move.board = MazeNetUtil.deepCloneBoard (board);
			move.PlayerTreasureDistance = d;
			return move;
		}

		public static MazeNetPosition deepCloneMCard(MazeNetPosition pos)
		{
			cardType tmpCard = new cardType ();
			cardTypeOpenings tmpOpenings = new cardTypeOpenings ();
			tmpOpenings.bottom = pos.Card.openings.bottom;
			tmpOpenings.left = pos.Card.openings.left;
			tmpOpenings.right = pos.Card.openings.right;
			tmpOpenings.top = pos.Card.openings.top;
			tmpCard.openings = tmpOpenings;
			tmpCard.treasure = pos.Card.treasure;
			tmpCard.treasureSpecified = pos.Card.treasureSpecified;
			int[] pins = new int[pos.Card.pin.Length];
			for (int x = 0; x < pos.Card.pin.Length; x++) {
				pins [x] = pos.Card.pin [x];
			}
			tmpCard.pin = pins;
			MazeNetPosition tmpPos = new MazeNetPosition (pos.X, pos.Y, tmpCard);
			return tmpPos;
		}

		public static MazeNetPosition[][] deepCloneBoard(MazeNetPosition[][] board)
		{
			MazeNetPosition[][] res = new MazeNetPosition[board.Length][];
			for (int i = 0; i < board.Length; i++) {
				res[i] = new MazeNetPosition[board[0].Length];
				for (int j = 0; j < board[i].Length; j++) {
					cardType tmpCard = new cardType ();
					cardTypeOpenings tmpOpenings = new cardTypeOpenings ();
					tmpOpenings.bottom = board [i] [j].Card.openings.bottom;
					tmpOpenings.left = board [i] [j].Card.openings.left;
					tmpOpenings.right = board [i] [j].Card.openings.right;
					tmpOpenings.top = board [i] [j].Card.openings.top;
					tmpCard.openings = tmpOpenings;
					tmpCard.treasure = board [i] [j].Card.treasure;
					tmpCard.treasureSpecified= board [i] [j].Card.treasureSpecified;
					int[] pins = new int[board[i][j].Card.pin.Length];
					for (int x = 0; x < board[i][j].Card.pin.Length; x++) {
						pins [x] = board [i] [j].Card.pin [x];
					}
					tmpCard.pin = pins;
					MazeNetPosition tmpPos = new MazeNetPosition (board [i] [j].X, board [i] [j].Y, tmpCard);
					res [i] [j] = tmpPos;
				}
			}
			return res;
		}

		public static MazeNetPosition[] getWayablePositions(MazeNetPosition start, MazeNetPosition[][] board)
		{
			List<MazeNetPosition> visited = new List<MazeNetPosition> ();
			ConcurrentStack<MazeNetPosition> res = new ConcurrentStack<MazeNetPosition> ();
			if (start != null) {
				visited.Add (start);
				if (start.Card.openings.bottom && start.Y < board.Length - 2) {
					if (board [start.X] [start.Y + 1].Card.openings.top) {
						res.Push (board [start.X] [start.Y + 1]);
						visited.Add (board [start.X] [start.Y + 1]);		
					}
				}
				if (start.Card.openings.left && start.X > 0) {
					if (board [start.X - 1] [start.Y].Card.openings.right) {
						res.Push (board [start.X - 1] [start.Y]);
						visited.Add (board [start.X - 1] [start.Y]);
					}
				}
				if (start.Card.openings.right && start.X < board.Length - 1) {
					if (board [start.X + 1] [start.Y].Card.openings.left) {
						res.Push (board [start.X + 1] [start.Y]);
						visited.Add (board [start.X + 1] [start.Y]);	
					}
				}
				if (start.Card.openings.top && start.Y > 0) {
					if (board [start.X] [start.Y - 1].Card.openings.bottom) {
						res.Push (board [start.X] [start.Y - 1]);
						visited.Add (board [start.X] [start.Y - 1]);
					}
				}
				Parallel.ForEach (visited, current => {
					var xPs = getNextWayable (current, board, visited);
					if (xPs != null) {
						res.PushRange (xPs);
					}
				});
//			foreach (var item in visited) {
//				if (item != start) {
//					var xPs = getNextWayable (item, board, visited);
//					if (xPs != null) {
//						res.PushRange (xPs);
//					}
//				}
//			}
			}
			res.Push (start);
			return res.Distinct ().ToArray ();
		}

		private static MazeNetPosition[] getNextWayable(MazeNetPosition start, MazeNetPosition[][] board, List<MazeNetPosition> seen)
		{
			List<MazeNetPosition> visited = new List<MazeNetPosition> ();
			visited.AddRange (seen);
			List<MazeNetPosition> res = new List<MazeNetPosition> ();
			List<MazeNetPosition> returningRes = new List<MazeNetPosition>();
			if (start.Card.openings.bottom && start.Y < board.Length - 1) {
				if (!seen.Contains (board [start.X] [start.Y + 1])) {
					if (board [start.X] [start.Y + 1].Card.openings.top) {
						res.Add (board [start.X] [start.Y + 1]);
						visited.Add (board [start.X] [start.Y + 1]);		
					}
				}
			}
			if (start.Card.openings.left && start.X > 0) {
				if (!seen.Contains (board [start.X - 1] [start.Y])) {
					if (board [start.X - 1] [start.Y].Card.openings.right) {
						res.Add (board [start.X - 1] [start.Y]);
						visited.Add (board [start.X - 1] [start.Y]);
					}
				}
			}
			if (start.Card.openings.right && start.X < board.Length - 1) {
				if (!seen.Contains (board [start.X + 1] [start.Y])) {
					if (board [start.X + 1] [start.Y].Card.openings.left) {
						res.Add (board [start.X + 1] [start.Y]);
						visited.Add (board [start.X + 1] [start.Y]);	
					}
				}
			}
			if (start.Card.openings.top && start.Y > 0) {
				if (!seen.Contains (board [start.X] [start.Y - 1])) {
					if (board [start.X] [start.Y - 1].Card.openings.bottom) {
						res.Add (board [start.X] [start.Y - 1]);
						visited.Add (board [start.X] [start.Y - 1]);
					}
				}
			}

			if (res.Count > 0) {
				returningRes.AddRange (res);
				foreach (var item in res) {
					var xPs = getNextWayable(item, board, visited);
					if (xPs != null) {
						returningRes.AddRange (xPs);
					}
				}
				return returningRes.ToArray();
			} else {
				return null;
			}

		}
	}
}

