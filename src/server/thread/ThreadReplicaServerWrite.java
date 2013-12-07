package server.thread;

import server.ReplicaManager.NextStep;
import common.File;
import common.RemoteNode;
import common.Syncer;
import common.Syncer.ThreadResult;

public class ThreadReplicaServerWrite implements Runnable {
	private File file;
	private RemoteNode remoteReplica;
	private Syncer syncer;
	private NextStep nextStep = NextStep.ABORT;

	public ThreadReplicaServerWrite(RemoteNode remoteReplica, File file, Syncer syncer) {
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
		System.out.println("[ThreadReplicaServerWrite] run");
		try {
			this.remoteReplica.write(this.file);
			System.out.println("[ThreadReplicaServerWrite] write ok, calling callback...");
			// callback
			this.syncer.callback(this, ThreadResult.SUCCEEDED);
			
			/*
			 * For files, we have a two-step process,
			 * for meta-data, though, we stop here
			 */
			if (file.isFile()){
				System.out.println("[ThreadReplicaServerWrite] waiting...");
				synchronized (this) {
					this.wait();
				}
				System.out.println("[ThreadReplicaServerWrite] finished waiting");
			
				// do the action which was decided
				if(nextStep == NextStep.ABORT) {
					System.out.println("[ThreadReplicaServerWrite] abort" + this.file);
					this.remoteReplica.abortWrite(this.file);
				} else if(nextStep == NextStep.COMMIT) {
					System.out.println("[ThreadReplicaServerWrite] commit " + this.file);
					this.remoteReplica.commitWrite(this.file);
				}
			}
			System.out.println("[ThreadReplicaServerWrite] finished");
			
		} catch (Exception e) {
			System.out.println("[ThreadReplicaServerWrite] failed: " + e.getLocalizedMessage());
			this.syncer.callback(this, ThreadResult.FAILED);
		}
	}

	public RemoteNode getRemoteNode() {
		return remoteReplica;
	}
}