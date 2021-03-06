package common;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 *	Util class to synchronize threads and wait for all of them to finish
 */
public class Syncer {
	
	public enum ThreadResult {
		SUCCEEDED,		// when success
		FAILED,			// when server error
		UNAVAILABLE		// when connection error
	}

	private CountDownLatch latch;
	private ArrayList<Runnable> runnables;
	private ArrayList<Runnable> waitingthreads;
	private ArrayList<Runnable> failedThreads;
	private ArrayList<Runnable> succeededThreads;
	private ArrayList<Runnable> unavailableThreads;
	private ThreadResult[] results;
	private Object resultsLock = new Object();
	
	
	public Syncer(){
		waitingthreads 		= new ArrayList<Runnable>();
		failedThreads 		= new ArrayList<Runnable>();
		succeededThreads  	= new ArrayList<Runnable>();
		unavailableThreads 	= new ArrayList<Runnable>();
		runnables 			= new ArrayList<Runnable>();
	}
	
	public void addThread(Runnable thread){
		synchronized (waitingthreads){
			synchronized (runnables){
				waitingthreads.add(thread);
				runnables.add(thread);
			}
		}
	}
	
	/**
	 * Useful to retrieve and store thread results.
	 */
	public void callback(Runnable runnable, ThreadResult value){
		System.out.println("[Syncer] thread finished: " + runnable + " with result " + value);
		
		synchronized (waitingthreads){
			synchronized (succeededThreads){
				synchronized (failedThreads){
					synchronized (unavailableThreads){
						synchronized (runnables) {
							synchronized (resultsLock) {
								// store the value form callback
								int position = runnables.indexOf(runnable);
								
								// let's verify if the thread is expected
								if (position != -1){
									results[position] = value;
									System.out.println("[Syncer] stored a value " + results[position] + " for " + runnable);
									if(value == ThreadResult.FAILED) {
										failedThreads.add(runnable);
									} else if(value == ThreadResult.SUCCEEDED) {
										succeededThreads.add(runnable);
									} else {
										unavailableThreads.add(runnable);
									}
									waitingthreads.remove(runnable);
									latch.countDown();
								}else{
									System.out.println("[Syncer] fatal error callback from unknown thread " + runnable);
									if (failedThreads.indexOf(runnable) != -1){
										System.out.println("[Syncer] fatal error: " + runnable + " has already failed");
									}else if (succeededThreads.indexOf(runnable) != -1){
										System.out.println("[Syncer] fatal error: " + runnable + " has already succeeded");
									}else if (unavailableThreads.indexOf(runnable) != -1){
										System.out.println("[Syncer] fatal error: " + runnable + " has already been unavailable");
									}else {
										System.out.println("[Syncer] fatal error: " + runnable + " unknown");
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Start all added threads and stop the current thread until they have all finished.
	 */
	public void waitForAll() throws InterruptedException{
		latch = new CountDownLatch(waitingthreads.size());
		results = new ThreadResult[waitingthreads.size()];
		
		System.out.println("[Syncer] " + waitingthreads.size() + " threads to start");
		
		synchronized (failedThreads) {
			failedThreads.clear();
		}

		synchronized (succeededThreads) {
			succeededThreads.clear();
		}
		synchronized (waitingthreads) {
			// start all threads
			for(Runnable runnable : waitingthreads){
				System.out.println("[Syncer] running " + runnable);
				Thread thread = new Thread(runnable);
				thread.start();
			}
		}

		// wait for them to finish
		System.out.println("[Syncer] waiting for all " + this.hashCode());
		latch.await();
		synchronized (resultsLock) {
			System.out.print("[Syncer] finished " + this.hashCode() + ": [");
			for (int i = 0; i < results.length; i++){
				System.out.print(results[i] + " ");
			}
			System.out.println("]");
		}
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

	// Getters
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
