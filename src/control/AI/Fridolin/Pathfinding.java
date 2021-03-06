package control.AI.Fridolin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.jaxb.CardType.Pin;
import model.jaxb.TreasureType;
import model.jaxb.TreasuresToGoType;
import tools.WriteIntoFile;
import control.AI.Fridolin.ownClasses.Board;
import control.AI.Fridolin.ownClasses.Card;
import control.AI.Fridolin.ownClasses.CardHelp;
import control.AI.Fridolin.ownClasses.PinPosHelp;
import control.AI.Fridolin.ownClasses.Position;

public class Pathfinding {
	private final int X = 7;
	private final int Y = 7;
	private final int row0 = 01;
	private final int row6 = 06;
	private final int col0 = 10;
	private final int col6 = 60;

	private int id;
	private Board betterBoard;
	private Integer[] nextPlayer;
	private Map<Integer, Integer> map_treToGo;
	private List<TreasureType> list_foundTreasures;
	private List<PinPosHelp> list_rating;
	private WriteIntoFile wif_v2;
	private WriteIntoFile wif_player;

	public Pathfinding(int id) {
		this.id = id;
		list_rating = new ArrayList<>();
		wif_v2 = new WriteIntoFile(Fridolin.FILEPATH + "_v2" + WriteIntoFile.FILEEXTENSION);
		wif_player = new WriteIntoFile(Fridolin.FILEPATH + "_player" + WriteIntoFile.FILEEXTENSION);
	}

	public void setBoard(Board b) {
		this.betterBoard = b;
	}

	public void setTreToGo(List<TreasuresToGoType> list) {
		map_treToGo = new HashMap<>();
		for (TreasuresToGoType ttgt : list) {
			map_treToGo.put(ttgt.getPlayer(), ttgt.getTreasures());
		}

		int i = id + 1;
		nextPlayer = new Integer[list.size() - 1];
		for (int j = 0; j < nextPlayer.length; j++) {
			if (i > list.size()) {
				i = 1;
			}
			if (map_treToGo.containsKey(i)) {
				nextPlayer[j] = i;
			} else {
				j--;
			}
			i++;
		}
		wif_player.writeln(Arrays.asList(nextPlayer) + "");
	}

	public void setFoundTreasures(List<TreasureType> foundTreasures) {
		this.list_foundTreasures = foundTreasures;
	}

	/**
	 * start calculating the best turn
	 * 
	 * @return PinPosHelp with PinPos, ShiftPos and ShiftCard
	 */
	public PinPosHelp start() {
		wif_v2.writeln("\nnew Move");
		return friDoLin();
	}

	/**
	 * start Fridolin's Algorithm
	 * 
	 * @return PinPosHelp with shiftPosition, shiftCard and new PinPos
	 */
	private PinPosHelp friDoLin() {
		list_rating.clear();

		/*
		 * last treasure is reachable?
		 */
		TreasureType tre = betterBoard.getTreasure();
		if (map_treToGo.get(id) == 1) {
			List<PinPosHelp> l_pph = simpleSolution(tre, id);
			if (l_pph.size() > 0) {
				return l_pph.get(0);
			}
		}
		PinPosHelp pph;
		/*
		 * check if the next Player can win and how to deny it
		 */
		if (nextPlayer.length > 0) {
			pph = checkNextTurn(tre);
			if (pph != null) {
				return pph;
			}
		}

		/*
		 * is treasure reachable? - returns all possible CardHelps
		 */
		list_rating.clear();
		List<PinPosHelp> l_pph = simpleSolution(tre, id);

		if (l_pph.size() == 0) {
			pph = checkLast();
			if (pph == null) {
				reject(11);
				pph = nextStep(id);
			} else {
				return pph;
			}
		} else {
			list_rating = l_pph;
			reject(11);
			checkLastButOne();
			reject(7);
			beAnnoying();
			sealAway();
			if (nextPlayer.length > 0) {
				checkOtherPlayer();
			}
			pph = PinPosHelp.getLowestRating(list_rating);
		}
		return pph;
	}

	/**
	 * check if it is the last treasure and 1vs1 game
	 * 
	 * @return null if not else the calculated PinPosHelp
	 */
	private PinPosHelp checkLast() {
		if (map_treToGo.get(id) == 1 && nextPlayer.length == 1) {
			return strategyLastTreasure();
		}
		return null;
	}

	private void reject(int diff) {
		wif_v2.writeln("reject " + list_rating.size());
		double bestRating = PinPosHelp.getBestRating(list_rating);
		while (list_rating.size() > 30 && diff > 0) {
			// if (list_rating.size() > 10) {
			List<PinPosHelp> remove = new ArrayList<>();
			for (PinPosHelp pph : list_rating) {
				if (pph.getRating() > (bestRating + diff)) {
					remove.add(pph);
				}
			}
			list_rating.removeAll(remove);
			// }
			diff--;
			wif_v2.write("  " + list_rating.size());
		}
		wif_v2.writeln("reject ende" + list_rating.size());
	}

	/**
	 * take another move
	 * 
	 * @return the second best move
	 */
	public PinPosHelp getNewMove() {
		list_rating.remove(PinPosHelp.getLowestRating(list_rating));
		return PinPosHelp.getLowestRating(list_rating);
	}

	private void checkLastButOne() {
		wif_v2.writeln("checkLastButOne");
		TreasureType tre = betterBoard.getShiftCard().getTreasure();
		if (tre != null) {
			if (map_treToGo.get(id) == 2 && tre.equals(betterBoard.getTreasure())) {
				treIsOnShift();
			}
		}
	}

	private void checkOtherPlayer() {
		if (nextPlayer.length > 0) {
			for (Integer id : map_treToGo.keySet()) {
				switch (map_treToGo.get(id)) {
				case 1:
					sealEndPos(id);
					break;
				case 2:
					sealAway(id);
				}
			}
		}
	}

	/**
	 * calculate the best move in case that it is the last but one, the treasure is on the shift card and is reachable
	 * 
	 * @return
	 */
	private void treIsOnShift() {
		TreasureType tt = TreasureType.valueOf("START_0" + id);
		Position trePos = betterBoard.findTreasure(tt);
		for (PinPosHelp pph : list_rating) {
			CardHelp ch = pph.getCardHelp();
			Board board = (Board) betterBoard.clone();
			board.proceedShift(ch.getPos(), new Card(ch.getCard()));
			int diff = diff(ch.getPos(), trePos, board);
			pph.setRating(diff);
		}
	}

	/**
	 * proceed turn that the next opponent is sealed at his current position
	 * 
	 * @param list
	 *            of solutions
	 * @return list of solutions with sealed factor
	 */
	private void sealAway() {
		if (nextPlayer.length > 0) {
			Position pinPos;
			for (PinPosHelp pph : list_rating) {
				int min = 0;
				Board board = (Board) betterBoard.clone();
				board.proceedShift(pph.getCardHelp());
				List<Card> list_c = board.getShiftCard().getPossibleRotations();
				for (Card c : list_c) {
					for (int i = 1; i < 6; i += 2) {
						for (int k = 0; k < 7; k += 6) {
							for (int j = 0; j < 2; j++) {
								Board b = (Board) board.clone();
								Position shiftPos = new Position(k + (i - k) * j, i + (k - i) * j);
								if (b.getForbidden() != null && shiftPos.equals(new Position(b.getForbidden()))) {
									continue;
								}
								b.proceedShift(shiftPos, new Card(c));
								pinPos = b.getPinPos(nextPlayer[0]);
								List<Position> l = new ArrayList<>();
								l = findPossiblePos(b, l, pinPos);
								if (l.size() > min) {
									min = l.size();
								}
							}
						}
					}
				}

				pph.setRating(min);
			}
		}
	}

	/**
	 * proceed turn that the given opponent is sealed at his current position
	 * 
	 * @param list
	 *            of solutions
	 * @return list of solutions with sealed factor
	 */
	private void sealAway(int ID) {
		Position pinPos;
		for (PinPosHelp pph : list_rating) {
			Board board = (Board) betterBoard.clone();
			board.proceedShift(pph.getCardHelp());
			pinPos = board.getPinPos(ID);
			List<Position> l = new ArrayList<>();
			l = findPossiblePos(board, l, pinPos);
			pph.setRating(l.size() + (map_treToGo.size() + ID - id) % map_treToGo.size() + map_treToGo.get(ID));
		}
	}

	private void sealEndPos(int ID) {
		Position endPos = betterBoard.findTreasure(TreasureType.valueOf("START_0" + ID));
		for (PinPosHelp pph : list_rating) {
			Board board = (Board) betterBoard.clone();
			board.proceedShift(pph.getCardHelp());
			List<Position> l = new ArrayList<>();
			l = findPossiblePos(board, l, endPos);
			pph.setRating(-5 + l.size() + (map_treToGo.size() + ID - id) % map_treToGo.size());
		}
	}

	/**
	 * proceed turn with hopefully bad consequences for the opponent
	 * 
	 * @param list
	 *            of solutions
	 * @return list of solutions with annoying factor
	 */
	private void beAnnoying() {
		for (PinPosHelp pp : list_rating) {
			int c = 0;
			Board b = (Board) betterBoard.clone();
			CardHelp ch = pp.getCardHelp();
			Card shift = ch.getCard();
			Position shiftPos = ch.getPos();
			b.proceedShift(shiftPos, new Card(shift));
			outer: for (int i = 1; i < nextPlayer.length; i++) {
				Position next = b.getPinPos(nextPlayer[i]);
				if (i == 1 && analyse(b, next, shift)) {
					break outer;
				}
				c += (nextPlayer.length - i + 1);
			}
			pp.setRating(10 - c);
		}
	}

	/**
	 * analyse if the opponent shift is estimated good or bad for him
	 * 
	 * @param board
	 * @param position
	 *            of the player to analyse
	 * @return percent how much treasures he can reach
	 */
	private boolean analyse(Board b, Position player, Card shift) {
		List<Position> list = new ArrayList<>();
		final int number = 24;
		double pro_treFound = 100. / number * list_foundTreasures.size();

		int treFound = 0;
		int treFind = 0;
		list = findPossiblePos(b, list, player);
		for (Position p : list) {
			TreasureType t = b.getCard(p.getRow(), p.getCol()).getTreasure();
			if (t != null) {
				if (list_foundTreasures.contains(t) || t.equals(betterBoard.getTreasure())) {
					treFound++;
				} else {
					treFind++;
				}
			}
		}

		if (treFound > treFind && treFind <= pro_treFound * (treFound + treFind)) {
			return true;
		} else {
			return false;
		}
	}

	public List<CardHelp> lastChance() {
		wif_v2.writeln("lastChance");
		int nextPlayer = this.nextPlayer[0];
		List<Position> l = new ArrayList<>();
		List<CardHelp> l_sol = new ArrayList<>();
		Board board;
		TreasureType tt = TreasureType.valueOf("START_0" + nextPlayer);
		Position end = betterBoard.findTreasure(tt);
		wif_v2.writeln(end.toString());
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
							pos = true;
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
	}

	private PinPosHelp emergencyPlan(List<CardHelp> list, TreasureType tre) {
		wif_v2.writeln("emergencyPlan " + map_treToGo.get(id));
		System.out.println("emergencyPlan " + map_treToGo.get(id));
		if (nextPlayer.length == 1 && map_treToGo.get(id) == 1) {
			return strategyLastTreasure(list);
		}
		List<PinPosHelp> list_pph = simpleSolution(list, tre, id);
		if (list_pph.size() == 0) {
			list_pph = shortestPath(list);
			if (list_pph.size() == 0) {
				Position p = emergencyTreIsOnShift(list);
				list_pph = nearBy(p, list);
			}
		}
		list_rating = list_pph;
		return bestMove();
	}

	/**
	 * in 1vs1 go over the opposite
	 * 
	 * @param list
	 * @return
	 */
	private PinPosHelp strategyLastTreasure() {
		wif_v2.writeln("strategyLastTreasure");
		List<Position> list_pos = getNeighbours(id);
		list_pos.remove(betterBoard.getForbidden());
		List<Card> list_c = betterBoard.getShiftCard().getPossibleRotations();
		List<Position> l = new ArrayList<>();
		List<PinPosHelp> list_end = new ArrayList<>();
		List<PinPosHelp> list_near = new ArrayList<>();
		for (Card c : list_c) {
			for (Position pos : list_pos) {
				Board b = (Board) betterBoard.clone();
				b.proceedShift(pos, new Card(c));
				l.clear();
				l = findPossiblePos(b, l, b.getPinPos(id));
				if (l.contains(pos.getOpposite())) {
					list_end.add(new PinPosHelp(pos.getOpposite(), pos.getOpposite(), new CardHelp(c, pos)));
				} else {
					list_near.add(new PinPosHelp(pos.getOpposite(), null, new CardHelp(c, pos)));
				}
			}
		}
		wif_v2.writeln("list_end " + list_end.size());
		wif_v2.writeln("list_near " + list_near.size());
		wif_v2.writeln("list_rating " + list_rating.size());
		if (list_end.size() > 0) {
			if (list_end.size() == 1) {
				return list_end.get(0);
			}
			list_rating = list_end;
			sealEndPos(nextPlayer[0]);
			return PinPosHelp.getLowestRating(list_rating);
		} else {
			List<PinPosHelp> l_pph = shortestPathToOpposite(list_near);
			if (l_pph.size() == 0) {
				deadEnd();
				list_rating = sealOther_single();
				beAnnoying();
				wif_v2.writeln("nach sealOtherSingle " + list_rating.size());
				return PinPosHelp.getLowestRating(list_rating);
			} else {
				list_rating = l_pph;
				checkLastButOne();
				beAnnoying();
				sealAway();
				PinPosHelp pph = PinPosHelp.getLowestRating(list_rating);
				return pph;
			}
		}
	}

	private void sealOther() {
		sealAway();
		for (int i = 1; i < nextPlayer.length; i++) {
			if (map_treToGo.get(nextPlayer[i]) == 1) {
				sealEndPos(nextPlayer[i]);
			}
			sealAway(nextPlayer[i]);
		}
	}

	private List<PinPosHelp> sealOther_single() {
		wif_v2.write("sealOtherSingle");
		List<Position> l = new ArrayList<>();
		List<PinPosHelp> list = new ArrayList<>();
		List<Card> list_c = betterBoard.getShiftCard().getPossibleRotations();
		for (Card c : list_c) {
			for (int i = 1; i < 6; i += 2) {
				for (int k = 0; k < 7; k += 6) {
					for (int j = 0; j < 2; j++) {
						Board board = (Board) betterBoard.clone();
						Position shiftPos = new Position(k + (i - k) * j, i + (k - i) * j);
						if (betterBoard.getForbidden() != null && shiftPos.equals(new Position(betterBoard.getForbidden()))) {
							continue;
						}
						board.proceedShift(shiftPos, new Card(c));
						List<Card> list_c2 = board.getShiftCard().getPossibleRotations();
						for (Card c2 : list_c2) {
							for (int i2 = 1; i2 < 6; i2 += 2) {
								for (int k2 = 0; k2 < 7; k2 += 6) {
									for (int j2 = 0; j < 2; j++) {
										Board b = (Board) board.clone();
										Position shiftPos2 = new Position(k2 + (i2 - k2) * j2, i2 + (k2 - i2) * j2);
										if (b.getForbidden() != null && shiftPos2.equals(new Position(betterBoard.getForbidden()))) {
											continue;
										}
										b.proceedShift(new CardHelp(c2, shiftPos2));
										Position oldPinPos = board.getPinPos(nextPlayer[0]);
										l.clear();
										l = findPossiblePos(board, l, oldPinPos);
										list.add(new PinPosHelp(null, board.getPinPos(id), new CardHelp(c, shiftPos), l.size()));
									}
								}
							}
						}
					}
				}
			}
		}
		wif_v2.writeln(list.size() + "");
		return list;
	}

	/**
	 * return list of neighbour of the start card of the given player
	 * 
	 * @param id
	 * @return
	 */
	public List<Position> getNeighbours(int id) {
		Position end = betterBoard.findTreasure(TreasureType.valueOf("START_0" + id));
		wif_v2.writeln(TreasureType.valueOf("START_0" + id) + "");
		wif_v2.writeln(end + "");
		Position end1 = end.clone();
		Position end2 = end.clone();
		if (end.getCol() == 0) {
			end1.setCol(1);
			end2.setCol(0);
		} else {
			end1.setCol(5);
			end2.setCol(6);
		}

		if (end.getRow() == 0) {
			end1.setRow(0);
			end2.setRow(1);
		} else {
			end1.setRow(6);
			end2.setRow(5);
		}
		wif_v2.writeln("getNeighbours " + end + end1 + end2);
		List<Position> list_pos = new ArrayList<>();
		list_pos.add(end1);
		list_pos.add(end2);
		return list_pos;
	}

	/**
	 * in 1vs1 go over the opposite
	 * 
	 * @param list
	 * @return
	 */
	private PinPosHelp strategyLastTreasure(List<CardHelp> list) {
		List<Position> list_pos = getNeighbours(id);
		list_pos.remove(betterBoard.getForbidden());
		List<Position> l = new ArrayList<>();
		List<PinPosHelp> list_end = new ArrayList<>();
		List<PinPosHelp> list_near = new ArrayList<>();
		for (CardHelp ch : list) {
			if (list_pos.contains(ch.getPos())) {
				Board b = (Board) betterBoard.clone();
				b.proceedShift(ch);
				l.clear();
				l = findPossiblePos(b, l, b.getPinPos(id));
				Position pos = ch.getPos();
				if (l.contains(pos.getOpposite())) {
					list_end.add(new PinPosHelp(pos.getOpposite(), pos.getOpposite(), ch));
				} else {
					list_near.add(new PinPosHelp(pos.getOpposite(), null, ch));
				}
			}
		}
		if (list_end.size() > 0) {
			if (list_end.size() == 1) {
				return list_end.get(0);
			}
			list_rating = list_end;
			for (int i : nextPlayer) {
				sealEndPos(i);
			}
			return PinPosHelp.getLowestRating(list_rating);
		} else {
			List<PinPosHelp> l_pph = shortestPathToOpposite(list_near);
			list_rating = l_pph;
			checkLastButOne();
			beAnnoying();
			sealOther();
			PinPosHelp pph = PinPosHelp.getLowestRating(list_rating);
			return pph;
		}
	}

	/**
	 * find the nearest pin position for the given position p and the list of cardHelp
	 * 
	 * @param p
	 * @param list_ch
	 * @return
	 */
	private List<PinPosHelp> nearBy(Position p, List<CardHelp> list_ch) {
		wif_v2.writeln("nearBy");
		List<PinPosHelp> list_pph = new ArrayList<>();
		int diff = Integer.MAX_VALUE;
		for (CardHelp ch : list_ch) {
			Board board = (Board) betterBoard.clone();
			board.proceedShift(ch.getPos(), new Card(ch.getCard()));
			Position pinPos = board.getPinPos(id);
			int tmp = pinPos.diff(p);
			if (tmp < diff) {
				list_pph.clear();
				list_pph.add(new PinPosHelp(p, pinPos, ch));
				diff = tmp;
			} else {
				if (tmp == diff) {
					list_pph.add(new PinPosHelp(p, pinPos, ch));
				}
			}
		}
		return list_pph;
	}

	/**
	 * returns the position where the shiftcard with the treasure will probably shift in
	 * 
	 * @param list
	 * @return
	 */
	private Position emergencyTreIsOnShift(List<CardHelp> list) {
		wif_v2.writeln("emergencyTreIsOnShift " + list.size());
		TreasureType tre = null;
		switch (this.nextPlayer[0]) {
		case 1:
			tre = betterBoard.getCard(0, 0).getTreasure();
			break;
		case 4:
			tre = betterBoard.getCard(6, 6).getTreasure();
			break;
		case 2:
			tre = betterBoard.getCard(0, 6).getTreasure();
			break;
		case 3:
			tre = betterBoard.getCard(6, 0).getTreasure();
			break;
		}

		HashMap<Integer, Integer> map = new HashMap<>();

		map.put(row0, 0);
		map.put(row6, 0);
		map.put(col0, 0);
		map.put(col6, 0);
		for (CardHelp ch : list) {
			Board board = (Board) betterBoard.clone();
			board.proceedShift(ch.getPos(), new Card(ch.getCard()));
			simpleSolution(board, tre, this.nextPlayer[0]);
			PinPosHelp pph = nextStep(this.nextPlayer[0]);
			Position pos = pph.getCardHelp().getPos();
			if (pos.getRow() == 0) {
				map.put(row0, map.get(row0) + 1);
			} else if (pos.getCol() == 0) {
				map.put(col0, map.get(col0) + 1);
			} else if (pos.getRow() == 6) {
				map.put(row6, map.get(row6) + 1);
			} else if (pos.getCol() == 6) {
				map.put(col6, map.get(col6) + 1);
			}
		}

		int max1 = 0;
		int max2 = 0;
		List<Integer> key1 = new ArrayList<>();
		List<Integer> key2 = new ArrayList<>();
		for (Integer i : map.keySet()) {
			if (map.get(i) > max1) {
				key2 = key1;
				max2 = max1;
				key1.clear();
				key1.add(i);
				max1 = map.get(i);
			}
			if (map.get(i) == max1) {
				key1.add(i);
			}
			if (map.get(i) == max2) {
				key2.add(i);
			}
		}

		int pos1;
		int pos2;
		switch (key1.size()) {
		case 1:
			pos1 = key1.get(0);
			switch (key2.size()) {
			case 1:
				if ((key2.get(0) + pos1) % 7 == 0) {
					pos2 = 3;
				} else {
					pos2 = key2.get(0);
				}
				break;
			case 2:
				if ((key2.get(0) + key2.get(1)) % 7 == 0) {
					pos2 = 3;
				} else {
					if ((key2.get(0) + pos1) % 7 == 0) {
						pos2 = key2.get(1);
					} else {
						pos2 = key2.get(0);
					}
				}
				break;
			default:
				pos2 = 3;
			}

		case 3:
			pos2 = 3;
			if ((key1.get(0) + key1.get(1) % 7 == 0)) {
				pos1 = key1.get(2);
			} else {
				if ((key1.get(0) + key1.get(2) % 7 == 0)) {
					pos1 = key1.get(1);
				} else {
					pos1 = key1.get(0);
				}
			}
			break;
		case 2:
			if ((key1.get(0) + key1.get(1) % 7 == 0)) {
				pos1 = 3;
				pos2 = 3;
			} else {
				pos1 = key1.get(0);
				pos2 = key2.get(1);
			}
			break;
		default:
			pos1 = 3;
			pos2 = 3;
		}

		Position p = new Position(3, 3);
		switch (pos1) {
		case row0:
			p.setRow(0);
			break;
		case row6:
			p.setRow(6);
			break;
		case col0:
			p.setCol(0);
			break;
		case col6:
			p.setCol(6);
			break;
		}
		switch (pos2) {
		case row0:
			p.setRow(0);
			break;
		case row6:
			p.setRow(6);
			break;
		case col0:
			p.setCol(0);
			break;
		case col6:
			p.setCol(6);
			break;
		}

		return p;
	}

	/**
	 * check if the next player has one treasure left and if tries to deny the win
	 * 
	 * @param tre
	 * @return
	 */
	private PinPosHelp checkNextTurn(TreasureType tre) {
		if (map_treToGo.get(nextPlayer[0]) == 1) {
			List<CardHelp> list_ch = lastChance();
			wif_v2.writeln("checkNextTurn");
			wif_v2.writeList(list_ch);
			wif_v2.writeln("checkNextTurn ende");
			switch (list_ch.size()) {
			case 0:
				System.out.println("Player " + nextPlayer[0] + " will win. I (" + id + ") admit my defeat.");
				wif_v2.writeln("Player " + nextPlayer[0] + " will win. I (" + id + ") admit my defeat.");
				wif_player.writeln("Player " + nextPlayer[0] + " will win. I (" + id + ") admit my defeat.");
				List<PinPosHelp> l_pph = simpleSolution(tre, id);

				if (l_pph.size() == 0) {
					return nextStep(id);
				} else {
					if (l_pph.size() == 1) {
						return l_pph.get(0);
					}
					list_rating = l_pph;
					beAnnoying();
					sealAway();
					return PinPosHelp.getLowestRating(list_rating);
				}
			case 1:
				List<PinPosHelp> list_pph = simpleSolution(list_ch, tre, id);
				switch (list_pph.size()) {
				case 0:
					list_pph = shortestPath(list_ch);
					if (list_pph.size() == 0) {
						list_pph = nearBy(new Position(3, 3), list_ch);
					}
					break;
				case 1:
					return list_pph.get(0);
				}
				return bestMove();
			default:
				return emergencyPlan(list_ch, tre);
			}
		}
		return null;
	}

	private PinPosHelp bestMove() {
		wif_v2.writeln("bestMove " + list_rating.size());
		chooseOrientation((map_treToGo.get(id) == 1));
		reject(8);
		sealOther();
		beAnnoying();
		return PinPosHelp.getLowestRating(list_rating);
	}

	/**
	 * find best position to find the treasure in the next step
	 * 
	 * @param treasure
	 * @return PinPosHelp
	 */
	private PinPosHelp nextStep(int ID) {
		wif_v2.writeln("nextStep");
		chooseOrientation((map_treToGo.get(ID) == 1));
		reject(8);
		sealOther();
		beAnnoying();
		return PinPosHelp.getLowestRating(list_rating);
	}

	/**
	 * in case of a dead end although every possible shift position with each possible rotation of the shift Card was checked
	 * 
	 * @return
	 */
	private void deadEnd() {
		wif_v2.writeln("dead end");
		List<Position> l = new ArrayList<>();
		List<PinPosHelp> l_sol = new ArrayList<>();
		Map<Double, List<PinPosHelp>> map = new HashMap<>();
		TreasureType tre = betterBoard.getTreasure();
		Board board;
		Position trePos;
		Position shiftPos;
		CardHelp ch;

		List<Card> list_c = betterBoard.getShiftCard().getPossibleRotations();
		for (Card c : list_c) {
			for (int i = 1; i < 6; i += 2) {
				for (int k = 0; k < 7; k += 6) {
					for (int j = 0; j < 2; j++) {
						board = (Board) betterBoard.clone();
						shiftPos = new Position(k + (i - k) * j, i + (k - i) * j);
						if (betterBoard.getForbidden() != null && shiftPos.equals(new Position(betterBoard.getForbidden()))) {
							continue;
						}
						board.proceedShift(shiftPos, new Card(c));
						trePos = board.findTreasure(tre);
						if (trePos != null) {
							ch = new CardHelp(c, shiftPos);
							List<Card> list_c2 = betterBoard.getShiftCard().getPossibleRotations();
							for (Card c2 : list_c2) {
								for (int i2 = 1; i2 < 6; i2 += 2) {
									for (int k2 = 0; k2 < 7; k2 += 6) {
										for (int j2 = 0; j2 < 2; j2++) {
											Board board2 = (Board) board.clone();
											Position shiftPos2 = new Position(k2 + (i2 - k2) * j2, i2 + (k2 - i2) * j2);
											if (board.getForbidden() != null && shiftPos2.equals(new Position(board2.getForbidden()))) {
												continue;
											}
											Position oldPinPos2 = board2.getPinPos(id);
											board2.proceedShift(shiftPos2, new Card(c2));
											trePos = board.findTreasure(tre);
											if (trePos != null) {
												l.clear();
												l.add(oldPinPos2);
												l = findPossiblePos(board2, l, oldPinPos2);
												l_sol = shortestPath(l, trePos, ch, board2);
												if (map.containsKey(l_sol.get(0).getRating())) {
													map.get(l_sol.get(0).getRating()).addAll(l_sol);
												} else {
													map.put(l_sol.get(0).getRating(), l_sol);
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
		}
		double min = Collections.min(map.keySet());
		list_rating = map.get(min);
		for (PinPosHelp pph : list_rating) {
			Board b = ((Board) betterBoard.clone());
			b.proceedShift(pph.getCardHelp());
			pph.setPinPos(b.getPinPos(id));
		}
		wif_v2.writeln("Dead End");
	}

	/**
	 * take a card where the openings look in the direction of the treasure and it's openings.
	 * 
	 * @param list
	 * @return
	 */
	private void chooseOrientation(boolean lastTreasure) {
		wif_v2.writeln("chooseOrientaion " + list_rating.size());
		for (PinPosHelp pph : list_rating) {
			Position p = pph.getPinPos();
			Board b = (Board) betterBoard.clone();
			b.proceedShift(pph.getCardHelp().getPos(), new Card(pph.getCardHelp().getCard()));
			Position trePos = betterBoard.findTreasure(betterBoard.getTreasure());
			if (trePos == null) {
				continue;
			}
			int count = chooseOrientation(pph.getTrePos(), p, b);
			if (lastTreasure) {
				pph.setRating(-count);
			}
			pph.setRating(-count);
		}
	}

	/**
	 * returns max
	 * 
	 * @param start
	 * @param end
	 * @param board
	 * @return
	 */
	private int chooseOrientation(Position start, Position end, Board board) {
		Card cstart = new Card(board.getCard(start));
		Card cend = new Card(board.getCard(end));
		int count = 0;
		if (start.getRow() == end.getRow()) {
			if (start.getCol() > end.getCol() && cstart.getOpenings().isLeft()) {
				count++;
				if (cend.getOpenings().isRight()) {
					count++;
				}
			} else if (start.getCol() < end.getCol() && cstart.getOpenings().isRight()) {
				count++;
				if (cend.getOpenings().isLeft()) {
					count++;
				}
			}
		} else

		if (start.getCol() == end.getCol()) {
			if (start.getRow() > end.getRow() && cstart.getOpenings().isTop()) {
				count++;
				if (cend.getOpenings().isBottom()) {
					count++;
				}
			} else if (start.getRow() < end.getRow() && cstart.getOpenings().isBottom()) {
				count++;
				if (cend.getOpenings().isTop()) {
					count++;
				}
			}
		} else

		if (start.getCol() > end.getCol()) {
			if (cstart.getOpenings().isTop()) {
				count++;
			}
			if (start.getRow() > end.getRow() && cstart.getOpenings().isLeft()) {
				count++;
			} else if (start.getRow() > end.getRow() && cstart.getOpenings().isRight()) {
				count++;
			}
		} else

		if (start.getCol() < end.getCol()) {
			if (cstart.getOpenings().isBottom()) {
				count++;
			}
			if (start.getRow() > end.getRow() && cstart.getOpenings().isLeft()) {
				count++;
			} else if (start.getRow() > end.getRow() && cstart.getOpenings().isRight()) {
				count++;
			}
		}

		return count;
	}

	/**
	 * calculate the best pin position for the given Card Help, does not check if the treasure is reachable
	 * 
	 * @param list_ch
	 * @return List<PinPosHelp>
	 */
	private List<PinPosHelp> shortestPath(List<CardHelp> list_ch) {
		wif_v2.writeln("shortestPath list_ch");
		List<PinPosHelp> l_sol = new ArrayList<>();
		List<Position> list_pos = new ArrayList<>();
		Position trePos;
		int min = Integer.MAX_VALUE;
		for (CardHelp ch : list_ch) {
			Board b = (Board) betterBoard.clone();
			b.proceedShift(ch.getPos(), new Card(ch.getCard()));
			trePos = b.findTreasure(betterBoard.getTreasure());
			if (trePos == null) {
				continue;
			}
			list_pos.clear();
			list_pos = findPossiblePos(b, list_pos, b.getPinPos(id));
			for (Position p : list_pos) {
				int diff = diff(p, trePos, b);
				if (diff < min) {
					wif_v2.writeNewLine(1);
					wif_v2.write(" " + p);
					min = diff;
					l_sol.clear();
					l_sol.add(new PinPosHelp(trePos, p, ch));
				} else {
					if (diff == min) {
						wif_v2.write(" " + p);
						l_sol.add(new PinPosHelp(trePos, p, ch));
					}
				}
			}
		}
		wif_v2.writeNewLine(1);
		return l_sol;
	}

	/**
	 * calculate the best pin position for the given PinPosHelp to the given Position, does not check if the position is reachable
	 * 
	 * @param list_ch
	 * @return List<PinPosHelp>
	 */
	private List<PinPosHelp> shortestPathToOpposite(List<PinPosHelp> list) {
		wif_v2.writeln("shortestPathToOpposite list pinPosHelp " + list.size());
		List<PinPosHelp> l_sol = new ArrayList<>();
		List<Position> list_pos = new ArrayList<>();
		int min = Integer.MAX_VALUE;
		for (PinPosHelp pph : list) {
			CardHelp ch = pph.getCardHelp();
			Board b = (Board) betterBoard.clone();
			b.proceedShift(ch.getPos(), new Card(ch.getCard()));
			list_pos.clear();
			list_pos = findPossiblePos(b, list_pos, b.getPinPos(id));
			wif_v2.write(list_pos.size() + "  ");
			for (Position p : list_pos) {
				wif_v2.writeln("shortestPathToOpposite: " + ch.debug() + " " + ch.getPos().getOpposite());
				int diff = diff(p, ch.getPos().getOpposite(), b);
				if (diff < min) {
					wif_v2.writeNewLine(1);
					wif_v2.write(" " + p);
					min = diff;
					l_sol.clear();
					l_sol.add(new PinPosHelp(ch.getPos().getOpposite(), p, ch));
				} else {
					if (diff == min) {
						wif_v2.write(" " + p);
						l_sol.add(new PinPosHelp(ch.getPos().getOpposite(), p, ch));
					}
				}
			}
		}
		wif_v2.writeNewLine(1);
		return l_sol;
	}

	private int diff(Position start, Position end, Board b) {
		int diff = (Math.abs(start.getRow() - end.getRow()) + Math.abs(start.getCol() - end.getCol()));
		int chooseOrientation = chooseOrientation(start, end, b);
		return diff + 6 - chooseOrientation;
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
	private List<PinPosHelp> shortestPath(List<Position> list, Position trePos, CardHelp ch, Board b) {
		wif_v2.writeln("shortestPath lsit list");
		List<PinPosHelp> pos = new ArrayList<>();
		int diff = X * Y;
		for (Position p : list) {
			int tmp = diff(p, trePos, b);
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
	 * check if with the given CardHelp the treasure is reachable
	 * 
	 * @param list_ch
	 * @param tre
	 * @return list of all CardHelp who can reach their treasure
	 */
	private List<PinPosHelp> simpleSolution(List<CardHelp> list_ch, TreasureType tre, int id) {
		wif_v2.writeln("simplesolution list");
		List<Position> l = new ArrayList<>();
		List<PinPosHelp> l_sol = new ArrayList<>();
		Board board;
		Position trePos;
		Position oldPinPos;

		for (CardHelp ch : list_ch) {
			board = (Board) betterBoard.clone();
			board.proceedShift(ch.getPos(), new Card(ch.getCard()));
			oldPinPos = board.getPinPos(id);
			trePos = board.findTreasure(tre);
			if (trePos != null) {
				l.clear();
				l = findPossiblePos(board, l, oldPinPos);
				if (l.contains(trePos)) {
					l_sol.add(new PinPosHelp(trePos, trePos, ch));
				}
			}
		}
		return l_sol;
	}

	/**
	 * calculate all CardHelp who can reach the given treasure
	 * 
	 * @param tre
	 * @param id
	 * @return list with PinPosHelp(trePos, newPinPos, CardHelp)
	 */
	private List<PinPosHelp> simpleSolution(TreasureType tre, int id) {
		wif_v2.writeln("simpleSolution tre");
		boolean already = false;
		List<Position> l = new ArrayList<>();
		List<Position> l_rev = new ArrayList<>();
		List<PinPosHelp> l_sol = new ArrayList<>();
		Board board;
		Position trePos;
		Position oldPinPos;
		Position shiftPos;
		CardHelp ch;

		List<Card> list_c = betterBoard.getShiftCard().getPossibleRotations();
		for (Card c : list_c) {
			for (int i = 1; i < 6; i += 2) {
				for (int k = 0; k < 7; k += 6) {
					for (int j = 0; j < 2; j++) {
						board = (Board) betterBoard.clone();
						c.setPin(new Pin());
						shiftPos = new Position(k + (i - k) * j, i + (k - i) * j);
						if (betterBoard.getForbidden() != null && shiftPos.equals(new Position(betterBoard.getForbidden()))) {
							continue;
						}
						board.proceedShift(shiftPos, new Card(c));
						oldPinPos = board.getPinPos(id);
						trePos = board.findTreasure(tre);
						if (trePos != null) {
							l.clear();
							l = findPossiblePos(board, l, oldPinPos);
							l.add(oldPinPos);
							l_rev.clear();
							l_rev = findPossiblePos(board, l_rev, trePos);
							l_rev.add(trePos);
							ch = new CardHelp(c, shiftPos);
							if (l.contains(trePos)) {
								already = true;
								l_sol.add(new PinPosHelp(trePos, trePos, ch));
							}
							if (!already) {
								calcData(oldPinPos, trePos, ch, l, l_rev, board);
							}
						}
					}
				}
			}
		}
		return l_sol;
	}

	/**
	 * calculate all CardHelp who can reach the given treasure
	 * 
	 * @param tre
	 * @param id
	 * @return list with PinPosHelp(trePos, newPinPos, CardHelp)
	 */
	private List<PinPosHelp> simpleSolution(Board b, TreasureType tre, int id) {
		wif_v2.writeln("simpleSolution tre with board");
		boolean already = false;
		List<Position> l = new ArrayList<>();
		List<Position> l_rev = new ArrayList<>();
		List<PinPosHelp> l_sol = new ArrayList<>();
		Board board;
		Position trePos;
		Position oldPinPos;
		Position shiftPos;
		CardHelp ch;

		List<Card> list_c = b.getShiftCard().getPossibleRotations();
		for (Card c : list_c) {
			for (int i = 1; i < 6; i += 2) {
				for (int k = 0; k < 7; k += 6) {
					for (int j = 0; j < 2; j++) {
						board = (Board) b.clone();
						c.setPin(new Pin());
						shiftPos = new Position(k + (i - k) * j, i + (k - i) * j);
						if (b.getForbidden() != null && shiftPos.equals(new Position(b.getForbidden()))) {
							continue;
						}
						board.proceedShift(shiftPos, new Card(c));
						oldPinPos = board.getPinPos(id);
						trePos = board.findTreasure(tre);
						if (trePos != null) {
							l.clear();
							l = findPossiblePos(board, l, oldPinPos);
							l.add(oldPinPos);
							l_rev.clear();
							l_rev = findPossiblePos(board, l_rev, trePos);
							l_rev.add(trePos);
							ch = new CardHelp(c, shiftPos);
							if (l.contains(trePos)) {
								already = true;
								l_sol.add(new PinPosHelp(trePos, trePos, ch));
							}
							if (!already) {
								calcData(oldPinPos, trePos, ch, l, l_rev, board);
							}
						}
					}
				}
			}
		}
		return l_sol;
	}

	private void calcData(Position start, Position trePos, CardHelp ch, List<Position> list, List<Position> list_rev, Board b) {
		int diff;
		for (Position p : list) {
			diff = Integer.MAX_VALUE;
			for (Position pr : list_rev) {
				int tmp = diff(pr, p, b) + diff(pr, trePos, b);
				if (tmp < diff) {
					diff = tmp;
				}
			}
			double tmp = diff(p, trePos, b) + diff;
			// wif_v2.writeln("calcData " + trePos + " " + p + " " + ch.debug() + " " + tmp);
			list_rating.add(new PinPosHelp(trePos, p, ch, tmp));
		}
	}

	private List<Position> findPossiblePos(Board b, List<Position> list, Position start) {
		Position p;
		int col_start = start.getCol();
		int row_start = start.getRow();
		Card c = new Card(b.getCard(row_start, col_start));
		if (col_start < X - 1) {
			p = new Position(row_start, col_start + 1);
			if (!list.contains(p) && b.getCard(row_start, col_start + 1).getOpenings().isLeft() && c.getOpenings().isRight()) {
				list.add(p);
				list = (findPossiblePos(b, list, p));
			}
		}
		if (col_start > 0) {
			p = new Position(row_start, col_start - 1);
			if (!list.contains(p) && b.getCard(row_start, col_start - 1).getOpenings().isRight() && c.getOpenings().isLeft()) {
				list.add(p);
				list = (findPossiblePos(b, list, p));
			}
		}
		if (row_start > 0) {
			p = new Position(row_start - 1, col_start);
			if (!list.contains(p) && b.getCard(row_start - 1, col_start).getOpenings().isBottom() && c.getOpenings().isTop()) {
				list.add(p);
				list = (findPossiblePos(b, list, p));
			}
		}
		if (row_start < Y - 1) {
			p = new Position(row_start + 1, col_start);
			if (!list.contains(p) && b.getCard(row_start + 1, col_start).getOpenings().isTop() && c.getOpenings().isBottom()) {
				list.add(p);
				list = (findPossiblePos(b, list, p));
			}
		}
		return list;
	}

}
