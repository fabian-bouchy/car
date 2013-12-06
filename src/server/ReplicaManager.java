package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import server.thread.ThreadReplicaMetadataUpdate;
import server.thread.ThreadReplicaMetadataUpdate.ActionThreadMetadata;
import server.thread.ThreadReplicaWriteOrDelete;

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

	/**
	 * Start threads to write file on replicas
	 */
	public boolean replicate(File file){
		Syncer syncer = new Syncer();
		
		// initialize variables
		Collections.shuffle(replicas);
		int done = 0, K = ConfigManager.getK();
		int replicasNeeded = 0, replicasRemaining = 0;

		// Check global version file
		// if == 1 => write only on K+1 server
		// else broadcast update to all servers
		if(file.getGlobalVersion() == 1) {
			replicasNeeded = K;
		} else {
			replicasNeeded = ConfigManager.getN() - 1;
		}
		replicasRemaining = replicasNeeded;

		// Replicate
		while(done < replicasNeeded && replicasRemaining > 0) {
			for(int i = 0; i < replicasRemaining && i < replicas.size(); i++) {
				ThreadReplicaWriteOrDelete thread = new ThreadReplicaWriteOrDelete(replicas.get(i), file, syncer);
				syncer.addThread(thread);
			}
			try {
				syncer.waitForAll();
				// Remove succeed replication
				for (Runnable runnable : syncer.getSucceedThreads()) {
					ThreadReplicaWriteOrDelete thread = (ThreadReplicaWriteOrDelete)runnable;
					synchronized (thread) {
						replicas.remove(thread.getRemoteNode());
						thread.setNextStep(NextStep.COMMIT);
						thread.notify();
					}
				}
				// Remove failed replication
				for (Runnable runnable : syncer.getFailedThreads()) {
					ThreadReplicaWriteOrDelete thread = (ThreadReplicaWriteOrDelete)runnable;
					replicas.remove(thread.getRemoteNode());
				}
				done += syncer.getSucceedThreads().size();
				replicasRemaining = replicasNeeded - done - syncer.getFailedThreads().size();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (replicasRemaining > 0){
			System.out.println("[ReplicaManager] could not replicate to K servers");
		}
		return true;
	}

	/**
	 * Start threads to delete file on replicas
	 */
	public boolean delete(File file){
		Syncer syncer = new Syncer();

		for (RemoteNode remoteReplica : replicas) {
			syncer.addThread(new ThreadReplicaWriteOrDelete(remoteReplica, file, syncer));
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
			System.out.println("[ReplicaManager - delete] delete failed!");
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

	public HashMap<String, File> getMetadata() {
		for (RemoteNode remoteReplica : replicas) {
			try {
				// Return first metadata found!
				HashMap<String, File> metadataTmp = remoteReplica.getMetadata(); 
				if(metadataTmp != null) {
					return metadataTmp;
				}
			} catch (UnknownHostException e) {
				System.out.println("[server - metadata] Unable to connect to :" + remoteReplica + " " + e.getLocalizedMessage());
			} catch (IOException e) {
				System.out.println("[server - metadata] Unable to connect to :" + remoteReplica + " " + e.getLocalizedMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void propagateMetadataAdd(File metadata) {
		for(RemoteNode remoteReplica : replicas) {
			new Thread(new ThreadReplicaMetadataUpdate(metadata, remoteReplica, ActionThreadMetadata.ADD)).start();
		}
	}
}
