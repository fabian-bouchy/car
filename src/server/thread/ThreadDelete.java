package server.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import common.File;
import common.FileManager;
import common.UtilBobby;

public class ThreadDelete extends ThreadWorker{

	public ThreadDelete(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[thread server delete] init");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}
}
