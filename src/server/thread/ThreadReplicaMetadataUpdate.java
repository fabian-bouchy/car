package server.thread;

import java.net.ConnectException;

import common.File;
import common.RemoteNode;

public class ThreadReplicaMetadataUpdate implements Runnable {
	public enum ActionThreadMetadata {
		ADD,
		DELETE
	}

	private File metadata;
	private RemoteNode remoteReplica;
	private ActionThreadMetadata actionThreadMetadata;

	public ThreadReplicaMetadataUpdate(File metadata, RemoteNode remoteReplica, ActionThreadMetadata action) {
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
		} catch(ConnectException e) {
			System.out.println("[Server - thread replicate update metadata] Unable to connect to remote: " + e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}