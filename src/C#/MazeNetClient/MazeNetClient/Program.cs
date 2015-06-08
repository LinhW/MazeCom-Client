using System;
using System.Net;

namespace MazeNetClient
{
	class MainClass
	{
		public static void Main (string[] args)
		{
			Console.WriteLine ("Press any key to start...");
			Console.Read ();
			MazeNetUser user = new MazeNetUser ("HAL 9000", "KI");
			IPAddress addr = IPAddress.Parse("127.0.0.1");
			MazeNetController listener = new MazeNetController (addr, 5123, user);
			try {
				listener.start();
				Console.Write("Connection Established.\n");
				MazeCom msg = new MazeCom();
				LoginMessageType login = new LoginMessageType();
				login.name = user.Username;
				msg.id = -1;
				msg.Item = login;
				msg.mcType = MazeComType.LOGIN;
				MazeNetMessage mazeMsg = new MazeNetMessage();
				mazeMsg.mazeCom = msg;
				listener.send(mazeMsg);
			} catch (Exception ex) {
				Console.Write (ex.Message);
			}
		}
	}
}
