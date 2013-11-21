package server.thread;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadWrite implements Runnable{

	private Socket clientSocket;
	private ServerSocket serverSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	public ThreadWrite(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		this.serverSocket = serverSocket;
		this.clientSocket = clientSocket;
		this.out = out;
		this.in = in;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
