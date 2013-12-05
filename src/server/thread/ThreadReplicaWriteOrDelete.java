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