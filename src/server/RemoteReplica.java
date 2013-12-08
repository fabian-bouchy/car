package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.HashMap;

import common.File;
import common.RemoteNode;
import common.UtilBobby;

public class RemoteReplica extends RemoteNode{
	public RemoteReplica(String sName, String sIpAddress, int iPriority, int iPort) {
		super(sName, sIpAddress, iPriority, iPort);
	}

	public void write(File file) throws UnknownHostException, IOException{

		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());
		
		// Write or override file on replica
		out.writeObject(UtilBobby.REPLICA_WRITE + UtilBobby.SPLIT_REGEX + file.getId());
		
		String line;
		try {
			line = (String) in.readObject();
			
			if (line.contains(UtilBobby.REPLICA_WRITE_READY)){
				
				if (line.equals(UtilBobby.REPLICA_WRITE_READY_FILE)){
					out.writeObject(file);
				}else{
					out.writeObject(file.generateMetadata());
				}
				
				// Check if write OK
				String status = (String) in.readObject();
				if(status.equals(UtilBobby.REPLICA_WRITE_OK)){
					System.out.println("[remote replica] Replication OK: " + file);
				}else{
					throw new InvalidParameterException();
				}
			}else{
				throw new IOException();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	public boolean has(File metadata) throws UnknownHostException, IOException{
		System.out.println("[RemoteReplica - has] " + metadata + "?");
		
		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());

		// send the command
		out.writeObject(UtilBobby.REPLICA_HAS + UtilBobby.SPLIT_REGEX + metadata.getId());

		// return the answer
		boolean answer = false;
		try {
			answer = ((String) in.readObject()).equals(UtilBobby.ANSWER_TRUE);
			System.out.println("[RemoteReplica - has] " + answer );
			return answer;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return answer;
	}

	@Override
	public boolean commitWrite(File file) throws UnknownHostException, IOException{
		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());
		
		out.writeObject(UtilBobby.REPLICA_TRANSACTION_COMMIT + UtilBobby.SPLIT_REGEX + file.getId());

		// return the answer
		boolean answer = false;
		try {
			answer = ((String) in.readObject()).equals(UtilBobby.REPLICA_TRANSACTION_COMMITED);
			System.out.println("[RemoteReplica - has] " + answer );
			return answer;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return answer;
	}

	@Override
	public boolean abortWrite(File file) throws UnknownHostException, IOException {
		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());
		
		out.writeObject(UtilBobby.REPLICA_TRANSACTION_ABORT + UtilBobby.SPLIT_REGEX + file.getId());

		// return the answer
		boolean answer = false;
		try {
			answer = ((String) in.readObject()).equals(UtilBobby.REPLICA_TRANSACTION_ABORTED);
			System.out.println("[RemoteReplica - has] " + answer );
			return answer;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return answer;
	}

	public boolean delete(File file)throws UnknownHostException, IOException{
		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());
		
		out.writeObject(UtilBobby.REPLICA_DELETE + UtilBobby.SPLIT_REGEX + file.getId());

		// return the answer
		boolean answer = false;
		try {
			String text = ((String) in.readObject());
			answer = text.equals(UtilBobby.REPLICA_DELETE_OK) || text.equals(UtilBobby.REPLICA_DELETE_NOT_FOUND);
			System.out.println("[RemoteReplica - has] " + answer );
			return answer;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return answer;
	}

	public HashMap<String, File> getMetadata() throws UnknownHostException, IOException, ClassNotFoundException {
		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());
		
		out.writeObject(UtilBobby.REPLICA_METADATA_GET);

		try {
			String line = ((String) in.readObject());
		
			if (line.equals(UtilBobby.REPLICA_METADATA_READY)){
				try{
					HashMap<String, File> metadata = (HashMap<String, File>) in.readObject();
					out.writeObject(UtilBobby.REPLICA_METADATA_OK);
					System.out.println("[remote replica] metadata sent");
					return metadata;
				} catch (Exception e){
					System.out.println("[remote replica] error readObject!");
					out.writeObject(UtilBobby.REPLICA_METADATA_KO);
					e.printStackTrace();
					return null;
				}
			}else{
				System.out.println("[remote replica] replica not ready to metadata!");
				throw new IOException();
			}
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String toString(){
		return "[remote replica] ("+this.getPriority()+") "+this.getName()+" - "+this.getIpAddress()+":"+this.getPort();
	}

	@Override
	public File read(File file) throws UnknownHostException, IOException {
		// Initialize the connection:
		Socket socketToServer  = this.connect();
		ObjectOutputStream out = new ObjectOutputStream(socketToServer.getOutputStream());                   
		ObjectInputStream  in  = new ObjectInputStream(socketToServer.getInputStream());
		
		out.writeObject(UtilBobby.REPLICA_READ + UtilBobby.SPLIT_REGEX + file.getId());
		
		try {
			File input = ((File) in.readObject());
			socketToServer.close();
			return input;
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
