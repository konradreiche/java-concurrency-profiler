package de.fu.profiler;

import java.io.IOException;

import de.fu.profiler.controller.ProfilerController;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.service.Server;
import de.fu.profiler.view.ProfilerObserver;
import de.fu.profiler.view.ProfilerView;

public class Main {

	private static int PORT = 49125;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length == 1) {
			try {
				PORT = Integer.parseInt(args[0]);				
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
			}
		}
		
		try {
			ProfilerModel model = new ProfilerModel();
			new Thread(new Server(model, PORT)).start(); 
			ProfilerView view = new ProfilerView(model);
			model.addObserver(new ProfilerObserver(view));
			model.notifyObservers();
			new ProfilerController(view, model);
			view.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
