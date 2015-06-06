package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Timer extends Thread {
	private long delay;
	private long initdelay;
	private List<ActionListener> list;
	private boolean stop = false;
	private boolean init = true;

	@Override
	public void run() {
		try {
			if (init) {
				TimeUnit.MILLISECONDS.sleep(initdelay);
				init = false;
			}
			for (ActionListener listener : list) {
				listener.actionPerformed(new ActionEvent(this, list.indexOf(listener), "fireF"));
			}
			TimeUnit.MILLISECONDS.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!stop) {
			run();
		}
	}

	public void stopIt() {
		stop = true;
	}

	public Timer(long shiftDelay, ActionListener listener) {
		this.setDelay(shiftDelay);
		this.initdelay = shiftDelay;
		list = new ArrayList<>();
		this.list.add(listener);
	}

	public void setInitialDelay(long initDelay) {
		this.initdelay = initDelay;
	}

	public long getInitialDelay() {
		return initdelay;
	}

	public void addActionListener(ActionListener listener) {
		this.list.add(listener);
	}

	public boolean removeActionListener(ActionListener listener) {
		return this.list.remove(listener);
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

}
