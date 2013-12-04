package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import common.ConfigManager;
import common.File;
import common.RemoteNode;
import common.UtilBobby;

public class RemoteServer extends RemoteNode {

	public RemoteServer(String sName, String sIpAddress, String sInterface,
			int iPriority, int iPort) {
		super(sName, sIpAddress, sInterface, iPriority, iPort);
	}

	@Override
	public void write(File file) throws UnknownHostException, IOException {
		// Init the connection:
		System.out.println("[remote server] writing " + file);
		Socket socketToServer = this.connect();
		PrintWriter out = new PrintWriter(socketToServer.getOutputStream(), true);                   
        BufferedReader in = new BufferedReader(new InputStreamReader(socketToServer.getInputStream()));
        
        out.println(UtilBobby.CLIENT_WRITE);
        String answer = in.readLine();
        if(answer.equals(UtilBobby.SERVER_WRITE_READY)) {
        	// Create output stream to send file to server
        	ObjectOutputStream outStream = new ObjectOutputStream(socketToServer.getOutputStream());
			outStream.writeObject(file);
			answer = in.readLine();
			if(answer.equals(UtilBobby.SERVER_WRITE_OK))
				System.out.println("Send succeed!");
			else
				System.out.println("Send failed!");
        }
	}

	@Override
	public boolean has(File id) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(File file) throws UnknownHostException, IOException {
		// Init the connection:
		Socket socketToServer = this.connect();
		PrintWriter out = new PrintWriter(socketToServer.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socketToServer.getInputStream()));

        out.println(UtilBobby.CLIENT_DELETE);
        String answer = in.readLine();
        if(answer.equals(UtilBobby.SERVER_DELETE_READY)) {
        	// Create output stream to send file to server
        	ObjectOutputStream outStream = new ObjectOutputStream(socketToServer.getOutputStream());
        	outStream.writeObject(file);
			answer = in.readLine();
			if(answer.equals(UtilBobby.SERVER_DELETE_OK))
				System.out.println("Delete succeed!");
			else
				System.out.println("Delete failed!");
        }
		return false;
	}

	@Override
	public File read(File metadata) throws Exception {
		// Init the connection:
		Socket socketToServer = this.connect();
		PrintWriter out = new PrintWriter(socketToServer.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socketToServer.getInputStream()));

        out.println(UtilBobby.CLIENT_READ);
        String answer = in.readLine();
        if(answer.equals(UtilBobby.SERVER_READ_READY)) {
        	System.out.println("[client] ready to read from " + this);
        	// Create output stream to send metadata to server 
        	ObjectOutputStream outStream = new ObjectOutputStream(socketToServer.getOutputStream());
        	outStream.writeObject(metadata);
			answer = in.readLine();
			if(answer.equals(UtilBobby.SERVER_READ_FILE_FOUND)){
				// Read file from server
				ObjectInputStream reader = new ObjectInputStream(socketToServer.getInputStream());
				File tmp = (File) reader.readObject();
				if(tmp != null ) {
					System.out.println("[remote server] File read "+tmp);
					tmp.writeToFile("read_"+ tmp.getFileName());
					return tmp;
				}
			}
			// TODO Change this stack overflow possible on read function...
			// Internal redirection
			else if(answer.contains(UtilBobby.SERVER_READ_REDIRECT_TO) && (answer.split(UtilBobby.SPLIT_REGEX).length == 4)){
				String nextHopName = answer.split(UtilBobby.SPLIT_REGEX)[3];
				RemoteNode nextHop =  ConfigManager.getRemoteNode(nextHopName);
				socketToServer.close();
				System.out.println("Redirected to " + nextHop);
				return nextHop.read(metadata);
			}
			else {
				System.out.println("Read failed: " + answer);
			}
        } else {
        	System.out.println("Server not ready for reading..." + answer);
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
}
