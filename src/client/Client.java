package client;

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
		ConfigManager.init(ConfigType.CLIENT);
		
		// get the first replica on the list - for now
		String cmd = args[0];
		String fileName = args[1];
		
		if ("write".equals(cmd)){
			System.out.println("[client] Writing "+fileName);
			// TODO Generate file ID
			File file = new File( fileName, fileName);
			ServerManager.write(file);
		}else if ("delete".equals(cmd)){
			System.out.println("[client] Deleting "+fileName);
			ServerManager.delete(fileName);
		}else if ("read".equals(cmd)){
			System.out.println("[client] Deleting "+fileName);
			ServerManager.read(fileName);
		}else{
			System.out.println("[client] Getting "+fileName);
			
		}
	}
}
