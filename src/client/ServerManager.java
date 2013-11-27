package client;

import java.io.IOException;
import java.net.UnknownHostException;

import common.ConfigManager;
import common.File;

/**
 * Hide to client how to get the current server, redirection works etc...
 */
public class ServerManager {
	static Server currentServer;
	static int	  currentServerCount = 0;
	
	static {
		System.out.println("[Init ServeurManager]");
		currentServer = getNextServer();
	}
	
	private ServerManager() {}
	
	private static Server getNextServer() {
		int nbServer = ConfigManager.getRemoteNodes().size();
		Server tmp = (Server) ConfigManager.getRemoteNodes().get(currentServerCount);
		currentServerCount = (currentServerCount + 1) % nbServer;
		return tmp;
	}
	
	
	public static void write(File file) throws UnknownHostException, IOException {
		// TODO get the next server, if the current one is null
		if(currentServer != null)
			currentServer.write(file);
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
