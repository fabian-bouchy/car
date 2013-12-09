package server.thread;

import common.File;
import common.RemoteNode;
import common.Syncer;
import common.UtilPrinter;
import common.Syncer.ThreadResult;

/**
 * Thread to answer to the delete session in a replica.
 * Send to the server a message to indicate if the process succeed or failed. 
 */
public class ThreadReplicaServerDelete implements Runnable {

	private File file;
	private RemoteNode remoteReplica;
	private Syncer syncer;
	
	public ThreadReplicaServerDelete(RemoteNode remoteReplica, File file, Syncer syncer) {
		super();
		this.remoteReplica = remoteReplica;
		this.file = file;
		this.syncer = syncer;
	}
	
	public void run() {
		System.out.println("[ThreadReplicaServerDelete] run");
		try {
			this.remoteReplica.delete(this.file);
			this.syncer.callback(this, ThreadResult.SUCCEEDED);
		} catch (Exception e) {
			UtilPrinter.printlnError("[ThreadReplicaServerDelete] failed: " + e.getLocalizedMessage());
			this.syncer.callback(this, ThreadResult.FAILED);
		}
	}
}
