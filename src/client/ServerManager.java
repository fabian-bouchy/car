package client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;

import common.ConfigManager;
import common.File;
import common.RemoteNode;

/**
 * Hide to client how to get the current server, redirection works etc...
 */
public class ServerManager {
	static RemoteServer currentServer;
	static Iterator<RemoteNode> currentServerIterator = null;
	
	static {
		System.out.println("[Init ServeurManager]");
	}
	
	private ServerManager() {}
	
	private static void initRemoteNodeAndIterator(){
		currentServerIterator = ConfigManager.getRemoteNodes().values().iterator();
		currentServer = (RemoteServer) currentServerIterator.next();
	}
	
	private static RemoteServer getNextRemoteServer() {
		return (RemoteServer) currentServerIterator.next();
	}
	
	public static void write(File file) throws UnknownHostException, IOException {
		// try the first servers
		boolean sent = false;

		initRemoteNodeAndIterator();

		while (!sent && currentServerIterator.hasNext()){
			try{
				currentServer.write(file);
				sent = true;
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("[server manager] Error writing to server " + currentServer);
				System.out.println("[server manager] " + e);
				currentServer = getNextRemoteServer();
				System.out.println("[server manager] Current server changed to " + currentServer);
			}
		}	
		if(!sent)
			System.out.println("[server manager] No servers available. Failed to send.");
	}
	
	public static File read(File file) {
		// try the first servers
		boolean read = false;

		initRemoteNodeAndIterator();

		while (!read && currentServerIterator.hasNext()){
			try{
				return currentServer.read(file);
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("[server manager] Error reading to server " + currentServer);
				System.out.println("[server manager] " + e);
				currentServer = getNextRemoteServer();
				System.out.println("[server manager] Current server changed to " + currentServer);
			}
		}
		System.out.println("[server manager] No servers available. Failed to read.");
		return null;
	}
	
	public static void delete(File file) {
		// try the first servers
		boolean delete = false;

		while (!delete && currentServerIterator.hasNext()){
			try{
				currentServer.delete(file);
				delete = true;
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("[server manager] Error deleting to server " + currentServer);
				System.out.println("[server manager] " + e);
				currentServer = getNextRemoteServer();
				System.out.println("[server manager] Current server changed to " + currentServer);
			}
		}
		System.out.println("[server manager] No servers available. Failed to delete.");
	}
	
	public static String[] listFiles() {
		return null;
	}
}
