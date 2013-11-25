package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import common.File;

public class Replica {
	
	private String 	sName;
	private String 	sIpAddress;
	private int 	iPriority;
	private int 	iPort;

	public Replica(String sName, String sIpAddress, int iPriority, int iPort) {
		super();
		this.sName 		= sName;
		this.sIpAddress = sIpAddress;
		this.iPriority 	= iPriority;
		this.iPort 		= iPort;
	}
	
	public Socket connect() throws UnknownHostException, IOException{
		return new Socket(sIpAddress, iPort);
	}
	
	
	public void write(File file) throws UnknownHostException, IOException{
		
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		
		out.println("replica:write");
		
		String line = in.readLine();
		
		if (line.equals("OK")){
			ObjectOutputStream outStream = new ObjectOutputStream(echoSocket.getOutputStream());
			outStream.writeObject(file);
			outStream.close();
		}else{
			throw new IOException();
		}
		

	}
	
	public boolean has(String id) throws UnknownHostException, IOException{
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		
		// send the command
		// a replica should answer "true" or "false"
		out.println("replica:has:"+id);
		
		// test the output
		return in.readLine().equals("true");
	}
	
	public boolean delete(String id)throws UnknownHostException, IOException{
		Socket echoSocket = connect();
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		
		out.println("replica:delete:"+id);
		
		return in.readLine().equals("OK");
	}
	
	

	public String getName() {
		return sName;
	}

	public void setName(String sName) {
		this.sName = sName;
	}

	public String getIpAddress() {
		return sIpAddress;
	}

	public void setIpAddress(String sIpAddress) {
		this.sIpAddress = sIpAddress;
	}

	public int getPriority() {
		return iPriority;
	}

	public void setPriority(int iPriority) {
		this.iPriority = iPriority;
	}
	
	public int getPort() {
		return iPort;
	}

	public void setPort(int iPort) {
		this.iPort = iPort;
	}
	
	
}
