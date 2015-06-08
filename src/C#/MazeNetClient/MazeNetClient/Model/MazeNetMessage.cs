using System;
using System.IO;
using System.Xml;
using System.Text;

namespace MazeNetClient
{
	public class MazeNetMessage
	{
		public XmlDocument OriginalXML {
			get;
			set;
		}
		public MazeCom mazeCom {
			get;
			set;
		}

		public MazeNetPosition[][] board {
			get;
			set;
		}

		public double PlayerTreasureDistance {
			get;
			set;
		}

		public bool annoying {
			get;
			set;
		}

		public override string ToString()
		{
			StringBuilder sb = new StringBuilder ();
			switch (mazeCom.mcType) {
			case MazeComType.MOVE:
				MoveMessageType mType = (MoveMessageType)(mazeCom.Item);
				sb.Append ("[Type: MOVE]");
				sb.Append ("[ col: ");
				sb.Append (mType.shiftPosition.col);
				sb.Append (" row: ");
				sb.Append (mType.shiftPosition.row);
				sb.Append (" ] \nTile:\n");
				MazeNetPosition mp = new MazeNetPosition (0, 0, mType.shiftCard);
				sb.Append (MazeNetUtil.posToString (mp));
				sb.Append ("[ px: ");
				sb.Append (mType.newPinPos.col);
				sb.Append (" py: ");
				sb.Append (mType.newPinPos.row);
				sb.Append (" ]");
				break;
			default:
				break;
			}
			return sb.ToString ();
		}
	}
}

