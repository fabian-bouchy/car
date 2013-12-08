package client.main;

import java.io.Console;

import client.ServerManager;
import common.ConfigManager;
import common.ConfigManager.ConfigType;
import common.File;
import server.UserManager;

/**
 * Main class for client side. 
 */
public class Client {
	
	public void run(String cmd, String fileName, String userName, String configFile) throws Exception{
		// client write|get|delete file or listfiles
		
		// initialize the configuration
		if (configFile != null){
			// configuration name specified
			ConfigManager.init(ConfigType.CLIENT, configFile);
		}else{
			// all default parameters
			ConfigManager.init(ConfigType.CLIENT);
		}

		// Ask the password if it's needed
		String password = null;
		if("write".equals(cmd) || "read".equals(cmd)) {
			System.out.println("Enter password for this file:");
			Console co = System.console();
			password = new String(co.readPassword());
		}
		
		// init the user manager
		UserManager.init(userName, password);
		
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
			file.writeToFile("read_"+ file.getFileName());

		}else if ("ls".equals(cmd)){

			System.out.println("[client] Listing files owned by " + userName);
			ServerManager.listFile(userName);
			
		}else{
			
			System.out.println("[client] Command unknown "+cmd);
		}

		long elapsedTime = System.nanoTime() - startTime;
		System.out.println("[client] Done in "+ ((double)elapsedTime / 1000000000.0) + " seconds");
	}
}
