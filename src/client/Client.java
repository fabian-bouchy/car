package client;

import java.io.IOException;
import java.net.UnknownHostException;

import common.File;

import server.Replica;

/**
 * Main class for client side. 
 */
public class Client {
	
	public void run(String[] args){
		
		// the very first version - hostname and IP in the com
		String file = args[3];
		String hostName = args[1];
		int portNumber = Integer.parseInt(args[2]);
		
		
		try{
			
			Replica replica = new Replica("bob", hostName, "em0", 1, portNumber);
			File f = new File(args[3], args[3]);
			replica.write(f);
			
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + args[1]);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +hostName);
			System.exit(1);
		} 
	}
}
