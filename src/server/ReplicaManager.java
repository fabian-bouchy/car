package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
		// Shuffle list
		HashMap<String, RemoteNode> replicasHashMap = ConfigManager.getRemoteNodes();
		List<RemoteNode> replicasShuffled = new ArrayList<RemoteNode>(replicasHashMap.values());
		Collections.shuffle(replicasShuffled);

		// Init var
		int done = 0, K = ConfigManager.getK();
		int nbReplicationNeeded = 0, nbReplicationRemaining = 0;

		// Check global version file
		// if == 1 => write only on K+1 server
		// else broadcast update to all servers
		if(file.getGlobalVersion() == 1) {
			nbReplicationNeeded = K;
		} else {
			nbReplicationNeeded = ConfigManager.getN() - 1;
		}
		nbReplicationRemaining = nbReplicationNeeded;

		// Replicate
		while(done < nbReplicationNeeded && nbReplicationRemaining > 0) {
			for(int i = 0; i < nbReplicationRemaining && i < replicasShuffled.size(); i++) {
				ThreadReplicaWriteOrDelete thread = new ThreadReplicaWriteOrDelete(replicasShuffled.get(i), file, syncer);
				syncer.addThread(thread);
			}
			try {
				syncer.waitForAll();
				// Remove succeed replication
				for (Runnable runnable : syncer.getSucceedThreads()) {
					ThreadReplicaWriteOrDelete thread = (ThreadReplicaWriteOrDelete)runnable;
					synchronized (thread) {
						replicasShuffled.remove(thread.getRemoteNode());
						thread.setNextStep(NextStep.COMMIT);
						thread.notify();
					}
				}
				// Remove failed replication
				for (Runnable runnable : syncer.getFailedThreads()) {
					ThreadReplicaWriteOrDelete thread = (ThreadReplicaWriteOrDelete)runnable;
					synchronized (thread) {
						replicasShuffled.remove(thread.getRemoteNode());
						thread.setNextStep(NextStep.COMMIT);
						thread.notify();
					}
				}
				done += syncer.getSucceedThreads().size();
				nbReplicationRemaining = nbReplicationNeeded - done - syncer.getFailedThreads().size();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
				System.out.println("[ReplicaManager - delete] all succeed!");
				return true;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[ReplicaManager - delete] delete failed!");
		return false;
	}

	public RemoteNode has(File file){
		for (RemoteNode remoteReplica : replicas) {
			try {
				if(remoteReplica.has(file))
					return remoteReplica;
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

	public void propagateMetadataDelete(File metadata) {
		for(RemoteNode remoteReplica : replicas) {
			new Thread(new ThreadReplicaMetadataUpdate(metadata, remoteReplica, ActionThreadMetadata.DELETE)).start();
		}
	}
}
