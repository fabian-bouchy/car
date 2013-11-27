package common;

import client.Client;
import server.Server;

public class Starter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

        if (args[0].equals("server") && (args.length == 3 || args.length == 1)){
        	// server mode
        	Server server = new Server();
        	try {
				server.run(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }else if(args.length == 2 && (args[0].equals("write") || args[0].equals("get") || args[0].equals("delete"))){
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
            System.err.println("    java -jar bobby.jar server [config.json] [interface]");
            System.err.println("    java -jar bobby.jar write|get|delete file");
            System.exit(1);
        }
	}

}
