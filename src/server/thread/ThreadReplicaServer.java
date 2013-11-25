package server.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import common.File;

public class ThreadReplicaServer implements Runnable{

	private Socket clientSocket;
	private ServerSocket serverSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	public ThreadReplicaServer(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		this.serverSocket = serverSocket;
		this.clientSocket = clientSocket;
		this.out = out;
		this.in = in;
		
		System.out.println("[thread replica server] init");
	}
	
	@Override
	public void run() {
		System.out.println("[thread replica server] run");
		try {
			String input = in.readLine();
			String[] cmd = input.split(":");
			
			/**
			 * 0 : should be replica
			 * 1 : action write etc...
			 */
			if(cmd.length >= 2) {
				// Be sure that the message is for the replica thread!
				if(cmd[0].compareTo("replica") != 0)
					return;

				// To the job:
				switch (cmd[1]) {
				case "write":
					out.println("replica:write:ready");
					ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
					File file = (File) reader.readObject();
					System.out.println("Object received: " + file);
					file.writeToFile(file.getId());
					out.println("replica:write:ok");
					break;

				default:
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
