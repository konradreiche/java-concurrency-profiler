public class SimpleThread {
      native static void check(Thread thr, ClassLoader cl);
      static MyThread t;

      public static void main(String args[]) throws Throwable{
//          System.loadLibrary("a");

	  t = new MyThread();
          System.out.println("Creating and running 2 threads...");
          for(int i = 0; i < 2; i++) {
	  	Thread thr = new Thread(t,"MyThread"+i);
          	thr.start();

 //         	check(thr, thr.getContextClassLoader());
		try {
                 thr.join();
                } catch (Throwable t) {
                }
	  }

      }
}


