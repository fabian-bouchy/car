package common;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Syncer {
	
	public enum ThreadResult {
		SUCCEED,
		FAILED
	}

	private CountDownLatch latch;
	private ArrayList<Runnable> threads;
	private int N = 0;
	private ThreadResult[] results;
	
	public Syncer(){
		threads = new ArrayList<Runnable>();
	}
	
	public synchronized void addThread(Runnable thread){
		N++;
		threads.add(thread);
	}
	
	public synchronized void callback(Runnable runnable, ThreadResult value){
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
		results = new ThreadResult[N];

		// start all threads
		for(Runnable runnable : threads){
			System.out.println("[syncer] running " + runnable);
			Thread thread = new Thread(runnable);
			thread.start();
		}

		// wait for them to finish
		System.out.println("[syncer] waiting for everybody");
		latch.await();
		System.out.print("[syncer] finished: [");
		for (ThreadResult i : results){
			System.out.print(i + " ");
		}
		System.out.println("]");
	}

	public boolean isAllSucceed() {
		for(ThreadResult result : results) {
			if(result != ThreadResult.SUCCEED) {
				return false;
			}
		}
		return true;
	}
}
