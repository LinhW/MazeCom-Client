using System;
using System.Linq;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace MazeNetClient
{
	public class MazeNetKI
	{
		public MazeNetUser user {
			get;
			set;
		}

		public boardType activeBoard {
			get;
			set;
		}

		public TreasuresToGoType treasuresToGo {
			get;
			set;
		}

		public treasureType treasure {
			get;
			set;
		}

		public List<treasureType> found {
			get;
			set;
		}

		public MazeNetKI (treasureType treasure, TreasuresToGoType treasuresToGo,boardType activeBoard, MazeNetUser user)
		{
			this.user = user;
			this.treasure = treasure;
			this.treasuresToGo = treasuresToGo;
			this.activeBoard = activeBoard;
			found = new List<treasureType> ();
		}

		public MazeNetMessage calculateNextMove()
		{
			MazeNetPosition[][] board = MazeNetUtil.boardToMatrix (activeBoard);
			MazeNetPosition player = MazeNetUtil.getPlayerPosition (user.ID, board);
			MazeNetPosition itreasure = MazeNetUtil.getTreasurePosition (this.treasure, board);
			Dictionary<double, List<MazeNetMessage>> moves = new Dictionary<double, List<MazeNetMessage>> ();
			//MazeNetPosition[][] outputBoard = null;
			double minDist = 99;
			//MazeNetPosition tile = new MazeNetPosition (0, 0, activeBoard.shiftCard);
			//Console.WriteLine ("Aktuelles Tile: \n" + MazeNetUtil.posToString (tile));
			//Console.WriteLine ("Aktuelles Board: \n" + MazeNetUtil.MatrixToString (board));

			MazeNetMessage finalMsg = null;
			MazeNetPosition sCard = new MazeNetPosition (0, 0, activeBoard.shiftCard);
			found.Add (treasure);
			for (int z = 0; z < 4; z++) { 
				sCard = MazeNetUtil.rotateRigth (sCard);
				//Console.WriteLine ("Aktuelles Tile: \n" + MazeNetUtil.posToString (sCard));
				for (int i = 0; i < board.Length; i++) {
					for (int j = 0; j < board.Length; j++) {
						MazeNetPosition[][] tmpBoard = MazeNetUtil.deepCloneBoard (board);
						MazeNetPosition p = new MazeNetPosition (i, j, null);
						if (activeBoard.forbidden == null || activeBoard.forbidden.col != i && activeBoard.forbidden.row != j) {
							tmpBoard = MazeNetUtil.Shift (user.ID, tmpBoard, sCard.Card, p);
							if (tmpBoard != null) {
								player = MazeNetUtil.getPlayerPosition (user.ID, tmpBoard);
								itreasure = MazeNetUtil.getTreasurePosition (this.treasure, tmpBoard);
								if (itreasure != null) {
									MazeNetPosition[] playerWayable = MazeNetUtil.getWayablePositions (player, tmpBoard);
									MazeNetPosition[] treasureWayable = MazeNetUtil.getWayablePositions (itreasure, tmpBoard);
									foreach (var pWay in playerWayable) {
										foreach (var tWay in treasureWayable) {
											double d = MazeNetUtil.getDistance (pWay, tWay);
											if (d <= minDist) {
												minDist = d;
												MazeNetPosition mcp = new MazeNetPosition (i, j, MazeNetUtil.deepCloneMCard (sCard).Card); 
												//outputBoard = tmpBoard;
												List<MazeNetMessage> tmpList;
												moves.TryGetValue (d, out tmpList);
												if (tmpList == null) {
													tmpList = new List<MazeNetMessage> ();
													moves.Add (d, tmpList);
												}
												if (d == 0) {
													tmpList.Add(MazeNetUtil.getMoveMessage (itreasure, mcp, tmpBoard, d)); 
													//Console.WriteLine ("d = " + d + " bei px = " + itreasure.X + " py = " + itreasure.Y + " und  ix = " + mcp.X + " iy = " + mcp.Y);
												} else {
													tmpList.Add(MazeNetUtil.getMoveMessage (pWay, mcp, tmpBoard, d)); 
													//Console.WriteLine ("d = " + d + " bei px = " + pWay.X + " py = " + pWay.Y + " und  ix = " + mcp.X + " iy = " + mcp.Y);
												}

											}
										}
									}
								}
							}
						}
					}
				}
			}
			List<MazeNetMessage> finalMoves;
			moves.TryGetValue (minDist, out finalMoves);
			finalMsg = recalculateMoves (finalMoves);

			Console.WriteLine ("Bester Move mit d = " + minDist);
			//Console.WriteLine ("finalMsg={0}", finalMsg.ToString());	
			//Console.WriteLine ("outputBoard=\n{0}", MazeNetUtil.MatrixToString (outputBoard));
			return finalMsg;
		}

		MazeNetMessage recalculateMoves (List<MazeNetMessage> finalMoves)
		{
			Dictionary<MazeNetMessage, List<double>> calc = new Dictionary<MazeNetMessage, List<double>> ();
			double d = finalMoves [0].PlayerTreasureDistance;
			List<MazeNetMessage> resList = new List<MazeNetMessage>();
			int mCount = 0;
			foreach (var item in finalMoves) {
				MoveMessageType mt = (item.mazeCom.Item as MoveMessageType);
				item.annoying = MazeNetUtil.CalculateAnnoyingness(MazeNetUtil.boardToMatrix(activeBoard), new MazeNetPosition(mt.shiftPosition.col, mt.shiftPosition.row, mt.shiftCard), user.ID, found);
				if (item.annoying == true && d == 0) {
					calc.Add (item, new List<double> ());
				}
			}
			if (calc.Count == 0) {
				foreach (var item in finalMoves) {
					MoveMessageType mt = (item.mazeCom.Item as MoveMessageType);
					calc.Add(item, new List<double>());
				}
			}

			//HARDCOREBERECHNUNG NUR AUF ULTRAMASCHIENEN DA SONST TIMEOUT NICHT REICHT!!!!!!!!!!
//			if (d > 0) {
//				Parallel.ForEach (calc, current => {
//					List<double> x;
//					calc.TryGetValue (current.Key, out x);
//					x = prepareRatedList (current.Key);
//				});
//
////				foreach (var item in calc) {
////					List<double> x;
////					calc.TryGetValue (item.Key, out x);
////					x = prepareRatedList (item.Key);
////				}	
//				foreach (var item in calc) {
//					if (mCount < item.Value.Count) {
//						mCount = item.Value.Count;
//					}
//				}
//				foreach (var item in calc) {
//					if (item.Value.Count == mCount) {
//						resList.Add (item.Key);
//					}
//				}
//			}
			foreach (var item in calc) {
				resList.Add (item.Key);
			}

			Random r = new Random ();

			if (d > 0) {
				return resList[(int)(r.Next (0, resList.Count - 1))];
			}
			List<MazeNetMessage> res = calc.Keys.ToList ();
			return res[(int)(r.Next (0, res.Count - 1))];
		}

		public List<double> prepareRatedList (MazeNetMessage key)
		{
			MoveMessageType move = key.mazeCom.Item as MoveMessageType;
			MazeNetPosition sCard = new MazeNetPosition (0, 0, move.shiftCard);
			MazeNetPosition[][] board = MazeNetUtil.deepCloneBoard(key.board);
			MazeNetPosition player = MazeNetUtil.getPlayerPosition (user.ID, board);
			MazeNetPosition itreasure = MazeNetUtil.getTreasurePosition (this.treasure, board);
			List<double> res = new List<double> ();
			double minDist = 99;
			for (int z = 0; z < 4; z++) { 
				sCard = MazeNetUtil.rotateRigth (sCard);
				//Console.WriteLine ("Aktuelles Tile: \n" + MazeNetUtil.posToString (sCard));
				for (int i = 0; i < board.Length; i++) {
					for (int j = 0; j < board.Length; j++) {
						MazeNetPosition[][] tmpBoard = MazeNetUtil.deepCloneBoard (board);
						MazeNetPosition p = new MazeNetPosition (i, j, null);
						if (activeBoard.forbidden == null || activeBoard.forbidden.col != i && activeBoard.forbidden.row != j) {
							tmpBoard = MazeNetUtil.Shift (user.ID, tmpBoard, sCard.Card, p);
							if (tmpBoard != null) {
								player = MazeNetUtil.getPlayerPosition (user.ID, tmpBoard);
								itreasure = MazeNetUtil.getTreasurePosition (this.treasure, tmpBoard);
								if (itreasure != null) {
									MazeNetPosition[] playerWayable = MazeNetUtil.getWayablePositions (player, tmpBoard);
									MazeNetPosition[] treasureWayable = MazeNetUtil.getWayablePositions (itreasure, tmpBoard);
									foreach (var pWay in playerWayable) {
										foreach (var tWay in treasureWayable) {
											double d = MazeNetUtil.getDistance (pWay, tWay);
											if (d < key.PlayerTreasureDistance) {
												minDist = d;
												res.Add (d);
											}
										}
									}
								}
							}
						}
					}
				}
			}

			return res;
		}
	}
}

