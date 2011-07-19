package de.fu.profiler.examples;

class College {
	  //{{{  
	  
	  //{{{  COMMENT documentation
	  //
	  //The College consists of 5 Philosophers, a Chef and the Canteen.  The Chef and
	  //the Philosophers are "active" objects.  The Canteen is a "passive" object
	  //through which the Philosophers and the Chef have to interact.
	  //
	  //The Canteen is implemented in the style of the CubbyHole example from Sun's
	  //Java Tutorial.  As such, it exposes each Philosopher to the danger of
	  //starvation (by infinite overtaking by fellow Philosophers).  Sadly, this
	  //does happen with the particular timing circumstances set up in this
	  //demonstration.  Happily, it's the greedy Philosopher that starves -- he
	  //never even gets one meal, despite being in the Canteen the whole time!
	  //
	  //}}}
	  
	  //{{{  main
	  public static void main (String argv[]) throws InterruptedException {
	    //{{{  
	    
		  Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		t.start();
		  
		  Thread.sleep(1000);
		  System.out.println(t.getState());
		  
		  
	    
	    //}}}
	  }
}