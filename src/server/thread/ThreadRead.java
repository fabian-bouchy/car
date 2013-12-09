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
import common.UtilPrinter;

/**
 * Thread to answer to the read session in the server.
 * Send to the server the file read or an error message when it's not found. 
 */
public class ThreadRead extends ThreadWorker{
	public ThreadRead(ServerSocket serverSocket, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[ThreadRead] init");
	}
	
	@Override
	public void run() {
		try {
			// send to client that the server is ready
			out.writeObject(UtilBobby.SERVER_READ_READY);
			File metadata = (File) in.readObject();
			System.out.println("[ThreadRead] reading: " + metadata.getFileName());

			// Get file from our list
			File fileRead = FileManager.getFile(metadata.getId());
			if( fileRead != null) {
				System.out.println("[ThreadRead] file found");
				// send to client that the file is found
				out.writeObject(UtilBobby.SERVER_READ_FILE_FOUND);
	        	out.writeObject(fileRead);
	        	System.out.println("[ThreadRead] reading succeeded!");
	        	close();
			} else {
				UtilPrinter.printlnWarning("[ThreadRead] reading failed: file not found locally");
				UtilPrinter.printlnError("[ThreadRead] reading failed: reading metadata...");

				File fileMetadata = FileManager.getMetadata(metadata.getId());
				if(fileMetadata != null) {
					System.out.println("[ThreadRead] looking for the file on replicas...");
					RemoteNode nextHop = this.replicaManager.has(metadata);
					if(nextHop != null){
						System.out.println("[ThreadRead] redirecting to " + nextHop);
						out.writeObject(UtilBobby.SERVER_READ_REDIRECT_TO + UtilBobby.SPLIT_REGEX + nextHop.getName());
						return;
					}
				}
				// Not found
				UtilPrinter.printlnError("[ThreadRead] file not found anywhere");
				out.writeObject(UtilBobby.SERVER_READ_FILE_NOT_FOUND);
				close();
			}
		} catch (IOException e )  {
			UtilPrinter.printlnError("[ThreadRead] read failed: " + e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			UtilPrinter.printlnError("[ThreadRead] read failed: " + e.getLocalizedMessage());
		}
	}
}
