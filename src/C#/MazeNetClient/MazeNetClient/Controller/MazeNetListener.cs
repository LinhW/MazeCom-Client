using System;
using System.Threading;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Xml;
using System.Xml.Serialization;
using System.IO;

namespace MazeNetClient
{
	public class MazeNetListener
	{
		public bool Listening {
			get;
			set;
		}

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

		public TcpClient client;

		public delegate void MazeObjectHandler (MazeNetMessage com);
			public MazeObjectHandler mazeObjHandler;

		public MazeNetListener (IPAddress ipaddr, int port, MazeNetUser muser)
		{
			this.ip = ipaddr;
			this.port = port;
			this.user = muser;
			client = new TcpClient ();
		}

		public bool connect()
		{
			bool res = false;
			try {
				client.Connect (ip, port);
				res = true;
			} catch (Exception ex) {
				throw ex;
			}
			return res;
		}

		public void beginListening()
		{
			Listening = true;
			ThreadStart ts1 = new ThreadStart (new Action (() => {
				NetworkStream stream = client.GetStream ();
				while (Listening) {
					Byte[] tmp = new byte[4];
					stream.Read(tmp, 0, 4);
					int len = 0;
					len |= (tmp[3] & 0xff);
					len <<= 8;
					len |= (tmp[2] & 0xff);
					len <<= 8;
					len |= (tmp[1] & 0xff);
					len <<= 8;
					len |= (tmp[0] & 0xff);
					Byte[] msg = new byte[len];
					stream.Read(msg, 0, len);
					decryptMessage(msg);
				}
			}));
			Thread t1 = new Thread (ts1);
			t1.Start ();
		}

		public void endListening()
		{
			Listening = false;
		}

		private void decryptMessage(Byte[] bytes)
		{
			if (bytes.Length < 1) {
				return;
			}
			string xml = Encoding.UTF8.GetString (bytes);
			XmlSerializer serializer = new XmlSerializer (typeof(MazeCom));

			MazeNetMessage mazeObj = new MazeNetMessage ();
			if (mazeObj.OriginalXML == null) {
				mazeObj.OriginalXML = new XmlDocument ();
			}
			mazeObj.OriginalXML.LoadXml (xml);
			mazeObj.mazeCom = (MazeCom)serializer.Deserialize (new StringReader (xml));
			if (mazeObjHandler != null) {
				mazeObjHandler (mazeObj);
			} else {
				Console.WriteLine("Received: " + mazeObj.mazeCom.mcType.ToString());
			}
		}

		public void send(MazeNetMessage msg)
		{
			if (msg.mazeCom.mcType != MazeComType.LOGIN) {
				msg.mazeCom.id = user.ID;
			}
			XmlSerializer serializer = new XmlSerializer (typeof(MazeCom));
			StringWriter sw = new StringWriter();
			serializer.Serialize (sw, msg.mazeCom);
			byte[] xmlData  = Encoding.UTF8.GetBytes(sw.GetStringBuilder().ToString()
			                                         .Replace("utf-16", "utf-8\" standalone=\"yes")
			                                         .Replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"", ""));
			byte[] sendData = new byte[xmlData.Length + 4];

			int len=xmlData.Length;
			sendData[0] = (byte) (len & 0xff);
				len >>= 8;
			sendData[1] = (byte) (len & 0xff);
			len >>= 8;
			sendData[2] = (byte) (len & 0xff);
			len >>= 8;
			sendData[3] = (byte) (len & 0xff);

			for (int i = 0; i < xmlData.Length; i++) {
				sendData [i + 4] = xmlData [i];
			}
			client.GetStream ().Write (sendData, 0, sendData.Length);
			client.GetStream ().Flush ();
		}
	}
}

