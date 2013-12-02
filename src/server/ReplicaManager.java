package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import common.ConfigManager;
import common.File;
import common.RemoteNode;
import common.Syncer;

public class ReplicaManager {
	
	private HashMap<String, RemoteNode> replicas;

	public ReplicaManager(){
		replicas = new HashMap<String, RemoteNode>(ConfigManager.getRemoteNodes());
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

		public ThreadReplicaWriteOrDelete(RemoteNode remoteReplica, File file, Syncer syncer) {
			super();
			this.remoteReplica = remoteReplica;
			this.file = file;
			this.syncer = syncer;
		}

		@Override
		public void run() {
			try {
				if(this.file.getData() != null){
					this.remoteReplica.write(this.file);
				}else{
					this.remoteReplica.delete(this.file);
				}
				
				// callback
				this.syncer.callback(this, 1);
			} catch (Exception e) {
				e.printStackTrace();
				
				// callback
				this.syncer.callback(this, 0);
			}
		}
	}

	/**
	 * Start threads to write file on replicas
	 */
	public void replicate(File file){
		
		Syncer syncer = new Syncer();
		
		// Check global version file
		// if == 1 => write only on K+1 server
		// else broadcast update to all servers
		if(file.getGlobalVersion() == 1) {
			// TODO Need to select K+1 server
			for (RemoteNode remoteReplica : replicas.values()) {
				syncer.addThread(new ThreadReplicaWriteOrDelete(remoteReplica, file, syncer));
			}
		} else {
			// Need to propagate broadcast update
			for (RemoteNode remoteReplica : replicas.values()) {
				syncer.addThread(new ThreadReplicaWriteOrDelete(remoteReplica, file, syncer));
			}
		}
		
		try {
			syncer.waitForAll();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Start threads to delete file on replicas
	 */
	public void delete(File file){
		
		Syncer syncer = new Syncer();
		
		for (RemoteNode remoteReplica : replicas.values()) {
			syncer.addThread(new ThreadReplicaWriteOrDelete(remoteReplica, file, syncer));
		}
		
		// wait for everybody
		try {
			syncer.waitForAll();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
}
