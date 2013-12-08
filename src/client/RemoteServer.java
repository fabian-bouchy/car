package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import common.ConfigManager;
import common.File;
import common.RemoteNode;
import common.UtilBobby;

/**
 *	@author mickey
 *	
 *	High-level representation of a replica server
 *
 */
public class RemoteServer extends RemoteNode {

	public RemoteServer(String sName, String sIpAddress, int iPriority, int iPort) {
		super(sName, sIpAddress, iPriority, iPort);
	}

	@Override
	public void write(File file) throws UnknownHostException, IOException {
		System.out.println("[remote server] writing " + file);
		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());
        
        out.writeObject(UtilBobby.CLIENT_WRITE);
        String answer;
		try {
			answer = (String) in.readObject();
			if(answer.equals(UtilBobby.SERVER_WRITE_READY)) {
				
				out.writeObject(file);
				answer = (String) in.readObject();
				if(answer.equals(UtilBobby.SERVER_WRITE_OK)){
					System.out.println("[remote server] Write done!");
				}else{
					System.out.println("[remote server] Write failed!");
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        
	}

	@Override
	public boolean has(File id) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(File file) throws UnknownHostException, IOException {
		// Initialize the connection:
		Socket socketToServer = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());

        out.writeObject(UtilBobby.CLIENT_DELETE);
        String answer;
		try {
			answer = (String) in.readObject();
			if(answer.equals(UtilBobby.SERVER_DELETE_READY)) {
				
				out.writeObject(file);
				answer = (String) in.readObject();
				
				if(answer.equals(UtilBobby.SERVER_DELETE_OK)){
					System.out.println("[remote server] Delete done!");
				}else{
					System.out.println("[remote server] Delete failed!");
				}
			}
			return false;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public File read(File metadata) throws Exception {
		System.out.println("[remote server] read init");
		// Initialize the connection:
		Socket socketToServer = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());

        out.writeObject(UtilBobby.CLIENT_READ);
        String answer = (String) in.readObject();
        
        if(answer.equals(UtilBobby.SERVER_READ_READY)) {
        	System.out.println("[remote server] ready to read from " + this);
        	
        	out.writeObject(metadata);
			answer = (String) in.readObject();
			
			if(answer.equals(UtilBobby.SERVER_READ_FILE_FOUND)){
				// Read file from server
				File tmp = (File) in.readObject();
				if(tmp != null ) {
					System.out.println("[remote server] File read "+tmp);
					return tmp;
				}
			}
			// TODO Change this stack overflow possible on read function...
			// Internal redirection
			else if(answer.contains(UtilBobby.SERVER_READ_REDIRECT_TO) && (answer.split(UtilBobby.SPLIT_REGEX).length == 4)){
				String nextHopName = answer.split(UtilBobby.SPLIT_REGEX)[3];
				RemoteNode nextHop =  ConfigManager.getRemoteNode(nextHopName);
				socketToServer.close();
				System.out.println("[remote server] Redirected to " + nextHop);
				return nextHop.read(metadata);
			}
			else if(answer.equals(UtilBobby.SERVER_READ_FILE_NOT_FOUND)){
				System.out.println("[remote server] File doesn't exist");
			}
			else {
				System.out.println("[remote server] Read failed: " + answer);
			}
        } else {
        	System.out.println("[remote server] Server not ready for reading..." + answer);
        }
		return null;
	}

	public String toString(){
		return "[remote server] "+this.getName()+" ("+this.getPriority()+") @ "+this.getIpAddress()+":"+this.getPort();
	}

	@Override
	public HashMap<String, File> getMetadata() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean commitWrite(File file) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean abortWrite(File file) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
