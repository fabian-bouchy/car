package client;

import java.io.IOException;
import java.net.UnknownHostException;

import common.ConfigManager;
import common.File;

/**
 * Hide to client how to get the current server, redirection works etc...
 */
public class ServerManager {
	static RemoteServer currentServer;
	static int	  currentServerCount = 0;
	
	static {
		System.out.println("[Init ServeurManager]");
		currentServer = getNextServer();
	}
	
	private ServerManager() {}
	
	private static RemoteServer getNextServer() {
		int nbServer = ConfigManager.getRemoteNodes().size();
		RemoteServer tmp = (RemoteServer) ConfigManager.getRemoteNodes().get(currentServerCount);
		currentServerCount = (currentServerCount + 1) % nbServer;
		return tmp;
	}
	
	
	public static void write(File file) throws UnknownHostException, IOException {
		
		// try the first servers
		int current = currentServerCount;
		boolean sent = false;
		
		while (!sent){
			try{
				currentServer.write(file);
				sent = true;
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("[server manager] Error writing to server " + currentServer);
				System.out.println("[server manager] " + e);
				currentServer = getNextServer();
				if (currentServerCount == current){
					System.out.println("[server manager] No servers available. Failed to send.");
					break;
				}
			}
		}	
	}
	
	public static File read(String fileName) {
		return null;
	}
	
	public static void delete(String fileName) {
	}
	
	public static String[] listFiles() {
		return null;
	}
}
