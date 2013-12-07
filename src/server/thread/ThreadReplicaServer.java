package server.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import common.ConfigManager;
import common.File;
import common.FileManager;
import common.RemoteNode;
import common.UtilBobby;

public class ThreadReplicaServer extends ThreadWorker{

	private String command;
	
	public ThreadReplicaServer(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in, String command){
		super(serverSocket, clientSocket, out, in);
		this.command = command;
		System.out.println("[thread replica server] init for other replica "+clientSocket.getInetAddress());
	}
	
	@Override
	public void run() {
		System.out.println("[thread replica server] run");
		try {
			String[] cmd = command.split(UtilBobby.SPLIT_REGEX);
			System.out.println("[thread replica server] " + command + " (" + cmd.length + ")");
			
			if(cmd.length >= 2) {
				
				// Be sure that the message is for the replica thread!
				if(cmd[0].compareTo(UtilBobby.REPLICA) != 0){
					System.out.println("[thread replica server] wrong command");
					return;
				}
				
/*
 *	----------------------------------------------------------------------------------------------------------------------
 *	METADATA
 */
				
				// send all meta-data from the node: the ones I have and the ones I don't have
				if (cmd[1].equals(UtilBobby.REPLICA_METADATA_SYMBOL)) {
					
					if (cmd[2].equals(UtilBobby.REPLICA_METADATA_GET_SYMBOL)) {
						
						ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
						out.println(UtilBobby.REPLICA_METADATA_READY);
						System.out.println("[thread replica server] get metadata ready");
						
						// external meta-data
						HashMap<String, File> metadata = FileManager.getMetadata();
						// internal meta-data
						for (File file : FileManager.getFiles().values()){
							metadata.put(file.getId(), file.generateMetadata());
						}
						
						outStream.writeObject(metadata);
						
						if (in.readLine().equals(UtilBobby.REPLICA_METADATA_OK)){
							System.out.println("[thread replica server] metadata sent!");
						}else{
							System.out.println("[thread replica server] metadata not sent!");
						}
					}
				}

/*
 *	----------------------------------------------------------------------------------------------------------------------
 *	WRITE
 */
				
				// store the files/metadata we receive
				if(cmd[1].equals(UtilBobby.REPLICA_WRITE_SYMBOL)) {
					
					System.out.println("[thread replica server] write");
					
					// check if we have the file
					File have = FileManager.getFile(cmd[2]);
					File metadata = FileManager.getMetadata(cmd[2]);
					// send a "ready" message
					if (have != null || metadata == null){
						out.println(UtilBobby.REPLICA_WRITE_READY_FILE);
					}else{
						out.println(UtilBobby.REPLICA_WRITE_READY_META);
					}
					// receive the file
					ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
					File file = (File) reader.readObject();
					
					System.out.println("[thread replica server] file received: "+file);
					
					if (file.isFile()){
						System.out.println("[thread replica server] is a file");
						// let's verify if there is a conflict
						File oldFile = FileManager.getFileOrMetadata(file.getId());

						if (oldFile == null)
						{
							// check if it's not already in the temporary storage waiting for a commit
							// new file
							System.out.println("[thread replica server] new file");
							FileManager.prepare(file); // store in temporary storage
							out.println(UtilBobby.REPLICA_WRITE_OK);
							System.out.println("[thread replica server] file prepared");
						}
						else 
						{
							System.out.println("[thread replica server] update");
							// TODO acquire a lock on the old file ?
							try{
								System.out.println("[thread replica server] acquiring a lock on : "+oldFile);
								oldFile.lock();
								System.out.println("[thread replica server] lock acquired on : "+oldFile);
							}catch(InterruptedException e){
								e.printStackTrace();
							}
							
							System.out.println("[thread replica server] comp:" + oldFile.isCompatibleWith(file) + " new:" + file.getGlobalVersion() + " old+1:" + (oldFile.getGlobalVersion() + 1));
							
							if (oldFile.isCompatibleWith(file) && (file.getGlobalVersion() >= (oldFile.getGlobalVersion() + 1)))
							{
								// not new, but no conflict
								// TODO verify conditions and locks
								// TODO unlock in commit
								FileManager.prepare(file); // store in temporary storage
								out.println(UtilBobby.REPLICA_WRITE_OK);
								
								try {
									oldFile.unlock();
									System.out.println("[thread replica server] released the lock on : "+oldFile);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							else
							{
								// there is a conflicted copy
								int myPriority = ConfigManager.getMe().getPriority();
								int theirPriority = Integer.MIN_VALUE;
								RemoteNode them = ConfigManager.getRemoteNodeByIp(clientSocket.getInetAddress().getHostAddress());
								if (them != null){
									theirPriority = them.getPriority();
								}
								
								System.out.println("[thread replica server] my priority is " + myPriority + " theirs is " +theirPriority);
								
								if (myPriority < theirPriority){
									// TODO verify this is correct
									System.out.println("[thread replica server] I obey and store the new file");
									FileManager.addOrReplaceFile(file);
									out.println(UtilBobby.REPLICA_WRITE_OK);
								}else{
									// reject the file
									System.out.println("[thread replica server] I refuse the new file");
									out.println(UtilBobby.REPLICA_WRITE_KO);
								}
								
								try {
									oldFile.unlock();
									System.out.println("[thread replica server] released the lock on : "+oldFile);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							
						}
						
					}else{
						System.out.println("[thread replica server] metadata received "+file);
						// it's a meta-data file, no content
						// we just store the new value and say we're good
						file.setHasFile(false);
						FileManager.prepare(file); // store in temporary storage
						out.println(UtilBobby.REPLICA_WRITE_OK);
					}
				}
				
/*
 *	---------------------------------------------------------------------------------------------------------------------- 
 *	HAS
 */
				
				// check if the file exists on this node
				if(cmd[1].equals(UtilBobby.REPLICA_HAS_SYMBOL)) {
					System.out.println("[thread replica server] has");
					
					if (FileManager.getFile(cmd[2]) != null){
						out.println(UtilBobby.ANSWER_TRUE);
					}else{
						out.println(UtilBobby.ANSWER_FALSE);
					}
				}
				
/*
 *	----------------------------------------------------------------------------------------------------------------------
 *	TRANSACTION
 */

				// commit file
				if(cmd[1].equals(UtilBobby.REPLICA_TRANSACTION_SYMBOL)){

					if(command.contains(UtilBobby.REPLICA_TRANSACTION_COMMIT)) {
						System.out.println("[thread replica server] commit for " + cmd[3]);
						FileManager.commit(cmd[3]);
						out.println(UtilBobby.REPLICA_TRANSACTION_COMMITED);
					} else {
						System.out.println("[thread replica server] abort for " + cmd[3]);
						FileManager.abort(cmd[3]);
						out.println(UtilBobby.REPLICA_TRANSACTION_ABORTED);
					}
				}
				
/*
 *	----------------------------------------------------------------------------------------------------------------------
 *	DELETE
 */

				// delete the file
				if(cmd[1].equals(UtilBobby.REPLICA_DELETE_SYMBOL)){
					try {
						System.out.println("[thread replica server] delete");
						if (FileManager.getFile(cmd[2]) != null){
							FileManager.removeFile(cmd[2]);
							System.out.println("[thread replica server] delete succeed");
							out.println(UtilBobby.REPLICA_DELETE_OK);
						}else{
							System.out.println("[thread replica server] delete failed: file not found.");
							out.println(UtilBobby.REPLICA_DELETE_NOT_FOUND);
						}
					} catch (Exception e) {
						out.println(UtilBobby.REPLICA_DELETE_KO);
					}
				}
/*
 *	---------------------------------------------------------------------------------------------------------------------- 
 */
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("[thread replica server] end");
		System.out.println();
	}
}
