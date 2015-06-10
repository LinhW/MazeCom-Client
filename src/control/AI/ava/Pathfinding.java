package control.AI.ava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.jaxb.CardType.Pin;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;
import control.AI.ava.ownClasses.Board;
import control.AI.ava.ownClasses.Card;
import control.AI.ava.ownClasses.Position;

public class Pathfinding {

	// TODO difference between treasure is glued or not?
	// TODO create at the beginning a list with the order of the players, beginning with nextPlayer
	private final int x = 7;
	private final int y = 7;
	private Board betterBoard;
	private int PlayerID;
	private static WriteIntoFile wif;
	private static WriteIntoFile possPos;
	private static WriteIntoFile wif_v2;
	private static WriteIntoFile wif_pin;
	private static WriteIntoFile wif_board;
	private List<TreasuresToGoType> list_treToGo;
	private static final String FILEPATH = "src/control/AI/ava/possPos.txt";
	private int nextPlayer;

	/**
	 * PinPosHelp with diff from new PinPos to TreasurePos
	 */
	private List<PinPosHelp> list_PinPosHelp_v1;
	/**
	 * PinPosHelp with diff from new PinPos to the next Position with connection to the Treasure
	 */
	private Map<CardHelp, ReverseHelp> map_PinPosHelp_v2;

	private List<TreasureType> list_foundTreasures;

	public Pathfinding(Board b, int PlayerID, List<TreasuresToGoType> list) {
		this(PlayerID);
		this.betterBoard = b;
		this.list_treToGo = list;
	}

	public Pathfinding(int PlayerID) {
		list_PinPosHelp_v1 = new ArrayList<>();
		map_PinPosHelp_v2 = new HashMap<>();
		list_foundTreasures = new ArrayList<>();
		wif = new WriteIntoFile(WriteIntoFile.FILEPATH + WriteIntoFile.FILEEXTENSION);
		wif_v2 = new WriteIntoFile(WriteIntoFile.FILEPATH + "_v2" + WriteIntoFile.FILEEXTENSION);
		wif_pin = new WriteIntoFile(WriteIntoFile.FILEPATH + "_pin" + WriteIntoFile.FILEEXTENSION);
		wif_board = new WriteIntoFile(WriteIntoFile.FILEPATH + "_board" + WriteIntoFile.FILEEXTENSION);
		possPos = new WriteIntoFile(FILEPATH);
		possPos.clearFile();
		wif_pin.clearFile();
		wif_board.clearFile();
		this.PlayerID = PlayerID;
	}

	public void setBoard(Board b) {
		this.betterBoard = b;
	}

	public void setTreToGo(List<TreasuresToGoType> list) {
		this.list_treToGo = list;
		nextPlayer = (PlayerID + 1) % list.size();
		if (nextPlayer == 0) {
			nextPlayer = list.size();
		}
	}

	public void setFoundTreasures(List<TreasureType> foundTreasures) {
		this.list_foundTreasures = foundTreasures;
	}

	/**
	 * start calculating the estimated best turn
	 * 
	 * @return best turn as PinPosHelp
	 */
	public PinPosHelp start() {
		return ava();
	}

	/**
	 * start Ava's Algorithm
	 * 
	 * @return PinPosHelp with shiftPosition, shiftCard and new PinPos
	 */
	private PinPosHelp ava() {
		list_PinPosHelp_v1.clear();
		map_PinPosHelp_v2.clear();

		TreasureType tre = betterBoard.getTreasure();
		List<PinPosHelp> l_pph = simpleSolution(tre);

		PinPosHelp pph;
		if (l_pph.size() == 0) {
			pph = nextStep(tre);
		} else {
			// TODO lastChance
			l_pph = beAnnoying(l_pph);
			if (l_pph.size() == 1) {
				pph = l_pph.get(0);
			} else {
				pph = sealAway(l_pph).get(0);
			}
		}
		return pph;
	}

	/**
	 * proceed turn that the opponent is sealed at his current position
	 * 
	 * @param list
	 *            of solutions
	 * @return list of solutions with sealed factor
	 */
	private List<PinPosHelp> sealAway(List<PinPosHelp> list) {
		// TODO
		return list;
	}

	/**
	 * proceed turn with hopefully bad consequences for the opponent
	 * 
	 * @param list
	 *            of solutions
	 * @return list of solutions with annoying factor
	 */
	private List<PinPosHelp> beAnnoying(List<PinPosHelp> l_sol) {
		List<PinPosHelp> tmp = new ArrayList<>();
		int count = 0;
		for (PinPosHelp pp : l_sol) {
			int c = 0;
			Board b = (Board) betterBoard.clone();
			CardHelp ch = pp.getCardHelp();
			Card shift = ch.getC();
			Position shiftPos = ch.getP();
			b.proceedShift(shiftPos, new Card(shift));
			for (int i = 1; i < list_treToGo.size(); i++) {
				// FIXME
				int number = (PlayerID + i) % list_treToGo.size();
				if (number == 0) {
					number = list_treToGo.size();
				}
				Position next = b.getPinPos(number);
				if (shiftPos.getRow() % 6 != 0 && shiftPos.getRow() == next.getRow() || (shiftPos.getCol() % 6 != 0 && shiftPos.getCol() == next.getCol())) {
					c++;
					if (i == 1 && analyse(b, next)) {
						continue;
					}
				} else {
					if (i == 1) {
						continue;
					}
				}
			}
			if (c == count) {
				tmp.add(pp);
			}
			if (c > count) {
				count = c;
				tmp.clear();
				tmp.add(pp);
			}
		}
		return tmp;
	}

	/**
	 * analyse if the opponent shift is estimated good or bad for him
	 * 
	 * @param board
	 * @param position
	 *            of the player to analyse
	 * @return
	 */
	private boolean analyse(Board b, Position player) {
		List<Position> list = new ArrayList<>();
		list = findPossiblePos(b, list, player);

		int treFound = 0;
		int treFind = 0;
		for (Position p : list) {
			TreasureType t = b.getCard(p.getRow(), p.getCol()).getTreasure();
			if (t != null) {
				if (list_foundTreasures.contains(t)) {
					treFound++;
				} else {
					treFind++;
				}
			}
		}
		if (treFound > treFind) {
			return true;
		} else {
			return false;
		}
	}

	private List<PinPosHelp> lastChance(List<PinPosHelp> list, int id) {
		// TODO
		if (list.size() == 0) {

		} else {

		}
		return null;
	}

	public List<CardHelp> lastChance(int id) {
		Position end = null;
		Position shiftPos;
		Board b;
		List<Position> list_shiftPos = new ArrayList<>();
		List<Position> tmp = new ArrayList<>();
		List<CardHelp> list = new ArrayList<>();
		int size = Integer.MAX_VALUE;
		Card shiftCard = betterBoard.getShiftCard();
		List<Card> list_C = shiftCard.getPossibleRotations();
		switch (id) {
		case 1:
			end = new Position(0, 0);
			break;
		case 4:
			end = new Position(6, 6);
			break;
		case 2:
			end = new Position(0, 6);
			break;
		case 3:
			end = new Position(6, 0);
			break;
		}

		for (int i = 0; i < 2; i++) {
			shiftPos = new Position((end.getRow() + (-end.getRow() / 3 + 1) * i), (end.getCol() + (-end.getCol() / 3 + 1) * (1 - i)));
			list_shiftPos.add(shiftPos);
			list_shiftPos.add(shiftPos.getOpposite());
		}
		list_shiftPos.remove(betterBoard.getForbidden());

		for (Card c : list_C) {
			for (Position pos : list_shiftPos) {
				b = (Board) betterBoard.clone();
				b.proceedShift(pos, new Card(c));
				tmp = findPossiblePos(b, tmp, end);
				if (!tmp.contains(end)) {
					if (tmp.size() == size) {
						list.add(new CardHelp(c, pos, id, size));
					} else {
						if (tmp.size() < size) {
							size = tmp.size();
							list.clear();
							list.add(new CardHelp(c, pos, id, size));
						}
					}
				}
			}
		}
		return list;
	}

	private List<CardHelp> lastChance() {
		//TODO korrekt?
		List<CardHelp> list_ch = lastChance(nextPlayer);
		if (list_ch.size() == 0) {
			List<Position> l = new ArrayList<>();
			List<CardHelp> l_sol = new ArrayList<>();
			Board board;
			Position end = null;


			switch (nextPlayer) {
			case 1:
				end = new Position(0, 0);
				break;
			case 4:
				end = new Position(6, 6);
				break;
			case 2:
				end = new Position(0, 6);
				break;
			case 3:
				end = new Position(6, 0);
				break;
			}

			List<Card> list_c = betterBoard.getShiftCard().getPossibleRotations();
			boolean pos = true;
			for (Card c : list_c) {
				for (int i = 1; i < 6; i += 2) {
					for (int k = 0; k < 7; k += 6) {
						for (int j = 0; j < 2; j++) {
							board = (Board) betterBoard.clone();
							Position shiftPos = new Position(k + (i - k) * j, i + (k - i) * j);
							if (betterBoard.getForbidden() != null && shiftPos.equals(new Position(betterBoard.getForbidden()))) {
								continue;
							}
							board.proceedShift(shiftPos, new Card(c));
							Position oldPinPos = board.getPinPos(nextPlayer);
							l.clear();
							l = findPossiblePos(board, l, oldPinPos);
							CardHelp ch = new CardHelp(c, shiftPos);
							if (!l.contains(end)) {
								List<Card> list_c2 = betterBoard.getCard(shiftPos.getOpposite()).getPossibleRotations();
								outer: for (Card c2 : list_c2) {
									for (int i2 = 1; i2 < 6; i2 += 2) {
										for (int k2 = 0; k2 < 7; k2 += 6) {
											for (int j2 = 0; j2 < 2; j2++) {
												Board board2 = (Board) board.clone();
												Position shiftPos2 = new Position(k2 + (i2 - k2) * j2, i2 + (k2 - i2) * j2);
												if (shiftPos2.equals(new Position(board.getForbidden()))) {
													continue;
												}
												board2.proceedShift(shiftPos2, new Card(c2));
												Position oldPinPos2 = board2.getPinPos(nextPlayer);
												l.clear();
												l = findPossiblePos(board2, l, oldPinPos2);
												if (l.contains(end)) {
													pos = false;
													break outer;
												}
											}
										}
									}
								}
								if (pos) {
									l_sol.add(ch);
								}
							}
						}
					}
				}
			}
			return l_sol;
		} else {
			return list_ch;
		}
	}

	private List<PinPosHelp> shortestPath(List<CardHelp> list_ch) {
		List<PinPosHelp> l_sol = new ArrayList<>();
		List<Position> list_pos = new ArrayList<>();
		Position trePos;
		int min = Integer.MAX_VALUE;
		for (CardHelp ch : list_ch) {
			Board b = (Board) betterBoard.clone();
			b.proceedShift(ch.getP(), new Card(ch.getC()));
			trePos = b.findTreasure(betterBoard.getTreasure());
			list_pos.clear();
			list_pos = findPossiblePos(b, list_pos, b.getPinPos(PlayerID));
			for (Position p : list_pos) {
				int diff = p.diff(trePos);
				if (diff < min) {
					min = diff;
					l_sol.clear();
					l_sol.add(new PinPosHelp(p, ch));
				} else {
					if (diff == min) {
						l_sol.add(new PinPosHelp(p, ch));
					}
				}
			}
		}
		return l_sol;
	}

	/**
	 * find best position to find the treasure in the next step
	 * 
	 * @param treasure
	 * @return PinPosHelp
	 */
	private PinPosHelp nextStep(TreasureType tre) {
		PinPosHelp pph;
		List<PinPosHelp> list = PinPosHelp.getSmallestDiff(list_PinPosHelp_v1);
		Map<CardHelp, ReverseHelp> map = ReverseHelp.getValueableDiff(map_PinPosHelp_v2, list_treToGo.size());
		if (list.size() == 1) {
			List<List<CardHelp>> tmp = new ArrayList<>();
			//TODO startet bei nextPlayer, geht weiter zu den anderen
			for (TreasuresToGoType ttgt : list_treToGo) {
				if (ttgt.getTreasures() == 1 && ttgt.getPlayer() == nextPlayer) {
					List<CardHelp> list_ch = lastChance();
					if (list_ch.size() == 0) {
						return list_PinPosHelp_v1.get(0);
					} else {
						if (list_ch.contains(list.get(0).getCardHelp())) {
							return list.get(0);
						} else {
							list = shortestPath(list_ch);
							if (list.size() == 1) {
								return list.get(0);
							} else {
								list = beAnnoying(list);
								if (list.size() == 1) {
									return list.get(0);
								} else {
									return sealAway(list).get(0);
								}
							}
						}
					}
				} else {
					if (ttgt.getTreasures() == 1 && ttgt.getPlayer() != this.PlayerID) {
						tmp.add(lastChance(ttgt.getPlayer()));
						// TODO
					}
				}
			}
			// TODO vergleichen was ist am besten
			pph = list.get(0);
		} else {
			if (list.size() == 0) {
				System.out.println("sonderfall");
				// TODO annoy or look forward
				return new PinPosHelp(betterBoard.getPinPos(PlayerID), new CardHelp(betterBoard.getShiftCard(), new Position(0, 1)));
			}
			List<PinPosHelp> tmp_remove = new ArrayList<>();
			for (PinPosHelp pp : list) {
				if (!map.containsKey(pp.getCardHelp())) {
					tmp_remove.add(pp);
				}
			}
			list.removeAll(tmp_remove);
			if (list.size() == 0) {
				list.addAll(tmp_remove);
				list = chooseOrientation(list);
				if (list.size() == 1) {
					return list.get(0);
				}
			}
			if (list.size() == 1) {
				list = chooseOrientation(list);
				if (list.size() == 0) {
					list = list_PinPosHelp_v1;
				} else {
					return list.get(0);
				}
			}
			// TODO
			pph = PinPosHelp.getSmallestDiff(list_PinPosHelp_v1).get(0);
		}
		return pph;
	}

	/**
	 * take a card where the openings look in the direction of the treasure and it's openings
	 * 
	 * @param list
	 * @return
	 */
	private List<PinPosHelp> chooseOrientation(List<PinPosHelp> list) {
		List<PinPosHelp> tmp1 = new ArrayList<>();
		List<PinPosHelp> tmp2 = new ArrayList<>();
		for (PinPosHelp pph : list) {
			Position p = pph.getPinPos();
			Card c = pph.getCardHelp().getC();
			int count = 0;
			if (pph.getTrePos().getCol() <= p.getCol() && c.getOpenings().isTop()) {
				count++;
			}
			if (pph.getTrePos().getCol() >= p.getCol() && c.getOpenings().isBottom()) {
				count++;
			}
			if (pph.getTrePos().getRow() <= p.getRow() && c.getOpenings().isLeft()) {
				count++;
			}
			if (pph.getTrePos().getRow() >= p.getRow() && c.getOpenings().isRight()) {
				count++;
			}
			if (count == 2) {
				tmp2.add(pph);
			} else if (count == 1) {
				tmp1.add(pph);
			}
		}
		if (tmp2.size() == 0) {
			return tmp1;
		} else {
			tmp1.clear();
			tmp2.clear();
			list = tmp2;
			for (PinPosHelp pph : list) {
				Position p = pph.getPinPos();
				int count = 0;
				Card tre = new Card(betterBoard.getCard(betterBoard.findTreasure(betterBoard.getTreasure())));
				if (pph.getTrePos().getCol() <= p.getCol() && tre.getOpenings().isBottom()) {
					count++;
				}
				if (pph.getTrePos().getCol() >= p.getCol() && tre.getOpenings().isTop()) {
					count++;
				}
				if (pph.getTrePos().getRow() <= p.getRow() && tre.getOpenings().isRight()) {
					count++;
				}
				if (pph.getTrePos().getRow() >= p.getRow() && tre.getOpenings().isLeft()) {
					count++;
				}
				if (count == 2) {
					tmp2.add(pph);
				} else if (count == 1) {
					tmp1.add(pph);
				}
			}
		}
		if (tmp2.size() == 0) {
			return tmp1;
		}
		return tmp2;
	}

	/**
	 * calculate the shortest path between all positions in list and the position of the treasure
	 * 
	 * @param list
	 *            <Position>
	 * @param treasurePosition
	 * @param CardHelp
	 *            (source of the current situation)
	 * @return list of PinPosHelp (include CardHelp, treasurePosition, a position of list, the calculated difference)
	 */
	private List<PinPosHelp> shortestPath(List<Position> list, Position trePos, CardHelp ch) {
		List<PinPosHelp> pos = new ArrayList<>();
		int diff = x * y;
		for (Position p : list) {
			int tmp = p.diff(trePos);
			if (tmp < diff) {
				diff = tmp;
				pos.clear();
				pos.add(new PinPosHelp(trePos, p, ch, diff));
			} else if (tmp == diff) {
				pos.add(new PinPosHelp(trePos, p, ch, tmp));
			}
		}
		return pos;
	}

	/**
	 * calculate the shortest Path between each position in list and each position in list_rev
	 * 
	 * @param list
	 *            <Position>
	 * @param list_rev
	 *            <Position>
	 * @param CardHelp
	 *            (source of the current situation)
	 * @param trePos
	 * @return
	 */
	private Map<CardHelp, ReverseHelp> shortestPath(List<Position> list, List<Position> list_rev, CardHelp ch, Position trePos) {
		Map<CardHelp, ReverseHelp> pos = new HashMap<>();
		// wif_v2.write("------------------- " + list.size() + " " + list_rev.size());
		int diff = x * y;
		for (Position p : list) {
			for (Position pr : list_rev) {
				int tmp = p.diff(pr);
				// wif_pin.write(p + "-" + pr + "=" + tmp);
				if (tmp < diff) {
					diff = tmp;
					pos.clear();
					pos.put(ch, new ReverseHelp(p, diff, trePos.diff(pr)));
					// wif_v2.write(new PinPosHelp(p, diff).toString());
				} else if (tmp == diff) {
					pos.put(ch, new ReverseHelp(p, diff, trePos.diff(pr)));
					// wif_v2.write("add " + new PinPosHelp(p, diff).toString());
				}
			}
		}
		// wif_v2.write("-------------------");
		return pos;
	}

	private List<PinPosHelp> simpleSolution(TreasureType tre) {
		// System.out.println("simpleSolution");
		// possPos.write(betterBoard.toString());
		boolean already = false;
		List<Position> l = new ArrayList<>();
		List<Position> l_rev = new ArrayList<>();
		List<PinPosHelp> l_sol = new ArrayList<>();
		Board board;
		Position trePos;
		Position oldPinPos;
		Position shiftPos;
		CardHelp ch;
		// wif_pin.write("ShiftCard:");
		// wif_pin.write(betterBoard.getShiftCard().toString());

		List<Card> list_c = betterBoard.getShiftCard().getPossibleRotations();
		for (Card c : list_c) {
			// wif_pin.write(c.toString());
			for (int i = 1; i < 6; i += 2) {
				for (int k = 0; k < 7; k += 6) {
					for (int j = 0; j < 2; j++) {
						board = (Board) betterBoard.clone();
						c.setPin(new Pin());
						shiftPos = new Position(k + (i - k) * j, i + (k - i) * j);
						if (betterBoard.getForbidden() != null && shiftPos.equals(new Position(betterBoard.getForbidden()))) {
							// wif_pin.write("ForbiddenPos: " + betterBoard.getForbidden());
							continue;
						}
						board.proceedShift(shiftPos, new Card(c));
						oldPinPos = board.getPinPos(PlayerID);
						trePos = board.findTreasure(tre);
						if (trePos != null) {
							l.clear();
							l = findPossiblePos(board, l, oldPinPos);
							l_rev.clear();
							l_rev = findPossiblePos(board, l_rev, trePos);
							ch = new CardHelp(c, shiftPos);
							// wif_v2.write(board.toString());
							// wif_v2.write(betterBoard.getPinPos(PlayerID) + " " + oldPinPos.toString() + " " + trePos.toString());
							if (l.contains(trePos)) {
								already = true;
								// wif.write(board.toString());
								// wif.write(oldPinPos.toString());
								// wif.write("************************************************\n" + ch.toString() + "\n************************************************");
								// wif_pin.write("Found it: " + trePos);
								// possPos.write("found it");
								// possPos.write(ch.toString());
								// wif_v2.write(board.toString());
								// wif_v2.write(betterBoard.getPinPos(PlayerID) + " " + oldPinPos.toString());
								// wif_v2.write("************************************************\n" + ch.toString() + "\n************************************************");
								l_sol.add(new PinPosHelp(trePos, ch));
								// System.out.println("possible");
							}
							if (!already) {
								calcData(oldPinPos, trePos, ch, l, l_rev);
							}
						}
					}
				}
			}
		}
		return l_sol;
	}

	private void calcData(Position start, Position trePos, CardHelp ch, List<Position> list, List<Position> list_rev) {
		// wif.write(list.size() + " " + list_rev.size());
		List<PinPosHelp> lpph = shortestPath(list, trePos, ch);
		// possPos.writeList(lpph);
		// for (PinPosHelp pp : lpph) {
		// possPos.write(pp.debug());
		// }
		// possPos.writeNewLine(1);
		list_PinPosHelp_v1.addAll(lpph);

		Map<CardHelp, ReverseHelp> mpph = shortestPath(list, list_rev, ch, trePos);
		map_PinPosHelp_v2.putAll(mpph);

		// possPos.write(map_PinPosHelp_v2.size());
		// possPos.writeNewLine(2);
	}

	private List<Position> findPossiblePos(Board b, List<Position> list, Position start) {
		// count++;
		Position p;
		int col_start = start.getCol();
		int row_start = start.getRow();
		Card c = new Card(b.getCard(row_start, col_start));
		if (col_start < x - 1) {
			p = new Position(row_start, col_start + 1);
			if (!list.contains(p) && b.getCard(row_start, col_start + 1).getOpenings().isLeft() && c.getOpenings().isRight()) {
				list.add(p);
				// possPos.write("right " + list.size());
				list = (findPossiblePos(b, list, p));
			}
		}
		if (col_start > 0) {
			p = new Position(row_start, col_start - 1);
			if (!list.contains(p) && b.getCard(row_start, col_start - 1).getOpenings().isRight() && c.getOpenings().isLeft()) {
				list.add(p);
				// possPos.write("left " + list.size());
				list = (findPossiblePos(b, list, p));
			}
		}
		if (row_start > 0) {
			p = new Position(row_start - 1, col_start);
			if (!list.contains(p) && b.getCard(row_start - 1, col_start).getOpenings().isBottom() && c.getOpenings().isTop()) {
				list.add(p);
				// possPos.write("up " + list.size());
				list = (findPossiblePos(b, list, p));
			}
		}
		if (row_start < y - 1) {
			p = new Position(row_start + 1, col_start);
			if (!list.contains(p) && b.getCard(row_start + 1, col_start).getOpenings().isTop() && c.getOpenings().isBottom()) {
				list.add(p);
				// possPos.write("down " + list.size());
				list = (findPossiblePos(b, list, p));
			}
		}
		return list;
	}

	public static class PinPosHelp {
		private Position pinPos;
		private int diff = Integer.MAX_VALUE;
		private CardHelp ch;
		private Position trePos;

		public PinPosHelp(Position pinPos, CardHelp ch) {
			this.pinPos = pinPos;
			this.ch = ch;
			if (ch == null) {
				possPos.write(this.toString());
			}
		}

		public PinPosHelp(Position trePos, Position pinPos, CardHelp ch, int diff) {
			this.trePos = trePos;
			this.pinPos = pinPos;
			this.ch = ch;
			this.diff = diff;
			if (ch == null) {
				possPos.write(this.toString());
			}
		}

		public Position getPinPos() {
			return pinPos;
		}

		public int getDiff() {
			return diff;
		}

		public Position getTrePos() {
			return this.trePos;
		}

		public void setTrePos(Position trePos) {
			this.trePos = trePos;
		}

		public static List<PinPosHelp> getSmallestDiff(List<PinPosHelp> list) {
			List<PinPosHelp> res = new ArrayList<>();
			int min = Integer.MAX_VALUE;
			int diff;
			for (PinPosHelp pph : list) {
				diff = pph.getDiff();
				if (diff == min) {
					res.add(pph);
				}
				if (diff < min) {
					min = diff;
					res.clear();
					res.add(pph);
				}
			}
			return res;
		}

		public CardHelp getCardHelp() {
			return ch;
		}

		public void setCardHelp(CardHelp ch) {
			this.ch = ch;
		}

		public String toString() {
			return "PinPos: " + pinPos + " " + ch;
		}

		public String debug() {
			if (ch != null) {
				return "TrePos: " + trePos + " PinPos: " + pinPos + " " + ch.debug() + "\t" + diff;
			} else {
				return "PinPos: " + pinPos + " " + " null\t" + diff;
			}
		}

		public boolean equals(PinPosHelp pph) {
			return pph.ch.equals(this.getCardHelp()) && this.pinPos.equals(pph.getPinPos());
		}

		public boolean equalsWithoutRot(PinPosHelp pph) {
			return pph.getCardHelp().equalsWithoutRot(this.ch) && this.pinPos.equals(pph.getPinPos());
		}
	}

	public static class CardHelp {
		private Card c;
		private Position p;
		private int diff;
		private int player;

		public CardHelp(Card c, Position p) {
			this.c = c;
			this.p = p;
		}

		public CardHelp(Card c, Position p, int player, int diff) {
			this.c = c;
			this.p = p;
			this.player = player;
			this.diff = diff;
		}

		public Card getC() {
			return c;
		}

		public Position getP() {
			return p;
		}

		public String toString() {
			return "CardPos: " + p + "\n" + c;
		}

		public String debug() {
			return "CardPos: " + p + " Card: " + c.getShape() + c.getOrientation().value();
		}

		public boolean equals(CardHelp ch) {
			return this.c.equals(ch.getC()) && this.p.equals(ch.getP());
		}

		public boolean equalsWithoutRot(CardHelp ch) {
			return this.c.getShape() == ch.getC().getShape() && this.p.equals(ch.getP());
		}

		public int getDiff() {
			return diff;
		}

		public int getPlayer() {
			return player;
		}

	}

	public static class ReverseHelp {
		private int diff;
		private int dist;
		private Position pinPos;

		public ReverseHelp(Position pinPos, int diff, int dist) {
			this.diff = diff;
			this.dist = dist;
			this.pinPos = pinPos;
		}

		public static Map<CardHelp, ReverseHelp> getValueableDiff(Map<CardHelp, ReverseHelp> map, int player) {
			List<CardHelp> tmp = new ArrayList<>();
			for (CardHelp key : map.keySet()) {
				if (map.get(key).getDist() > 6 - player) {
					tmp.add(key);
				}
			}
			map.remove(tmp);
			return map;
		}

		public int getDiff() {
			return diff;
		}

		public int getDist() {
			return dist;
		}

		public Position getPinPos() {
			return pinPos;
		}

		public String toString() {
			return "ReverseHelp: " + pinPos + " diff:" + diff + " dist:" + dist;
		}
	}

}
