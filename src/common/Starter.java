package common;

import client.Client;
import server.Server;

public class Starter {

	/**
	 * @param args
	 * Usage:
	 * 		- bobby server [config.json] (starts a server with a configuration from the file)
	 * 		- bobby create|update|get|delete file
	 */
	public static void main(String[] args) {
		
        if (args.length != 2 && !(args.length == 1 && args[0].equals("server"))) {
            System.err.println("Usage:");
            System.err.println("    " +
            		"bobby server [config.json] (starts a server with a configuration from the file)");
            System.err.println("    " +
            		"bobby create|update|get|delete file");
            System.exit(1);
        }
        
        
        if (args[0].equals("server")){
        	// server mode
        	Server server = new Server();
        	server.run(args);
        }else{
        	// client mode
        	Client client = new Client();
        	client.run(args);
        }
	}

}
