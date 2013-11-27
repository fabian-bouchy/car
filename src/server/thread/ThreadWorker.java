package server.thread;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import server.ReplicasManager;

public abstract class ThreadWorker implements Runnable{

	protected Socket clientSocket;
	protected ServerSocket serverSocket;
	protected PrintWriter out;
	protected BufferedReader in;

	protected ReplicasManager replicasManager;

	public ThreadWorker(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		this.serverSocket = serverSocket;
		this.clientSocket = clientSocket;
		this.out = out;
		this.in = in;
		this.replicasManager = new ReplicasManager();
	}

}