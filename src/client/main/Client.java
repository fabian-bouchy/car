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
		if((cmd.startsWith("write") || cmd.startsWith("compress") || cmd.startsWith("read")) && cmd.endsWith("+crypto")) {
			System.out.println("[Client] Enter password for this file:");
			Console co = System.console();
			password = new String(co.readPassword());
		}
		
		// init the user manager
		UserManager.init(userName, password);
		
		long startTime = System.nanoTime();
		
		if (cmd.startsWith("write")){
			
			System.out.println("[Client] Writing "+fileName);
			ServerManager.write(new File(fileName, true, false));
			
		}else if (cmd.startsWith("compress")){
			
			System.out.println("[Client] Compressing and writing "+fileName);
			ServerManager.write(new File(fileName, true, true));
			
		}else if ("delete".equals(cmd)){
			
			System.out.println("[Client] Deleting "+fileName);
			ServerManager.delete(new File(fileName, false, false));
			
		}else if (cmd.startsWith("read")){
			
			System.out.println("[Client] Reading "+fileName);
			File file = ServerManager.read(new File(fileName, false, false));
			if(file != null) {
				file.writeToFile("read_"+ file.getFileName());
			} else {
				System.out.println("[Client] File not found.");
			}

		}else if ("ls".equals(cmd)){

			System.out.println("[Client] Listing files owned by " + userName);
			ServerManager.listFile(userName);
			
		}else{
			
			System.out.println("[Client] Command unknown "+cmd);
		}

		long elapsedTime = System.nanoTime() - startTime;
		System.out.println("[Client] Done in "+ ((double)elapsedTime / 1000000000.0) + " seconds");
	}
}
