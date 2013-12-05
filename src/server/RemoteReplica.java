package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import common.File;
import common.RemoteNode;
import common.UtilBobby;

public class RemoteReplica extends RemoteNode{
	public RemoteReplica(String sName, String sIpAddress, String sInterface, int iPriority, int iPort) {
		super(sName, sIpAddress, sInterface, iPriority, iPort);
	}

	public void write(File file) throws UnknownHostException, IOException{
		// Checking if update was needed :
		if(file.getGlobalVersion() != 1 && !has(file))
			return;

		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));


		// Write or override file on replica.
		out.println(UtilBobby.REPLICA_WRITE);
		
		String line = in.readLine();
		
		if (line.equals(UtilBobby.REPLICA_WRITE_READY)){
			ObjectOutputStream outStream = new ObjectOutputStream(echoSocket.getOutputStream());
			outStream.writeObject(file);

			// Check if write OK
			String status = in.readLine();
			if(status.equals(UtilBobby.REPLICA_WRITE_OK)){
				System.out.println("[remote replica] Replication OK: " + file);
			}else{
				throw new IOException();
			}
		}else{
			throw new IOException();
		}
	}

	public boolean has(File metadata) throws UnknownHostException, IOException{
		System.out.println("[RemoteReplica - has] " + metadata + "?");
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

		// send the command
		out.println(UtilBobby.REPLICA_HAS + UtilBobby.SPLIT_REGEX + metadata.getId());

		// return the answer
		boolean answer = in.readLine().equals(UtilBobby.ANSWER_TRUE);
		System.out.println("[RemoteReplica - has] " + answer );
		return answer;
	}

	@Override
	public boolean commitWrite(File file) throws UnknownHostException, IOException{
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

		out.println(UtilBobby.REPLICA_TRANSACTION_COMMIT + UtilBobby.SPLIT_REGEX + file.getId());

		return in.readLine().equals(UtilBobby.REPLICA_TRANSACTION_COMMITED);
	}

	@Override
	public boolean abortWrite(File file) throws Exception {
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

		out.println(UtilBobby.REPLICA_TRANSACTION_ABORT + UtilBobby.SPLIT_REGEX + file.getId());

		return in.readLine().equals(UtilBobby.REPLICA_TRANSACTION_ABORTED);
	}

	public boolean delete(File file)throws UnknownHostException, IOException{
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		
		out.println(UtilBobby.REPLICA_DELETE + UtilBobby.SPLIT_REGEX + file.getId());
		
		// return the answer from the remote server
		String line = in.readLine();
		return line.equals(UtilBobby.REPLICA_DELETE_OK) || line.equals(UtilBobby.REPLICA_DELETE_NOT_FOUND);
	}

	public HashMap<String, File> getMetadata() throws UnknownHostException, IOException, ClassNotFoundException {
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

		out.println(UtilBobby.REPLICA_METADATA_GET);

		System.out.println("[remote replica] waiting metadata...");
		try {
			String line = in.readLine();
		
			if (line.equals(UtilBobby.REPLICA_METADATA_READY)){
				try{
					ObjectInputStream reader = new ObjectInputStream(echoSocket.getInputStream());
					// FIXME Sometimes block here
					HashMap<String, File> metadata = (HashMap<String, File>) reader.readObject();
					out.println(UtilBobby.REPLICA_METADATA_OK);
					System.out.println("[remote replica] metadata read!");
					return metadata;
				} catch (Exception e){
					System.out.println("[remote replica] error readObject!");
					out.println(UtilBobby.REPLICA_METADATA_KO);
					e.printStackTrace();
					return null;
				}
			}else{
				System.out.println("[remote replica] replica not ready to metadata!");
				throw new IOException();
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String toString(){
		return "[remote replica] ["+this.getPriority()+"] "+this.getName()+" - "+this.getIpAddress()+":"+this.getPort();
	}

	@Override
	public File read(File file) throws UnknownHostException, IOException {
		return null;
	}

	@Override
	public boolean addMetadata(File metadata) throws Exception {
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

		out.println(UtilBobby.REPLICA_METADATA_ADD);
		String line = in.readLine();
		if (line.equals(UtilBobby.REPLICA_METADATA_ADD_READY)){
			ObjectOutputStream outStream = new ObjectOutputStream(echoSocket.getOutputStream());
			outStream.writeObject(metadata);
		}else{
			throw new IOException();
		}
		// return the answer from the remote server
		line = in.readLine();
		return line.equals(UtilBobby.REPLICA_METADATA_ADDED);
	}

	@Override
	public boolean deleteMetadata(File metadata) throws Exception {
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

		out.println(UtilBobby.REPLICA_METADATA_DELETE);
		String line = in.readLine();
		if (line.equals(UtilBobby.REPLICA_METADATA_DELETE_READY)){
			ObjectOutputStream outStream = new ObjectOutputStream(echoSocket.getOutputStream());
			outStream.writeObject(metadata);
		}else{
			throw new IOException();
		}
		// return the answer from the remote server
		line = in.readLine();
		return line.equals(UtilBobby.REPLICA_METADATA_DELETED);
	}
}
