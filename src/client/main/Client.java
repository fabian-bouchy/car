package client.main;

import server.UserManager;
import client.ServerManager;
import common.ConfigManager;
import common.ConfigManager.ConfigType;
import common.File;

/**
 * Main class for client side. 
 */
public class Client {
	
	public void run(String[] args) throws Exception{
		
		// arguments
		String cmd = args[0];
		String fileName = args[1];
		String username = args[2];
		
		// client write|get|delete file
		
		// initialize the configuration
		if (args.length == 4){
			// config name specified
			ConfigManager.init(ConfigType.CLIENT, args[3]);
		}else{
			// all default parameters
			ConfigManager.init(ConfigType.CLIENT);
		}
		
		// init the user manager
		UserManager.init(username);
		
		long startTime = System.nanoTime();
		
		if ("write".equals(cmd)){
			
			System.out.println("[client] Writing "+fileName);
			ServerManager.write(new File(fileName, true));
			
		}else if ("delete".equals(cmd)){
			
			System.out.println("[client] Deleting "+fileName);
			ServerManager.delete(new File(fileName, false));
			
		}else if ("read".equals(cmd)){
			
			System.out.println("[client] Reading "+fileName);
			File file = ServerManager.read(new File(fileName, false));
			System.out.println(file);
			
		}else{
			
			System.out.println("[client] Command unknown "+cmd);
		}
		long elapsedTime = System.nanoTime();
		System.out.println("[client] Done in "+ ((double)elapsedTime / 1000000000.0) + " seconds");
	}
}
