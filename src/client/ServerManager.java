package client;

import java.io.IOException;
import java.net.UnknownHostException;

import common.ConfigManager;
import common.File;
import common.RemoteNode;

/**
 * Hide to client how to get the current server, redirection works etc...
 */
public class ServerManager {
	static RemoteServer currentServer = null;
	
	static {
		System.out.println("[server manager] init");
	}
	
	private ServerManager() {}
	
	public static void write(File file) throws UnknownHostException, IOException {	
		
		for(RemoteNode server : ConfigManager.getRemoteNodes().values()){
			
			currentServer = (RemoteServer) server;
			System.out.print("[server manager] Trying " + currentServer + " ... ");
			try{
				currentServer.write(file);
				return;
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("Error connecting to server: " + e);
			}
		}
		
		System.out.println("[server manager] No servers available");
	}
	
	public static File read(File file) {
		
		for(RemoteNode server : ConfigManager.getRemoteNodes().values()){
			
			currentServer = (RemoteServer) server;
			System.out.print("[server manager] Trying " + currentServer + " ... ");
			try{
				return currentServer.read(file);
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("Error connecting to server: " + e);
			}
		}
		
		System.out.println("[server manager] No servers available");
		return null;
	}
	
	public static void delete(File file) {
		
		for(RemoteNode server : ConfigManager.getRemoteNodes().values()){
			
			currentServer = (RemoteServer) server;
			System.out.print("[server manager] Trying " + currentServer + " ... ");
			try{
				currentServer.delete(file);
				return;
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("Error connecting to server: " + e);
			}
		}
		
		System.out.println("[server manager] No servers available");
	}
	
	public static String[] listFiles() {
		return null;
	}
}
