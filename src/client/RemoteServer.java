package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import common.File;
import common.FileManager;
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
	public boolean has(String id) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(String id) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public File read(String fileId) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
