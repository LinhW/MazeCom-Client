package view.testClasses.userInterface;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class StreamToTextArea extends OutputStream {

	private final JTextArea textArea;
	
	private final StringBuilder sb = new StringBuilder();
	
	   public StreamToTextArea(final JTextArea textArea) {
		      this.textArea = textArea;
	   }
	
	@Override
	public void write(int b) throws IOException {
	      if (b == '\r')
	          return;

	       if (b == '\n') {
	          final String text = sb.toString() + "\n"; //$NON-NLS-1$
	          SwingUtilities.invokeLater(new Runnable() {
	             public void run() {
	                textArea.append(text);
	                textArea.setCaretPosition(textArea.getDocument().getLength());
	             }
	          });
	          sb.setLength(0);
	          return;
	       }

	       sb.append((char) b);
	       
	}

	public JTextArea getTextArea() {
		return textArea;
	}

}
