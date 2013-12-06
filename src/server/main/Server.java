package server.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;

import common.ConfigManager;
import common.File;
import common.FileManager;
import common.UtilBobby;
import common.ConfigManager.ConfigType;
import server.RemoteReplica;
import server.ReplicaManager;
import server.thread.ThreadDelete;
import server.thread.ThreadRead;
import server.thread.ThreadReplicaServer;
import server.thread.ThreadWrite;

public class Server {
	
	public void restoreMetaData() {
		System.out.println("[Server] Updating metadata...");
		ReplicaManager replicaManager = new ReplicaManager();
		HashMap<String, File> metadata = replicaManager.getMetadata();
		if(metadata != null)
			FileManager.setMetadata(metadata);
		System.out.println("[Server] Metadata updated...");
		System.out.println(FileManager.represent());
	}

	public void run(String[] args) throws Exception{
		
		// initialize the configuration
		if (args.length == 3){
			// config name and hostname
			ConfigManager.init(ConfigType.SERVER, args[1], args[2]);
		}else if (args.length == 2){
			// only config name
			ConfigManager.init(ConfigType.SERVER, args[1]);
		}else{
			// all default parameters
			ConfigManager.init(ConfigType.SERVER);
		}

		try {
			restoreMetaData();
			System.out.println("[Server -metadata] " + FileManager.representMetadata());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			RemoteReplica me = (RemoteReplica)ConfigManager.getMe();
			
			// create a socket and wait for connections
			ServerSocket serverSocket = new ServerSocket(me.getPort());
			System.out.println("[Server] Server started " + me);
			
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
	            		ThreadReplicaServer thread = new ThreadReplicaServer(serverSocket, clientSocket, out, in, command);
	            		new Thread(thread).start();
	            	
	            	// a client asks to read a file
	                } else if (command.equals(UtilBobby.CLIENT_READ)){
	                	
	                	// instantiate a read thread
	                	System.out.println("[Server] Initializing a read thread for " + clientSocket.getInetAddress());
	            		ThreadRead thread = new ThreadRead(serverSocket, clientSocket, out, in);
	            		new Thread(thread).start();

	            	// a client asks to delete
	                } else if (command.equals(UtilBobby.CLIENT_DELETE)){

	                	// instantiate a read thread
	                	System.out.println("[Server] Initializing a delete thread for " + clientSocket.getInetAddress());
	            		ThreadDelete thread = new ThreadDelete(serverSocket, clientSocket, out, in);
	            		new Thread(thread).start();

	            	// a client asks to write or delete
	                } else{
	                	
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
