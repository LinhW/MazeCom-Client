package gui.view;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;

import config.Settings;

public class AISelector extends JDialog {
	private static final long serialVersionUID = 1623631697132432499L;
	
	private int value;
	
	private JPanel pn_main;
	private JButton bt_accept;
	private JButton bt_abort;
	private JList<String> ls_ais;

	public AISelector() {
		this.value = -1;
		initWindow();
	}
	
	public int showDialog() {
		setVisible(true);
		return value;
	}
	
	private void initWindow() {
		setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		
		pn_main = new JPanel();
		getContentPane().add(pn_main);
		
		ls_ais = new JList<String>(Settings.AIList);
		pn_main.add(ls_ais);
		
		bt_accept = new JButton("Accept");
		pn_main.add(bt_accept);
		
		bt_abort = new JButton("Abort");
		pn_main.add(bt_abort);
		
		pack();
	}
}
