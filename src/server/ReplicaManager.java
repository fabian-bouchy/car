package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import server.thread.ThreadReplicaMetadataUpdate;
import server.thread.ThreadReplicaMetadataUpdate.ActionThreadMetadata;
import server.thread.ThreadReplicaWriteOrDelete;
import common.ConfigManager;
import common.File;
import common.RemoteNode;
import common.Syncer;

public class ReplicaManager {
	
	private HashMap<String, RemoteNode> replicas, replicasK;

	public ReplicaManager(){
		replicas = new HashMap<String, RemoteNode>(ConfigManager.getRemoteNodes());
        replicasK = new HashMap<String, RemoteNode>(ConfigManager.getRemoteReplicas());
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
		//ArrayList<ThreadReplicaWriteOrDelete> threads = new ArrayList<ThreadReplicaWriteOrDelete>();

		// Check global version file
		// if == 1 => write only on K+1 server
		// else broadcast update to all servers
		if(file.getGlobalVersion() == 1) {

			// Need to select K+1 server
			for (RemoteNode remoteReplica : replicasK.values()) {
				ThreadReplicaWriteOrDelete thread = new ThreadReplicaWriteOrDelete(remoteReplica, file, syncer);
				//threads.add(thread);
				syncer.addThread(thread);
			}
		} else {
			// Need to propagate broadcast update
			for (RemoteNode remoteReplica : replicas.values()) {
				ThreadReplicaWriteOrDelete thread = new ThreadReplicaWriteOrDelete(remoteReplica, file, syncer);
				//threads.add(thread);
				syncer.addThread(thread);
			}
		}

		try {
			syncer.waitForAll();
			System.out.println("[ReplicaManager replicate] after waitForAll");
			
			int i = 0, nbRetry = ConfigManager.getN() - ConfigManager.getK();
			while(!syncer.isAllSucceed() && i < nbRetry) {
				System.out.println("[ReplicaManager replicate] syncer one or many failed, retrying...");
				for (Runnable runnable : syncer.getFailedThreads()) {
					ThreadReplicaWriteOrDelete thread = (ThreadReplicaWriteOrDelete)runnable;
					synchronized (thread) {
						thread.setNextStep(NextStep.ABORT);
						RemoteNode newRemoteNode = ConfigManager.getOtherRemoteReplica(replicasK);
						syncer.addThread(new ThreadReplicaWriteOrDelete(newRemoteNode, thread.getFile(), syncer));
						thread.notify();
					}
				}
				syncer.waitForAll();
				System.out.println("[ReplicaManager replicate] syncer one or many failed");
				i++;
			}
			// Need to abort all succeed thread.
			if(!syncer.isAllSucceed()) {
				System.out.println("[ReplicaManager replicate] syncer abort all succeed threads");
				// BROADCAST abort
				for(Runnable runnable: syncer.getSucceedThreads()) {
					ThreadReplicaWriteOrDelete thread = (ThreadReplicaWriteOrDelete)runnable;
					synchronized (thread) {
						thread.setNextStep(NextStep.ABORT);
						thread.notify();
					}
				}
			}
			else {
				System.out.println("[ReplicaManager replicate] syncer all succeed");
				for(Runnable runnable: syncer.getSucceedThreads()) {
					ThreadReplicaWriteOrDelete thread = (ThreadReplicaWriteOrDelete)runnable;
					synchronized (thread) {
						thread.setNextStep(NextStep.COMMIT);
						thread.notify();
					}
				}
				return true;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Start threads to delete file on replicas
	 */
	public boolean delete(File file){
		Syncer syncer = new Syncer();

		for (RemoteNode remoteReplica : replicas.values()) {
			syncer.addThread(new ThreadReplicaWriteOrDelete(remoteReplica, file, syncer));
		}

		// wait for everybody
		try {
			syncer.waitForAll();
			if(syncer.isAllSucceed()) {
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
		// TODO need to be change
		for (RemoteNode remoteReplica : replicas.values()) {
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
		for (RemoteNode remoteReplica : replicas.values()) {
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
		for(RemoteNode remoteReplica : replicas.values()) {
			new Thread(new ThreadReplicaMetadataUpdate(metadata, remoteReplica, ActionThreadMetadata.ADD)).start();
		}
	}

	public void propagateMetadataDelete(File metadata) {
		for(RemoteNode remoteReplica : replicas.values()) {
			new Thread(new ThreadReplicaMetadataUpdate(metadata, remoteReplica, ActionThreadMetadata.DELETE)).start();
		}
	}
}
