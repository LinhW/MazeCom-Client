using System;

namespace MazeNetClient
{
	public class MazeNetUser
	{
		public string Username {
			get;
			set;
		}

		public string Tag {
			get;
			set;
		}

		public int ID {
			get;
			set;
		}

		public MazeNetUser (string name, string tag)
		{
			Username = name;
			Tag = tag;
		}
	}
}

