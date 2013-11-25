package client;

import server.Replica;
import common.ConfigManager;

/**
 * Main class for client side. 
 */
public class Client {
	
	public void run(String[] args) throws Exception{
		
		// client write|get|delete file
		
		// initialize the configuration
		ConfigManager.init();
		
		// get the first replica on the list - for now
		Replica replica = ConfigManager.getReplicas().get(0);
		System.out.println("[client] Replica chosen: " + replica);
		
		String cmd = args[0];
		String filename = args[1];
		
		if ("write".equals(cmd)){
			System.out.println("[client] Writing "+filename);
			
		}else if ("delete".equals(cmd)){
			System.out.println("[client] Deleting "+filename);
			
		}else{
			System.out.println("[client] Getting "+filename);
			
		}
	}
}
