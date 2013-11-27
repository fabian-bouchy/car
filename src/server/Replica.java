package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import common.File;
import common.RemoteNode;
import common.UtilBobby;

public class Replica extends RemoteNode{
	public Replica(String sName, String sIpAddress, String sInterface, int iPriority, int iPort) {
		super(sName, sIpAddress, sInterface, iPriority, iPort);
	}
	
	public void write(File file) throws UnknownHostException, IOException{
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		
		out.println(UtilBobby.REPLICA_WRITE);
		
		String line = in.readLine();
		
		if (line.equals(UtilBobby.REPLICA_WRITE_READY)){
			ObjectOutputStream outStream = new ObjectOutputStream(echoSocket.getOutputStream());
			outStream.writeObject(file);

			// Check if write successed
			String status = in.readLine();
			if(status.equals(UtilBobby.REPLICA_WRITE_OK))
				System.out.println("Replication successed file: " + file);
			else
				throw new IOException();
		}else{
			throw new IOException();
		}
	}
	
	public boolean has(String id) throws UnknownHostException, IOException{
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		
		// send the command
		out.println(UtilBobby.REPLICA_HAS + UtilBobby.SPLIT_REGEX + id);
		
		// return the answer
		return in.readLine().equals(UtilBobby.ANSWER_TRUE);
	}
	
	public boolean delete(String id)throws UnknownHostException, IOException{
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		
		out.println(UtilBobby.REPLICA_DELETE + UtilBobby.SPLIT_REGEX + id);
		
		// return the answer from the remote server
		return in.readLine().equals(UtilBobby.ANSWER_OK);
	}
	
	public String toString(){
		return "[replica] ["+this.getPriority()+"] "+this.getName()+" - "+this.getIpAddress()+"("+this.getPort()+"):"+this.getPort();
	}

	@Override
	public File read(String fileId) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
}