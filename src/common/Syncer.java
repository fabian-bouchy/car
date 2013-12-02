package common;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Syncer {
	
	private CountDownLatch latch;
	private ArrayList<Runnable> threads;
	private int N = 0;
	private int[] results;
	
	public Syncer(){
		threads = new ArrayList<Runnable>();
	}
	
	public synchronized void addThread(Runnable thread){
		N++;
		threads.add(thread);
	}
	
	public synchronized void callback(Runnable runnable, int value){
		System.out.println("[syncer] thread finished: " + runnable);
		
		// store the value form callback
		int position = threads.indexOf(runnable);
		if (position != -1){
			results[position] = value;
		}
		latch.countDown();
	}
	
	public void waitForAll() throws InterruptedException{
		latch = new CountDownLatch(N);
		results = new int[N];
		
		// start all threads
		for(Runnable runnable : threads){
			System.out.println("[syncer] running " + runnable);
			Thread thread = new Thread(runnable);
			thread.run();
		}
		
		// wait for them to finish
		System.out.println("[syncer] waiting for everybody");
		latch.await();
		System.out.print("[syncer] finished: [");
		for (int i : results){
			System.out.print(i + ", ");
		}
		System.out.println("]");
	}

}
