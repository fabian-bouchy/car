package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;

import common.ConfigManager;
import common.File;
import common.RemoteNode;

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

		public ThreadReplicaWriteOrDelete(RemoteNode remoteReplica, File file) {
			super();
			this.remoteReplica = remoteReplica;
			this.file = file;
		}

		@Override
		public void run() {
			try {
				if(this.file.getData() != null)
					this.remoteReplica.write(this.file);
				else
					this.remoteReplica.delete(this.file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Start threads to write file on replicas
	 */
	public void replicate(File file){
		// Check global version file
		// if == 1 => write only on K+1 server
		// else broadcast update to all servers
		if(file.getGlobalVersion() == 1) {
			// Need to select K+1 server
			for (RemoteNode remoteReplica : replicas.values()) {
				Thread writeThread = new Thread(new ThreadReplicaWriteOrDelete(remoteReplica, file));
				writeThread.run();
			}
		} else {
			// Need to propagate broadcast update
			for (RemoteNode remoteReplica : replicas.values()) {
				Thread writeThread = new Thread(new ThreadReplicaWriteOrDelete(remoteReplica, file));
				writeThread.run();
			}
		}
	}

	/**
	 * Start threads to delete file on replicas
	 */
	public void delete(File file){
		for (RemoteNode remoteReplica : replicas.values()) {
			Thread deleteThread = new Thread(new ThreadReplicaWriteOrDelete(remoteReplica, file));
			deleteThread.run();
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
		HashMap<String, File> metadataOut = new HashMap<String, File>(); 
		for (RemoteNode remoteReplica : replicas.values()) {
			try {
				HashMap<String, File> metadataTmp = remoteReplica.getMetadata(); 
				if(metadataTmp != null) {
					metadataOut = mergeMetadata(metadataOut, metadataTmp);
				}
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			} catch (Exception e) {
			}
		}
		return metadataOut;
	}

	private HashMap<String, File> mergeMetadata(HashMap<String, File> base, HashMap<String, File> other) {
		for(Entry<String, File> metadata : other.entrySet()) {
			File current = base.get(metadata.getKey());
			if(current == null) {
				base.put(metadata.getKey(), metadata.getValue());
			} else {
				// TODO Check conflict on metadata version !!!
			}
		}
		return base;
	}
}
