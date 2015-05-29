package gui.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import control.Settings;

public class AISelector extends JDialog implements ActionListener {
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
		setModal(true);
		setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		setLocationRelativeTo(null);

		pn_main = new JPanel();
		getContentPane().add(pn_main);

		ls_ais = new JList<String>(Settings.AIList);
		ls_ais.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ls_ais.setSelectedIndex(0);

		pn_main.add(ls_ais);

		bt_accept = new JButton("Accept");
		bt_accept.addActionListener(this);
		bt_accept.setActionCommand("accept");
		pn_main.add(bt_accept);

		bt_abort = new JButton("Abort");
		bt_abort.addActionListener(this);
		bt_abort.setActionCommand("abort");
		pn_main.add(bt_abort);

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "accept":
			value = ls_ais.getSelectedIndex();
			this.dispose();
			break;
		case "abort":
			value = -1;
			this.dispose();
			break;
		}
	}
}
