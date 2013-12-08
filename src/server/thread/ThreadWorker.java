package server.thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import server.ReplicaManager;

public abstract class ThreadWorker implements Runnable{

	protected Socket clientSocket;
	protected ServerSocket serverSocket;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;

	protected ReplicaManager replicaManager;

	public ThreadWorker(ServerSocket serverSocket, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in){
		this.serverSocket = serverSocket;
		this.clientSocket = clientSocket;
		this.out = out;
		this.in = in;
		this.replicaManager = new ReplicaManager();
	}
	
	public void close(){
		try {
			in.close();
			out.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}