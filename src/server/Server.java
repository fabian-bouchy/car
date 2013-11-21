package server;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

import server.thread.ThreadRead;
import server.thread.ThreadReplicaServer;
import server.thread.ThreadWrite;

public class Server {
	
	public void run(String[] args){
		
		// initialize the configuration
		if (args.length == 2){
			//ConfigManager.init(args[1]);
		}else{
			//ConfigManager.init();
		}
		
		// create connections to other replicas
		ArrayList<Replica> replicas = new ArrayList<Replica>();
		for (Replica replica : replicas){
			System.out.println(replica);
			// create a thread to deal with this connection
			ConnectionManager.createNewThread(replica);
		}
		
		// wait for new connections from clients and other replicas
		try {
			int port = 9999;
			ServerSocket serverSocket = new ServerSocket(port);
			
			while (true){
		        try {
		        	// accept any new connection
	                Socket clientSocket = serverSocket.accept();
	                System.out.println("[Server] Accepted a new connection from " + clientSocket.getInetAddress());
	                
	                // create io
	                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
	                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	                String inputLine = in.readLine();
	                
	                System.out.println("[Server] Received a message: '" + inputLine + "' " + clientSocket.getInetAddress());
	                
	                if (inputLine.equals("replica")){
	                	
	                	// instantiate a replica server thread
	                	System.out.println("[Server] Initializing a replica server thread for " + clientSocket.getInetAddress());
	            		ThreadReplicaServer thread = new ThreadReplicaServer(serverSocket, clientSocket, out, in);
	            		new Thread(thread).start();
	            		
	                }else if (inputLine.equals("read")){
	                	
	                	// instantiate a read thread
	                	System.out.println("[Server] Initializing a read thread for " + clientSocket.getInetAddress());
	            		ThreadRead thread = new ThreadRead(serverSocket, clientSocket, out, in);
	            		new Thread(thread).start();
	            		
	                }else{
	                	
	                	// instantiate a create/update/delete thread
	                	System.out.println("[Server] Initializing a write thread for " + clientSocket.getInetAddress());
	            		ThreadWrite thread = new ThreadWrite(serverSocket, clientSocket, out, in);
	            		new Thread(thread).start();
	                }
	                
	            } catch (IOException e) {
	                System.out.println("Exception caught when trying to listen on port "
	                    + port + " or listening for a connection");
	                System.out.println(e.getMessage());
	            }
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
