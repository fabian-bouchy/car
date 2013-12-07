package server.thread;

import server.ReplicaManager.NextStep;
import common.File;
import common.RemoteNode;
import common.Syncer;
import common.Syncer.ThreadResult;

	/**
	 * Thread use to write or delete in //
	 *
	 * Delete file if size = 0.
	 */
public class ThreadReplicaWriteOrDelete implements Runnable {
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
				System.out.println("[ReplicaManager - Thread replica WorD] write ok, calling callback...");
				// callback
				this.syncer.callback(this, ThreadResult.SUCCEEDED);
				System.out.println("[ReplicaManager - Thread replica WorD] waiting...");
				// Waiting all others threads.
				synchronized (this) {
					this.wait();
				}
				System.out.println("[ReplicaManager - Thread replica WorD] finished waiting");
				if(nextStep == NextStep.ABORT) {
					System.out.println("[ReplicaManager] abort" + this.file);
					this.remoteReplica.abortWrite(this.file);
				} else if(nextStep == NextStep.COMMIT) {
					System.out.println("[ReplicaManager] commit " + this.file);
					this.remoteReplica.commitWrite(this.file);
				}
			}else{
				this.remoteReplica.delete(this.file);
				this.syncer.callback(this, ThreadResult.SUCCEEDED);
			}
			
		} catch (Exception e) {
			System.out.println("[server - Thread replica WorD] failed: " + e.getLocalizedMessage());
			// callback
			this.syncer.callback(this, ThreadResult.FAILED);
		}
	}

	public RemoteNode getRemoteNode() {
		return remoteReplica;
	}

	public File getFile() {
		return file;
	}
}