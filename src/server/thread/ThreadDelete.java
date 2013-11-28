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
		try {
			// send to client that the server is ready
			out.println(UtilBobby.SERVER_DELETE_READY);
			ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
			File file = (File) reader.readObject();
			System.out.println("[Server] Deleting: " + file);

			// Remove file form our list
			if(FileManager.getFile(file.getId()) != null)
				FileManager.removeFile(file.getId());

			// TODO Check if delete succeed
			// Broadcast delete to replicas
			replicaManager.delete(file);
			// send to client that the write successed
			out.println(UtilBobby.SERVER_DELETE_OK);
		} catch (IOException e )  {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
