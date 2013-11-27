package server.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import server.ReplicasManager;
import common.File;
import common.FileManager;
import common.UtilBobby;

public class ThreadWrite implements Runnable{

	private Socket clientSocket;
	private ServerSocket serverSocket;
	private PrintWriter out;
	private BufferedReader in;

	private ReplicasManager replicasManager;

	public ThreadWrite(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		this.serverSocket = serverSocket;
		this.clientSocket = clientSocket;
		this.out = out;
		this.in = in;
		this.replicasManager = new ReplicasManager();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// send to client that the server is ready
			out.println(UtilBobby.SERVER_WRITE_READY);
			ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
			File file = (File) reader.readObject();
			System.out.println("Object received: " + file + " form client!");
			// Persit object on hdd
			file.writeToFile(file.getId());
			// Persit object in memory
			FileManager.addFile(file);

			// TODO check if replication successed
			replicasManager.replicate(file);
			// send to client that the write successed
			out.println(UtilBobby.SERVER_WRITE_OK);
		} catch (IOException e )  {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}

}
