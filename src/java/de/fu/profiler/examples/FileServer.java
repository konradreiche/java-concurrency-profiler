package de.fu.profiler.examples;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class FileServer {

	int hits = 0;
	String lock = "LOCK";	
	Storage storage = new Storage();

	public static void main(String... args) {
		
		final FileServer fileServer = new FileServer();
		
		int numberOfThreadsPerRunnable = 30;
		
		Runnable runDefaultRequest = new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					fileServer.defaultRequest();
				}
			}
		};
		
		Runnable runFileRequest = new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					fileServer.fileRequest(0);
				}
			}
		};
		
		for (int i = 0; i < numberOfThreadsPerRunnable; ++i) {
			new Thread(runDefaultRequest).start();
			new Thread(runFileRequest).start();
		}		
	}
	
	public void defaultRequest() {
		synchronized (lock) {
			++hits;
			// some default processing
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public File fileRequest(int id) {
		return storage.getFile(id);
	}
}

class Storage {
	
	Map<Integer, File> files = new TreeMap<Integer, File>();
	String lock = "LOCK";
	
	public File getFile(int id) {
		synchronized (lock) {
			return files.get(id);
		}
	}
}