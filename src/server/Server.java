package server;

import java.util.ArrayList;

public class Server {
	
	public void run(String[] args){
		if (args.length == 2){
			//ConfigManager.init(args[1]);
		}else{
			//ConfigManager.init();
		}
		
		
		// create connections to other replicas
		ArrayList<Replica> replicas = new ArrayList<Replica>();
		for (Replica replica : replicas){
			System.out.println(replica);
			// create a thread to dela with this connection
		}
	}
}
