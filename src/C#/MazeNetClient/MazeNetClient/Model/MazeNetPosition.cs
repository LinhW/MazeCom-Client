using System;
using System.Linq;
using System.Collections.Generic;

namespace MazeNetClient
{
	public class MazeNetPosition
	{
		public int X {
			get;
			set;
		}

		public int Y {
			get;
			set;
		}

		public cardType Card {
			get;
			set;
		}
		public MazeNetPosition (int x, int y, cardType card)
		{
			X = x;
			Y = y;
			if (card != null) {
				cardType tmpCard = new cardType ();
				cardTypeOpenings tmpOpenings = new cardTypeOpenings ();
				tmpOpenings.bottom = card.openings.bottom;
				tmpOpenings.left = card.openings.left;
				tmpOpenings.right = card.openings.right;
				tmpOpenings.top = card.openings.top;
				tmpCard.openings = tmpOpenings;
				tmpCard.treasure = card.treasure;
				tmpCard.treasureSpecified = card.treasureSpecified;
				List<int> pins = new List<int> ();
				if (card != null) {
					foreach (var item in card.pin) {
						pins.Add (item);
					}
				}
				tmpCard.pin = pins.ToArray();
				Card = tmpCard;
			}
		}
		public positionType getPositionType()
		{
			positionType res = new positionType ();
			res.col = X;
			res.row = Y;
			return res;
		}

		public void addPin(int id)
		{
			foreach (var item in Card.pin) {
				if (item == id) {
					return;
				}
			}
			int[] pins = new int[Card.pin.Length + 1];
			for (int i = 0; i < pins.Length - 1; i++) {
				pins [i] = Card.pin [i];
			}
			pins [pins.Length - 1] = id;
			Card.pin = pins;
		}
	}
}

