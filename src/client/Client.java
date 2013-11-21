package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Main class for client side. 
 */
public class Client {
	
	public void run(String[] args){
		// initialise a config manager with default values
		// ConfigManager.init();
		
		// parse values
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		try{
			Socket echoSocket = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			out.println("bonjour!");
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + args[1]);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +hostName);
			System.exit(1);
		} 
	}
}
