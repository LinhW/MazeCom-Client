using System;
using System.Net;
using System.Net.Sockets;

namespace MazeNetClient
{
	public class MazeNetController
	{
		public MazeNetUser user {
			get;
			set;
		}

		public IPAddress ip {
			get;
			set;
		}

		public int port {
			get;
			set;
		}

		public MazeNetListener listener {
			get;
			set;
		}

		public bool connected {
			get{
				return listener.client.Connected;
			}
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

		public MazeNetKI ki {
			get;
			set;
		}

		public MazeNetController (IPAddress ipaddr, int port, MazeNetUser muser)
		{
			this.ip = ipaddr;
			this.port = port;
			this.user = muser;
			listener = new MazeNetListener (ip, port, user);
			listener.mazeObjHandler = new MazeNetListener.MazeObjectHandler (messageHandler);
		}

		void messageHandler (MazeNetMessage com)
		{
			Console.WriteLine ("Received: " + com.mazeCom.mcType);
			switch (com.mazeCom.mcType) {
			case MazeComType.ACCEPT:
				if ((com.mazeCom.Item as AcceptMessageType).errorCode != ErrorType.NOERROR) {
					Console.WriteLine("ERROR: " + (com.mazeCom.Item as AcceptMessageType).errorCode);
				}
				break;
			case MazeComType.AWAITMOVE:
				this.activeBoard = (com.mazeCom.Item as AwaitMoveMessageType).board;
				this.treasuresToGo = (com.mazeCom.Item as AwaitMoveMessageType).treasuresToGo [this.user.ID - 1];
				this.treasure = (com.mazeCom.Item as AwaitMoveMessageType).treasure;
				if (ki == null) {
					ki = new MazeNetKI (treasure, treasuresToGo, activeBoard, user);
				} else {
					ki.activeBoard = activeBoard;
					ki.treasure = treasure;
				}
				send(ki.calculateNextMove ());
				break;
			case MazeComType.DISCONNECT:
				break;
			case MazeComType.LOGIN:
				break;
			case MazeComType.LOGINREPLY:
				this.user.ID = (com.mazeCom.Item as LoginReplyMessageType).newID;
				break;
			case MazeComType.MOVE:
				break;
			case MazeComType.WIN:
				if (((WinMessageType)(com.mazeCom.Item)).winner.id == user.ID) {
					Console.Write ("WINNERID=" + user.ID);
				} else {
					Console.Write ("STILL ALIVE ");
				}
				System.Environment.Exit(1);
				break;
			default:
				break;
			}
		}

		public bool start()
		{
			if (listener.connect ()) {
				listener.beginListening ();
			}
			return connected;
		}


		public void send(MazeNetMessage msg)
		{
			listener.send (msg);
		}
	}
}

