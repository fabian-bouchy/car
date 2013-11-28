package server.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import common.File;
import common.FileManager;
import common.UtilBobby;

public class ThreadRead extends ThreadWorker{
	public ThreadRead(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[thread server read] init");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// send to client that the server is ready
			out.println(UtilBobby.SERVER_READ_READY);
			ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
			File metadata = (File) reader.readObject();
			System.out.println("[Server] reading: " + metadata);

			// Get file form our list
			File fileRead = FileManager.getFile(metadata.getId());
			if( fileRead != null) {
				System.out.println("[Server] file found");
				// send to client that the file is found
				out.println(UtilBobby.SERVER_READ_FILE_FOUND);
				ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
	        	outStream.writeObject(fileRead);
			} else {
				System.out.println("[Server] reading failed: file not found locally.");
				// TODO Found file in another server here!
				out.println(UtilBobby.SERVER_READ_KO);
			}
		} catch (IOException e )  {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
