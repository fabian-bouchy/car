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
import common.RemoteNode;
import common.UtilBobby;

public class ThreadRead extends ThreadWorker{
	public ThreadRead(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[thread server read] init");
	}
	
	@Override
	public void run() {
		try {
			// send to client that the server is ready
			out.println(UtilBobby.SERVER_READ_READY);
			ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
			File metadata = (File) reader.readObject();
			System.out.println("[thread read] reading: " + metadata.getFileName());

			// Get file form our list
			File fileRead = FileManager.getFile(metadata.getId());
			if( fileRead != null) {
				System.out.println("[thread read] file found");
				// send to client that the file is found
				out.println(UtilBobby.SERVER_READ_FILE_FOUND);
				ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
	        	outStream.writeObject(fileRead);
	        	System.out.println("[thread read] reading succeeded!");
			} else {
				System.out.println("[thread read] reading failed: file not found locally");
				System.out.println("[thread read] reading failed: reading metadata...");

				File fileMetadata = FileManager.getMetadata(metadata.getId());
				if(fileMetadata != null) {
					System.out.println("[Server] looking for the file on replicas...");
					RemoteNode nextHop = this.replicaManager.has(metadata);
					if(nextHop != null){
						System.out.println("[thread read] redirecting to " + nextHop);
						out.println(UtilBobby.SERVER_READ_REDIRECT_TO + UtilBobby.SPLIT_REGEX + nextHop.getName());
						return;
					}
				}
				// Not found
				System.out.println("[thread read] file not found anywhere");
				out.println(UtilBobby.SERVER_READ_FILE_NOT_FOUND);
			}
		} catch (IOException e )  {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
