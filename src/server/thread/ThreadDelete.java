package server.thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import common.File;
import common.FileManager;
import common.UtilBobby;

public class ThreadDelete extends ThreadWorker{

	public ThreadDelete(ServerSocket serverSocket, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[thread server delete] init");
	}

	@Override
	public void run() {
		try {
			// send to client that the server is ready
			out.writeObject(UtilBobby.SERVER_DELETE_READY);
			File file = (File) in.readObject();
			System.out.println("[Server] deleting: " + file);

			// Remove file form our list
			if(FileManager.getFile(file.getId()) != null) {
				FileManager.removeFile(file.getId());
				System.out.println("[Server] delete succeeded");
			} else {
				System.out.println("[Server] delete failed: file not found.");
			}

			// Broadcast delete to replicas
			// Succeed = DELETE or file not found on replica
			if(replicaManager.delete(file)) {
				out.writeObject(UtilBobby.SERVER_DELETE_OK);
				close();
				return;
			}
		} catch (IOException e )  {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			out.writeObject(UtilBobby.SERVER_DELETE_KO);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
