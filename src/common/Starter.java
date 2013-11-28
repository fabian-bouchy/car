package common;

import client.main.Client;
import server.main.Server;

public class Starter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// server mode
        if ((args.length == 1 || args.length == 2 || args.length == 3) && args[0].equals("server")){
        	// server mode
        	Server server = new Server();
        	try {
				server.run(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
        // client mode
        }else if((args.length == 2 || args.length == 3) && (args[0].equals("write") || args[0].equals("get") || args[0].equals("delete")  || args[0].equals("read"))){
        	// client mode
        	Client client = new Client();
        	try {
				client.run(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }else{
            System.err.println("Usage:");
            System.err.println("    java -jar bobby.jar server [config.json] [hostname]");
            System.err.println("    java -jar bobby.jar write|read|delete file [config.json]");
            System.exit(1);
        }
	}

}
