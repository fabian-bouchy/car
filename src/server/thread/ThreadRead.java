package server.thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import common.File;
import common.FileManager;
import common.RemoteNode;
import common.UtilBobby;

public class ThreadRead extends ThreadWorker{
	public ThreadRead(ServerSocket serverSocket, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[thread server read] init");
	}
	
	@Override
	public void run() {
		try {
			// send to client that the server is ready
			out.writeObject(UtilBobby.SERVER_READ_READY);
			File metadata = (File) in.readObject();
			System.out.println("[thread read] reading: " + metadata.getFileName());

			// Get file from our list
			File fileRead = FileManager.getFile(metadata.getId());
			if( fileRead != null) {
				System.out.println("[thread read] file found");
				// send to client that the file is found
				out.writeObject(UtilBobby.SERVER_READ_FILE_FOUND);
	        	out.writeObject(fileRead);
	        	System.out.println("[thread read] reading succeeded!");
	        	close();
			} else {
				System.out.println("[thread read] reading failed: file not found locally");
				System.out.println("[thread read] reading failed: reading metadata...");

				File fileMetadata = FileManager.getMetadata(metadata.getId());
				if(fileMetadata != null) {
					System.out.println("[Server] looking for the file on replicas...");
					RemoteNode nextHop = this.replicaManager.has(metadata);
					if(nextHop != null){
						System.out.println("[thread read] redirecting to " + nextHop);
						out.writeObject(UtilBobby.SERVER_READ_REDIRECT_TO + UtilBobby.SPLIT_REGEX + nextHop.getName());
						return;
					}
				}
				// Not found
				System.out.println("[thread read] file not found anywhere");
				out.writeObject(UtilBobby.SERVER_READ_FILE_NOT_FOUND);
				close();
			}
		} catch (IOException e )  {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
