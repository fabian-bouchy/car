package server.main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import server.RemoteReplica;
import server.thread.ThreadDelete;
import server.thread.ThreadDiscovery;
import server.thread.ThreadListFiles;
import server.thread.ThreadRead;
import server.thread.ThreadReplicaServer;
import server.thread.ThreadWrite;
import common.ConfigManager;
import common.ConfigManager.ConfigType;
import common.UtilBobby;

/**
 *	@author mickey
 *	
 *	This is our main server class - we are waiting for connections from clients (both end users and other replicas) and dispatching events.
 *
 *	Main threads are (all of them extend the common ThreadWorker class):
 *		- ThreadWrite (client sends us a file)
 *		- ThreadRead (client wants a file)
 *		- ThreadDelete (client wants to delete a file)
 *		- ThreadReplicaServer (another replica wants to synchronize)
 *
 */
public class Server {
	
	public void run(String[] args) throws Exception{
		
		// initialize the configuration
		if (args.length == 3){
			// configure name and host name
			ConfigManager.init(ConfigType.SERVER, args[1], args[2]);
		}else if (args.length == 2){
			// only configure name
			ConfigManager.init(ConfigType.SERVER, args[1]);
		}else{
			// all default parameters
			ConfigManager.init(ConfigType.SERVER);
		}

    	// instantiate a read thread
    	System.out.println("[Server] Initializing a discovery thread");
		new Thread(new ThreadDiscovery()).start();

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
	                
	                // create input-output
	                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());  
	                out.flush();
	                ObjectInputStream  in  = new ObjectInputStream(clientSocket.getInputStream());
	                
	                String command = (String) in.readObject();
	                
	                System.out.println("[Server] Received a message: '" + command + "' " + clientSocket.getInetAddress());
	                
	                // the replica commands family - interactions between servers
	                if (command.contains("replica")){
	                	
	                	// instantiate a replica server thread
	                	System.out.println("[Server] Initializing a replica server thread for " + clientSocket.getInetAddress());
	            		ThreadReplicaServer thread = new ThreadReplicaServer(serverSocket, clientSocket, out, in, command);
	            		new Thread(thread).start();
	            	
	                }else{
	                	
	                	// if we are accepting the user connections
	                	if (ConfigManager.isAvailable()){
		                	
			            	// a client asks to read a file
			                if (command.equals(UtilBobby.CLIENT_READ)){
			                	
			                	// instantiate a read thread
			                	System.out.println("[Server] Initializing a read thread for " + clientSocket.getInetAddress());
			            		ThreadRead thread = new ThreadRead(serverSocket, clientSocket, out, in);
			            		new Thread(thread).start();
		
			            	// a client asks to delete a file
			                } else if (command.equals(UtilBobby.CLIENT_DELETE)){
		
			                	// instantiate a delete thread
			                	System.out.println("[Server] Initializing a delete thread for " + clientSocket.getInetAddress());
			            		ThreadDelete thread = new ThreadDelete(serverSocket, clientSocket, out, in);
			            		new Thread(thread).start();
		
			            	// a client asks to list his file
			                } else if (command.contains(UtilBobby.CLIENT_LIST)){
		
			                	// instantiate a listfile thread
			                	System.out.println("[Server] Initializing a list file thread for " + clientSocket.getInetAddress());
			            		ThreadListFiles thread = new ThreadListFiles(serverSocket, clientSocket, out, in, command);
			            		new Thread(thread).start();
		
			            	// a client asks to write or update a file
			                } else{
		
			                	// instantiate a create/update thread
			                	System.out.println("[Server] Initializing a write thread for " + clientSocket.getInetAddress());
			            		ThreadWrite thread = new ThreadWrite(serverSocket, clientSocket, out, in);
			            		new Thread(thread).start();
			                }
	                	}else{
	                		
	                		// user operations are locked, refuse the connection
	                		in.close();
	                		out.close();
	                		clientSocket.close();
	                	}
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
