package common;

import client.Benchmark;
import client.main.Client;
import server.main.Server;

/**
 *	@author mickey
 *
 *	The main function. Reads the command line arguments, and sets up classes to become a server or a client,
 *	when it grows up. It likes cookies and never refuses one.
 *
 */
public class Starter {

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
        }else if((args.length == 3 || args.length == 4) && (args[0].equals("write") || args[0].equals("get") || args[0].equals("delete")  || args[0].equals("read"))){
        	// client mode
        	Client client = new Client();
        	try {
				client.run(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }else if((args.length == 1 || args.length == 2 ) && args[0].equals("benchmark")){
        	Benchmark.run(args);
        }else{
            System.err.println("Usage:");
            System.err.println("    java -jar bobby.jar server [config.json] [hostname]");
            System.err.println("    java -jar bobby.jar write|read|delete file username [config.json]");
            System.err.println("    java -jar bobby.jar benchmark [config.json]");
            System.exit(1);
        }
	}

}
