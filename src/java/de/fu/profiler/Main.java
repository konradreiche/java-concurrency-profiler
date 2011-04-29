package de.fu.profiler;

import java.io.IOException;

import de.fu.profiler.controller.ProfilerController;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.model.Server;
import de.fu.profiler.view.ProfilerObserver;
import de.fu.profiler.view.ProfilerView;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			ProfilerModel model = new ProfilerModel();
			new Thread(new Server(model, 50000)).start();
			ProfilerView view = new ProfilerView(model);
			model.addObserver(new ProfilerObserver(view));
			model.notifyObservers();
			new ProfilerController(view, model);
			view.setVisible(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
