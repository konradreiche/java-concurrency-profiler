
public class MyThread implements Runnable {

   Thread t;
     
   public MyThread() {
   }
   
   public void run() {
     /* NO-OP */
		try {
			"a".getBytes("ASCII");
			//excep();
			Thread.sleep(10000);
		} catch (java.lang.InterruptedException e){
			e.printStackTrace();
		} catch (Throwable t) {
                }
   }

   public void excep() throws Throwable{

		 throw new Exception("Thread Exception from MyThread");
	}
   }        


