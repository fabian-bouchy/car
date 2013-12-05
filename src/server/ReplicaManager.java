package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import common.ConfigManager;
import common.File;
import common.RemoteNode;
import common.Syncer;
import common.Syncer.ThreadResult;

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
	 * Thread use to write or delete in //
	 *
	 * Delete file if size = 0.
	 */
	private class ThreadReplicaWriteOrDelete implements Runnable {
		private File file;
		private RemoteNode remoteReplica;
		private Syncer syncer;

		private NextStep nextStep = NextStep.ABORT;

		public ThreadReplicaWriteOrDelete(RemoteNode remoteReplica, File file, Syncer syncer) {
			super();
			this.remoteReplica = remoteReplica;
			this.file = file;
			this.syncer = syncer;
		}

		public void setNextStep(NextStep next) {
			this.nextStep = next;
		}
		@Override
		public void run() {
			try {
				if(this.file.getData() != null){
					this.remoteReplica.write(this.file);
					// callback
					this.syncer.callback(this, ThreadResult.SUCCEED);
					System.out.println("[ReplicaManager - threadwritedel] waiting...");
					// Waiting all others threads.
					synchronized (this) {
						this.wait();
					}
					System.out.println("[ReplicaManager - threadwritedel] Restarting!!!!");
					if(nextStep == NextStep.ABORT) {
						System.out.println("[ReplicaManager] abort" + this.file);
						this.remoteReplica.abortWrite(this.file);
					} else if(nextStep == NextStep.COMMIT) {
						System.out.println("[ReplicaManager] commit " + this.file);
						this.remoteReplica.commitWrite(this.file);
					}
				}else{
					this.remoteReplica.delete(this.file);
					this.syncer.callback(this, ThreadResult.SUCCEED);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				// callback
				this.syncer.callback(this, ThreadResult.FAILED);
			}
		}
	}

	/**
	 * Start threads to write file on replicas
	 */
	public boolean replicate(File file){
		
		Syncer syncer = new Syncer();
		ArrayList<ThreadReplicaWriteOrDelete> threads = new ArrayList<ReplicaManager.ThreadReplicaWriteOrDelete>();
		
		// Check global version file
		// if == 1 => write only on K+1 server
		// else broadcast update to all servers
		if(file.getGlobalVersion() == 1) {

			// Need to select K+1 server
			for (RemoteNode remoteReplica : replicasK.values()) {
				ThreadReplicaWriteOrDelete thread = new ThreadReplicaWriteOrDelete(remoteReplica, file, syncer);
				threads.add(thread);
				syncer.addThread(thread);
			}
		} else {
			// Need to propagate broadcast update
			for (RemoteNode remoteReplica : replicas.values()) {
				ThreadReplicaWriteOrDelete thread = new ThreadReplicaWriteOrDelete(remoteReplica, file, syncer);
				threads.add(thread);
				syncer.addThread(thread);
			}
		}
		
		try {
			syncer.waitForAll();
			System.out.println("[ReplicaManager replicate] after waitForAll");
			if(syncer.isAllSucceed()) {
				System.out.println("[ReplicaManager replicate] syncer all succeed");
				// BROADCAST commit
				for(ThreadReplicaWriteOrDelete thread: threads) {
					synchronized (thread) {
						thread.setNextStep(NextStep.COMMIT);
						thread.notify();
					}
				}
				return true;
			} else {
				System.out.println("[ReplicaManager replicate] syncer one or many failed");
				// BROADCAST abort
				for(ThreadReplicaWriteOrDelete thread: threads) {
					synchronized (thread) {
						thread.setNextStep(NextStep.ABORT);
						thread.notify();
					}
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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

	public enum ActionThreadMetadata {
		ADD,
		DELETE
	}

	private class ThreadMetadata implements Runnable{
		private File metadata;
		private RemoteNode remoteReplica;
		private ActionThreadMetadata actionThreadMetadata;

		public ThreadMetadata(File metadata, RemoteNode remoteReplica, ActionThreadMetadata action) {
			this.metadata = metadata;
			this.remoteReplica = remoteReplica;
			this.actionThreadMetadata = action;
		}

		@Override
		public void run() {
			try {
				if(actionThreadMetadata == ActionThreadMetadata.ADD) {
					remoteReplica.addMetadata(metadata);
				} else if(actionThreadMetadata == ActionThreadMetadata.DELETE) {
					remoteReplica.deleteMetadata(metadata);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void propagateMetadataAdd(File metadata) {
		for(RemoteNode remoteReplica : replicas.values()) {
			new Thread(new ThreadMetadata(metadata, remoteReplica, ActionThreadMetadata.ADD)).start();
		}
	}

	public void propagateMetadataDelete(File metadata) {
		for(RemoteNode remoteReplica : replicas.values()) {
			new Thread(new ThreadMetadata(metadata, remoteReplica, ActionThreadMetadata.DELETE)).start();
		}
	}
}
