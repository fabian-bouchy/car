package server.thread;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadRead extends ThreadWorker{
	public ThreadRead(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[thread server read] init");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
