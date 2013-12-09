package server.thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import common.ConfigManager;
import common.File;
import common.FileManager;
import common.UtilBobby;

/**
 * Thread to answer to the write/update sessions in the server side.
 * Receive and start replica sessions to replicate the file.
 */
public class ThreadWrite extends ThreadWorker{

	public ThreadWrite(ServerSocket serverSocket, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[ThreadWrite] init");
	}

	@Override
	public void run() {
		System.out.println("[ThreadWrite] running...");
		try {
			out.writeObject(UtilBobby.SERVER_WRITE_READY);
			File file = (File) in.readObject();
			System.out.println("[ThreadWrite] Object received: " + file + " from client!");
			
			// Manager version
			int myId = ConfigManager.getMe().getPriority();
			File fileOrMetadata = FileManager.getFileOrMetadata(file.getId());
			
			if(fileOrMetadata == null) // file doesn't exist on this server or any other server
			{
				System.out.println("[ThreadWrite] Creating new file " + file);
				
				file.reinitializeVector();
				file.incrementVersion(myId);
				FileManager.addOrReplaceFile(file);
				
				// replicate on all servers & reply to the client
				if(replicaManager.replicate(file, true)){
					System.out.println("[ThreadWrite] Replication of new file finished: " + file);
					out.writeObject(UtilBobby.SERVER_WRITE_OK);
				}else{
					System.out.println("[ThreadWrite] Replication of new file FAILED: " + file);
					out.writeObject(UtilBobby.SERVER_WRITE_KO);
				}
			}
			else 
			{
				if(fileOrMetadata.isFile()){
					// file exists on this node
					System.out.println("[ThreadWrite] File found on this node: "+fileOrMetadata);
				}else{
					// file exists on another node
					System.out.println("[ThreadWrite] Metadata of the file found "+fileOrMetadata);
				}
				
				// acquire lock
				try{
					fileOrMetadata.lock();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				
				file.reinitializeVector();
				file.setVersion(fileOrMetadata.getVersion());
				file.incrementVersion(myId);
				
				// replicate on all servers
				if(replicaManager.replicate(file, false)) {
					
					System.out.println("[ThreadWrite] Replication succeeded: " + file);
					
					FileManager.commit(file.getId());
					if(fileOrMetadata.isFile()){
						FileManager.addOrReplaceFile(file);
					}
					
					// send to client that the write succeeded
					out.writeObject(UtilBobby.SERVER_WRITE_OK);
				} else {
					
					System.out.println("[ThreadWrite] Replication failed: " + file);
					
					FileManager.abort(file.getId());
					
					// send to client that the write failed
					out.writeObject(UtilBobby.SERVER_WRITE_KO);
				}
				
				try{
					fileOrMetadata.unlock();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				
				System.out.println(FileManager.represent());
				System.out.println(FileManager.representMetadata());
			}
		} catch (IOException e )  {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
