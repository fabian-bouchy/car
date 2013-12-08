package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

import server.thread.ThreadReplicaServerDelete;
import server.thread.ThreadReplicaServerWrite;

import common.ConfigManager;
import common.File;
import common.RemoteNode;
import common.Syncer;

public class ReplicaManager {
	
	private ArrayList<RemoteNode> replicas;

	public ReplicaManager(){
		replicas = new ArrayList<RemoteNode>(ConfigManager.getRemoteNodesList());
	}

	public enum NextStep {
		COMMIT,
		ABORT
	}
	
	public boolean replicate(File file, boolean initial){
		System.out.println("[replica manager] replicate started");
		Syncer syncer = new Syncer();
		
		// initialize variables
		Collections.shuffle(replicas);
		int done = 0, K = ConfigManager.getK();
		int replicasNeeded, replicasRemaining;

		/*
		 * At the initial propagation, we need to store K copies of the file,
		 * and N - K - 1 copies of meta-data (empty header with no content)
		 * 
		 * Later on, we will be broadcasting it to everybody,
		 * and the thread itself will define whether to send a full copy,
		 * or just meta-data to the particular replica
		 * 
		 */
		if(initial) {
			replicasNeeded = K;
		} else {
			replicasNeeded = ConfigManager.getN() - 1;
		}
		replicasRemaining = replicasNeeded;

		while(done < replicasNeeded && replicasRemaining > 0) {
			
			// start K threads (or as many as we can) to store the file
			for(int i = 0; i < replicasRemaining && i < replicas.size(); i++) {
				ThreadReplicaServerWrite thread = new ThreadReplicaServerWrite(replicas.get(i), file, syncer);
				syncer.addThread(thread);
			}
			try {
				syncer.waitForAll();
				
				/*
				 * If we have one refusal of a replica, we need to rollback the entire operation
				 */
				if (syncer.getFailedThreads().size() > 0){
					
					for (Runnable runnable : syncer.getSucceedThreads()) {
						ThreadReplicaServerWrite thread = (ThreadReplicaServerWrite)runnable;
						synchronized (thread) {
							replicas.remove(thread.getRemoteNode());
							thread.setNextStep(NextStep.ABORT);
							thread.notify();
						}
					}
					
					return false;
				}
				
				/*
				 * For the successful replicas, we remove them form the pool, and ask to commit the transaction
				 */
				for (Runnable runnable : syncer.getSucceedThreads()) {
					ThreadReplicaServerWrite thread = (ThreadReplicaServerWrite)runnable;
					synchronized (thread) {
						replicas.remove(thread.getRemoteNode());
						thread.setNextStep(NextStep.COMMIT);
						thread.notify();
					}
				}
				/*
				 * For the unavailable replicas, we also remove them from the pool, so that we don't retry
				 */
				for (Runnable runnable : syncer.getUnavailableThreads()) {
					ThreadReplicaServerWrite thread = (ThreadReplicaServerWrite)runnable;
					replicas.remove(thread.getRemoteNode());
				}
				
				done += syncer.getSucceedThreads().size();
				replicasRemaining = replicasNeeded - done - syncer.getFailedThreads().size() - syncer.getUnavailableThreads().size();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[replica manager] replicate files done");
		if (replicasRemaining > 0){
			System.out.println("[ReplicaManager] could not replicate to K servers");
		}
		
		if (initial){
			// replicate meta-data
			Syncer syncerMeta = new Syncer();
			for(int i = 0; i < replicas.size(); i++) {
				ThreadReplicaServerWrite thread = new ThreadReplicaServerWrite(replicas.get(i), file.generateMetadata(), syncerMeta);
				syncerMeta.addThread(thread);
			}
			try {
				syncerMeta.waitForAll();
				
				/*
				 * If we have one refusal of a replica, we need to rollback the entire operation
				 */
				if (syncerMeta.getFailedThreads().size() > 0){
					
					for (Runnable runnable : syncerMeta.getSucceedThreads()) {
						ThreadReplicaServerWrite thread = (ThreadReplicaServerWrite)runnable;
						synchronized (thread) {
							replicas.remove(thread.getRemoteNode());
							thread.setNextStep(NextStep.ABORT);
							thread.notify();
						}
					}
					
					return false;
				}
				
				/*
				 * For the successful replicas, we remove them form the pool, and ask to commit the transaction
				 */
				for (Runnable runnable : syncerMeta.getSucceedThreads()) {
					ThreadReplicaServerWrite thread = (ThreadReplicaServerWrite)runnable;
					synchronized (thread) {
						replicas.remove(thread.getRemoteNode());
						thread.setNextStep(NextStep.COMMIT);
						thread.notify();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[replica manager] replicate finished");
		return true;
	}

	/**
	 * Start threads to delete file on replicas
	 */
	public boolean delete(File file){
		Syncer syncer = new Syncer();

		for (RemoteNode remoteReplica : replicas) {
			syncer.addThread(new ThreadReplicaServerDelete(remoteReplica, file, syncer));
		}

		// wait for everybody
		try {
			syncer.waitForAll();
			if(syncer.allSucceeded()) {
				System.out.println("[ReplicaManager - delete] all succeeded!");
				return true;
			}else{
				System.out.println("[ReplicaManager - delete] some servers unavailable");
				return true;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public RemoteNode has(File file){
		for (RemoteNode remoteReplica : replicas) {
			try {
				if(remoteReplica.has(file)){
					return remoteReplica;
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
