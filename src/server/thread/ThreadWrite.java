package server.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import common.ConfigManager;
import common.File;
import common.FileManager;
import common.UtilBobby;

public class ThreadWrite extends ThreadWorker{

	public ThreadWrite(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[thread server write] init");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// send to client that the server is ready
			out.println(UtilBobby.SERVER_WRITE_READY);
			ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
			File file = (File) reader.readObject();
			System.out.println("Object received: " + file + " form client!");
			
			// Manager version :
			int[] tmpVersion = file.getVersion(); // Should be 0.0.0.0 etc...
			int currentRemoteNodeId = ConfigManager.getMe().getPriority();
			
			// Exists => Update
			File metadataFile = FileManager.getMetadata(file.getId());
			if(metadataFile != null) {
				System.out.println("[server thread write] Metadata found!");
				// Set old version
				tmpVersion = metadataFile.getVersion();
				tmpVersion[currentRemoteNodeId] = metadataFile.getVersion()[currentRemoteNodeId]; 
			}
			
			// Update version
			file.incrementVersion(currentRemoteNodeId);
			
			// Replace current version of file
			FileManager.addOrReplaceFile(file);

			// TODO check if replication successed
			if(replicaManager.replicate(file)) {
				FileManager.commit(file.getId());
				replicaManager.propagateMetadataAdd(file.getMetadata());
				// send to client that the write succeed
				out.println(UtilBobby.SERVER_WRITE_OK);
			} else {
				FileManager.abort(file.getId());
				// send to client that the write failed
				out.println(UtilBobby.SERVER_WRITE_KO);
			}
		} catch (IOException e )  {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
