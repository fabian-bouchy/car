package common;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 *	@author mickey
 *	
 *	Util class to synchronize threads and wait for all of them to finish
 *
 */
public class Syncer {
	
	public enum ThreadResult {
		SUCCEEDED,		// when success
		FAILED,			// when server error
		UNAVAILABLE		// when connection error
	}

	private CountDownLatch latch;
	private ArrayList<Runnable> waitingthreads;
	private ArrayList<Runnable> failedThreads;
	private ArrayList<Runnable> succeededThreads;
	private ArrayList<Runnable> unavailableThreads;
	private ThreadResult[] results;
	
	
	public Syncer(){
		waitingthreads 		= new ArrayList<Runnable>();
		failedThreads 		= new ArrayList<Runnable>();
		succeededThreads  	= new ArrayList<Runnable>();
		unavailableThreads 	= new ArrayList<Runnable>();
	}
	
	public void addThread(Runnable thread){
		synchronized (waitingthreads){
			waitingthreads.add(thread);
		}
	}
	
	public void callback(Runnable runnable, ThreadResult value){
		System.out.println("[syncer] thread finished: " + runnable);
		
		synchronized (waitingthreads){
			synchronized (succeededThreads){
				synchronized (failedThreads){
					synchronized (unavailableThreads){
						synchronized (results) {
									
							// store the value form callback
							int position = waitingthreads.indexOf(runnable);
							if (position != -1){
								results[position] = value;
								if(value == ThreadResult.FAILED) {
									failedThreads.add(runnable);
								} else if(value == ThreadResult.SUCCEEDED) {
									succeededThreads.add(runnable);
								} else {
									unavailableThreads.add(runnable);
								}
								waitingthreads.remove(runnable);
							}
							latch.countDown();
						}
					}
				}
			}
		}
	}
	
	public void waitForAll() throws InterruptedException{
		latch = new CountDownLatch(waitingthreads.size());
		results = new ThreadResult[waitingthreads.size()];
		
		System.out.println("[syncer] " + waitingthreads.size() + " threads to start");
		
		synchronized (failedThreads) {
			failedThreads.clear();
		}

		synchronized (succeededThreads) {
			succeededThreads.clear();
		}
		synchronized (waitingthreads) {
			// start all threads
			for(Runnable runnable : waitingthreads){
				System.out.println("[syncer] running " + runnable);
				Thread thread = new Thread(runnable);
				thread.start();
			}
		}

		// wait for them to finish
		System.out.println("[syncer] waiting for all " + this.hashCode());
		latch.await();
		System.out.print("[syncer] finished " + this.hashCode() + ": [");
		for (ThreadResult i : results){
			System.out.print(i + " ");
		}
		System.out.println("]");
	}

	public boolean allSucceeded() {
		synchronized (results) {			
			return succeededThreads.size() == results.length;
		}
	}
	
	public boolean noneFailed() {
		synchronized (failedThreads) {			
			return failedThreads.size() == 0;
		}
	}
	
	
	public ArrayList<Runnable> getFailedThreads() {
		return failedThreads;
	}
	public ArrayList<Runnable> getSucceedThreads() {
		return succeededThreads;
	}
	public ArrayList<Runnable> getUnavailableThreads() {
		return unavailableThreads;
	}
}
