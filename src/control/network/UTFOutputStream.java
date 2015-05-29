package control.network;

import java.io.IOException;
import java.io.OutputStream;
 
public class UTFOutputStream {
 
  private OutputStream os;
 
  public UTFOutputStream(OutputStream stream) {
    os = stream;
  }
 
  public void writeUTF8(String text) throws IOException
  {
    byte[] bytes = text.getBytes("UTF-8");
    int len=bytes.length;
 
    byte[] lenbuf = new byte[4];
    lenbuf[0] = (byte) (len & 0xff);
    len >>= 8;
    lenbuf[1] = (byte) (len & 0xff);
    len >>= 8;
    lenbuf[2] = (byte) (len & 0xff);
    len >>= 8;
    lenbuf[3] = (byte) (len & 0xff);
 
    os.write(lenbuf);
    os.write(bytes);
  }
 
  public void flush() throws IOException {
    os.flush();
  }
 
  public void close() throws IOException {
    this.os.close();
  }
}