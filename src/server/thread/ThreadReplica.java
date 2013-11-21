package server.thread;

import server.Replica;

public class ThreadReplica implements Runnable{
	
	private Replica replica;
	
	public ThreadReplica(Replica replica){
		this.replica = replica;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
