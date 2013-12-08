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
 *	High-level representation of a replica server
 */
public class RemoteServer extends RemoteNode {

	public RemoteServer(String sName, String sIpAddress, int iPriority, int iPort) {
		super(sName, sIpAddress, iPriority, iPort);
	}

	/**
	 * Start connection and implement the protocol "write" with the thread ThreadWrite.
	 */
	@Override
	public void write(File file) throws UnknownHostException, IOException {
		System.out.println("[remote server] writing " + file);
		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());

		// Init client write connection
        out.writeObject(UtilBobby.CLIENT_WRITE);
        String answer;
		try {
			answer = (String) in.readObject();
			// Be sure that the server is ready.
			if(answer.equals(UtilBobby.SERVER_WRITE_READY)) {
				// Send the file to the server.
				out.writeObject(file);
				answer = (String) in.readObject();
				// Check result return by the server.
				if(answer.equals(UtilBobby.SERVER_WRITE_OK)){
					System.out.println("[remote server] Write done!");
				}else{
					System.out.println("[remote server] Write failed!");
				}
			}
		} catch (ClassNotFoundException e) {
			System.out.println("[remote server] write failed: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Implment the protocol "delete" initiate with the thread ThreadDelete.
	 */
	@Override
	public boolean delete(File metadata) throws UnknownHostException, IOException {
		// Initialize the connection:
		Socket socketToServer = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());

		// Initiate client delete session
        out.writeObject(UtilBobby.CLIENT_DELETE);
        String answer;
		try {
			answer = (String) in.readObject();
			// Check if server is ready to receive the metadata to identify the file to delete it.
			if(answer.equals(UtilBobby.SERVER_DELETE_READY)) {
				// Send metadata data
				out.writeObject(metadata);
				answer = (String) in.readObject();
				// Check if delete succeed
				if(answer.equals(UtilBobby.SERVER_DELETE_OK)){
					System.out.println("[remote server] Delete done!");
				}else{
					System.out.println("[remote server] Delete failed!");
				}
			}
			return false;
		} catch (ClassNotFoundException e) {
			System.out.println("[remote server] delete failed: " + e.getLocalizedMessage());
		}
		return false;
	}

	/**
	 * Implment the protocol "read" initiate with the thread ThreadRead. 
	 */
	@Override
	public File read(File metadata) throws Exception {
		System.out.println("[remote server] read init");
		// Initialize the connection:
		Socket socketToServer = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());

		// Initiate read session
        out.writeObject(UtilBobby.CLIENT_READ);
        String answer = (String) in.readObject();
        
        if(answer.equals(UtilBobby.SERVER_READ_READY)) {
        	System.out.println("[remote server] ready to read from " + this);

        	// Send metadata to identify the request file
        	out.writeObject(metadata);
			answer = (String) in.readObject();
			
			if(answer.equals(UtilBobby.SERVER_READ_FILE_FOUND)){
				// Read file from server
				File tmp = (File) in.readObject();
				if(tmp != null ) {
					System.out.println("[remote server] File read " + tmp);
					return tmp;
				}
			}
			// TODO Change this stack overflow possible on read function...
			// Internal redirection in case the remote server doesn't own the file.
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

	/**
	 * Retreive all file for a specific usename.
	 */
	@Override
	public HashMap<String, File> listFiles(String username) throws Exception {
		System.out.println("[remote server] listing files for " + username);
		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());

        out.writeObject(UtilBobby.CLIENT_LIST + UtilBobby.SPLIT_REGEX + username);
        String answer;
		try {
			answer = (String) in.readObject();
			if(answer.equals(UtilBobby.SERVER_LIST_READY)) {
				System.out.println("[remote server] anwser : " + answer);
				HashMap<String, File> metadata = (HashMap<String, File>) in.readObject();
				return metadata;
			} else {
				System.out.println("[remote server] anwser : " + answer);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Function not yet necessary in this context.
	 */
	@Override
	public boolean has(File id) throws UnknownHostException, IOException {
		return false;
	}

	@Override
	public HashMap<String, File> getMetadata() throws Exception {
		return null;
	}

	@Override
	public boolean commitWrite(File file) throws Exception {
		return false;
	}

	@Override
	public boolean abortWrite(File file) throws Exception {
		return false;
	}
}
