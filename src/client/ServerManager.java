package client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import common.ConfigManager;
import common.File;
import common.RemoteNode;

/**
 * Hide to client how to get the current server, redirection works etc...
 */
public class ServerManager {
	static RemoteServer currentServer = null;
	
	static {
		System.out.println("[ServerManager] init");
	}
	
	private ServerManager() {}

	/**
	 * Find an available remote node and initiate a write session.
	 * If this action failed, try for each other remote nodes.
	 * @param file The file to write
	 */
	public static void write(File file) throws UnknownHostException, IOException {
		
		for(RemoteNode server : ConfigManager.getRemoteNodesList()){
			
			currentServer = (RemoteServer) server;
			System.out.println("[ServerManager] Trying " + currentServer + " ... ");
			try{
				currentServer.write(file);
				return;
			}catch(Exception e){
				// server doesn't respond, display a  message and try another one
				System.out.println("[ServerManager] Error connecting to server: " + e);
			}
		}
		System.out.println("[ServerManager] No servers available");
	}

	/**
	 * Find an available remote node and initiate a read session.
	 * If this action failed, try for each other remote nodes.
	 * @param file The metadata to identify the request file.
	 * @return The file read or null if not found.
	 */
	public static File read(File file) {
		
		for(RemoteNode server : ConfigManager.getRemoteNodesList()){
			
			currentServer = (RemoteServer) server;
			System.out.print("[ServerManager] Trying " + currentServer + " ... ");
			try{
				return currentServer.read(file);
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("[ServerManager] Error connecting to server: " + e);
			}
		}
		
		System.out.println("[ServerManager] No servers available");
		return null;
	}

	/**
	 * Find an available remote node and initiate a delete session.
	 * If this action failed, try for each other remote nodes.
	 * @param file The metadata to identify the file to delete.
	 */
	public static void delete(File file) {
		
		for(RemoteNode server : ConfigManager.getRemoteNodesList()){
			
			currentServer = (RemoteServer) server;
			System.out.print("[ServerManager] Trying " + currentServer + " ... ");
			try{
				currentServer.delete(file);
				return;
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("[ServerManager] Error connecting to server: " + e);
			}
		}
		
		System.out.println("[ServerManager] No servers available");
	}

	/**
	 * Find an available remote node and initiate a listFile session.
	 * If this action failed, try for each other remote nodes.
	 * @param file The metadata to identify the request file
	 * @return The file read or null if not found.
	 */
	public static void listFile(String username) {

		for(RemoteNode server : ConfigManager.getRemoteNodesList()){

			currentServer = (RemoteServer) server;
			System.out.println("[ServerManager] Trying " + currentServer + " ... ");
			try{
				HashMap<String, File> userMetadata = currentServer.listFiles(username);
				if(userMetadata != null) {
					for (File metadata: userMetadata.values()) {
						System.out.println(metadata);
					}
				} else {
					System.out.println("[ServerManager] No files found for " + username);
				}
				return;
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("[ServerManager] Error connecting to server: " + e);
			}
		}

		System.out.println("[ServerManager] No servers available");
	}
}
