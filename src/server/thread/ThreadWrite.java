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
		try {
			ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
			out.println(UtilBobby.SERVER_WRITE_READY);
			File file = (File) reader.readObject();
			System.out.println("[server thread write] Object received: " + file + " from client!");
			
			// Manager version
			int myId = ConfigManager.getMe().getPriority();
			File fileOrMetadata = FileManager.getFileOrMetadata(file.getId());
			
			if(fileOrMetadata == null) // file doesn't exist on this server or any other server
			{
				System.out.println("[server thread write] Creating new file " + file);
				
				file.incrementVersion(myId);
				FileManager.addOrReplaceFile(file);
				
				// replicate on all servers
				if(replicaManager.replicate(file)) {
					
					System.out.println("[server thread write] Replication succeeded: " + file);
					
					FileManager.commit(file.getId());
					
					// send to client that the write succeeded
					out.println(UtilBobby.SERVER_WRITE_OK);
				} else {
					
					System.out.println("[server thread write] Replication failed: " + file);
					
					FileManager.abort(file.getId());
					// send to client that the write failed
					out.println(UtilBobby.SERVER_WRITE_KO);
				}
			}
			else 
			{
				if(fileOrMetadata.isFile()){
					// file exists on this node
					System.out.println("[server thread write] File found on this node: "+fileOrMetadata);
				}else{
					// file exists on another node
					System.out.println("[server thread write] Metadata of the file found "+fileOrMetadata);
				}
				
				// acquire lock
				try{
					fileOrMetadata.lock();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				
				file.incrementVersion(myId);
				
				// replicate on all servers
				if(replicaManager.replicate(file)) {
					
					System.out.println("[server thread write] Replication succeeded: " + file);
					
					FileManager.commit(file.getId());
					FileManager.addOrReplaceFile(file);
					
					// send to client that the write succeeded
					out.println(UtilBobby.SERVER_WRITE_OK);
				} else {
					
					System.out.println("[server thread write] Replication failed: " + file);
					
					FileManager.abort(file.getId());
					
					// send to client that the write failed
					out.println(UtilBobby.SERVER_WRITE_KO);
				}
				
				try{
					fileOrMetadata.unlock();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}


		} catch (IOException e )  {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
