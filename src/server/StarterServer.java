package server;

import java.net.*;
import java.io.*;

import common.ConfigManager;

import server.thread.ThreadRead;
import server.thread.ThreadReplicaServer;
import server.thread.ThreadWrite;

public class StarterServer {
	
	public void run(String[] args) throws Exception{
		
		// initialize the configuration
		if (args.length == 3){
			ConfigManager.init(args[1], args[2]);
		}else{
			ConfigManager.init();
		}
		
		try {
			Replica me = ConfigManager.getMe();
			
			// create a socket and wait for connections
			ServerSocket serverSocket = new ServerSocket(me.getPort());
			
			while (true){
		        try {
		        	// accept any new connection
	                Socket clientSocket = serverSocket.accept();
	                System.out.println("[Server] Accepted a new connection from " + clientSocket.getInetAddress());
	                
	                // create io
	                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
	                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	                String command = in.readLine();
	                
	                System.out.println("[Server] Received a message: '" + command + "' " + clientSocket.getInetAddress());
	                
	                // the replica commands family - interactions between servers
	                if (command.contains("replica")){
	                	
	                	// instantiate a replica server thread
	                	System.out.println("[Server] Initializing a replica server thread for " + clientSocket.getInetAddress());
	            		ThreadReplicaServer thread = new ThreadReplicaServer(serverSocket, clientSocket, out, in);
	            		new Thread(thread).start();
	            	
	            	// a client asks to read a file
	                }else if (command.equals("read")){
	                	
	                	// instantiate a read thread
	                	System.out.println("[Server] Initializing a read thread for " + clientSocket.getInetAddress());
	            		ThreadRead thread = new ThreadRead(serverSocket, clientSocket, out, in);
	            		new Thread(thread).start();
	            	
	            	// a client asks to write or delete
	                }else{
	                	
	                	// instantiate a create/update/delete thread
	                	System.out.println("[Server] Initializing a write thread for " + clientSocket.getInetAddress());
	            		ThreadWrite thread = new ThreadWrite(serverSocket, clientSocket, out, in);
	            		new Thread(thread).start();
	                }
	                
	            } catch (IOException e) {
	                System.out.println("Exception caught when trying to listen on port " + me.getPort() + " or listening for a connection");
	                System.out.println(e.getMessage());
	            }
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
