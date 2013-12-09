package server.thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import common.File;
import common.FileManager;
import common.UtilBobby;

/**
 * Use with a delete session. 
 */
public class ThreadDelete extends ThreadWorker{

	public ThreadDelete(ServerSocket serverSocket, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[ThreadDelete] init");
	}

	@Override
	public void run() {
		try {
			// send to client that the server is ready
			out.writeObject(UtilBobby.SERVER_DELETE_READY);
			File file = (File) in.readObject();
			System.out.println("[ThreadDelete] deleting: " + file);

			// Remove file form our list
			if(FileManager.getFile(file.getId()) != null) {
				FileManager.removeFile(file.getId());
				System.out.println("[ThreadDelete] delete succeeded");
			} else {
				System.out.println("[ThreadDelete] delete failed: file not found.");
			}

			// Broadcast delete to replicas
			// Succeed = DELETE or file not found on replica
			if(replicaManager.delete(file)) {
				out.writeObject(UtilBobby.SERVER_DELETE_OK);
				close();
				System.out.println(FileManager.represent());
				System.out.println(FileManager.representMetadata());
				return;
			}
		} catch (IOException e )  {
			System.out.println("[ThreadDelete] delete failed: " + e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("[ThreadDelete] delete failed: " + e.getLocalizedMessage());
		}
		try {
			out.writeObject(UtilBobby.SERVER_DELETE_KO);
		} catch (IOException e) {
			System.out.println("[ThreadDelete] delete send message failed: " + e.getLocalizedMessage());
		}
	}
}
