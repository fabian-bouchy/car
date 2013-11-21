package server;

import java.util.ArrayList;
import server.thread.ThreadReplica;

public class ConnectionManager {

	private static ArrayList<ThreadReplica> threads;
	static {
		threads = new ArrayList<ThreadReplica>();
	}
	
	public static void createNewThread(Replica replica){
		ThreadReplica thread = new ThreadReplica(replica);
		threads.add(thread);
		new Thread(thread).start();
	}
}
