package client.main;

import client.ServerManager;
import common.ConfigManager;
import common.ConfigManager.ConfigType;
import common.File;

/**
 * Main class for client side. 
 */
public class Client {
	
	public void run(String[] args) throws Exception{
		
		// client write|get|delete file
		
		// initialize the configuration
		if (args.length == 3){
			// config name
			ConfigManager.init(ConfigType.CLIENT, args[2]);
		}else{
			// all default parameters
			ConfigManager.init(ConfigType.CLIENT);
		}
		// arguments
		String cmd = args[0];
		String fileName = args[1];
		
		if ("write".equals(cmd)){
			System.out.println("[client] Writing "+fileName);
			// TODO Generate file ID
			File file = new File(fileName, fileName, true);
			ServerManager.write(file);
		}else if ("delete".equals(cmd)){
			System.out.println("[client] Deleting "+fileName);
			File file = new File(fileName, fileName, false);
			// TODO test delete
			ServerManager.delete(file);
		}else if ("read".equals(cmd)){
			System.out.println("[client] Reading "+fileName);
			
			// TODO test read
			ServerManager.read(fileName);
		}else{
			System.out.println("[client] Command unknown "+cmd);
		}
	}
}
