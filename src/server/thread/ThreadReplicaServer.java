package server.thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import common.ConfigManager;
import common.File;
import common.FileManager;
import common.RemoteNode;
import common.UtilBobby;

/**
 *	This thread is called when another replica calls this one to ask to do something (synchronization).
 *	It is reading the command, and executing it without any questions.
 *
 *	For parallel actions, it's using:
 *		- ThreadReplicaServerWrite (for	write and update actions)
 *		- ThreadReplicaServerDelete (yes, you guessed it, for deleting)
 *
 */
public class ThreadReplicaServer extends ThreadWorker{

	private String command;
	
	public ThreadReplicaServer(ServerSocket serverSocket, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in, String command){
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
					try {
						if (cmd[2].equals(UtilBobby.REPLICA_METADATA_GET_SYMBOL)) {

							out.writeObject(UtilBobby.REPLICA_METADATA_READY);
							System.out.println("[thread replica server] get metadata ready");

							// external meta-data
							HashMap<String, File> metadata = FileManager.getMetadata();
							// internal meta-data
							for (File file : FileManager.getFiles().values()){
								metadata.put(file.getId(), file.generateMetadata());
							}

							out.writeObject(metadata);

							if (((String)in.readObject()).equals(UtilBobby.REPLICA_METADATA_OK)){
								System.out.println("[thread replica server] metadata sent!");
							}else{
								System.out.println("[thread replica server] metadata not sent!");
							}						
						}
					} catch (Exception e) {
						out.writeObject(UtilBobby.REPLICA_METADATA_KO);
						System.out.println("[thread replica server] metadata failed! " + e.getLocalizedMessage());
					}
				}

/*
 *	----------------------------------------------------------------------------------------------------------------------
 *	WRITE
 */

				// store the files/metadata we receive
				if(cmd[1].equals(UtilBobby.REPLICA_WRITE_SYMBOL)) {
					try {

						System.out.println("[thread replica server] write");

						// check if we have the file
						File have = FileManager.getFile(cmd[2]);
						File metadata = FileManager.getMetadata(cmd[2]);
						// send a "ready" message
						if (have != null || metadata == null){
							out.writeObject(UtilBobby.REPLICA_WRITE_READY_FILE);
						} else {
							out.writeObject(UtilBobby.REPLICA_WRITE_READY_META);
						}
						// receive the file
						File file = (File) in.readObject();

						System.out.println("[thread replica server] file received: "+file);

						if (file.isFile()) {
							System.out.println("[thread replica server] is a file");
							// let's verify if there is a conflict
							File oldFile = FileManager.getFileOrMetadata(file.getId());

							if (oldFile == null) {
								// check if it's not already in the temporary storage waiting for a commit
								// new file
								System.out.println("[thread replica server] new file");
								FileManager.prepare(file); // store in temporary storage
								out.writeObject(UtilBobby.REPLICA_WRITE_OK);
								System.out.println("[thread replica server] file prepared");
							} else {
								System.out.println("[thread replica server] update");
								// TODO acquire a lock on the old file ?
								System.out.println("[thread replica server] acquiring a lock on : "+oldFile);
								oldFile.lock();
								System.out.println("[thread replica server] lock acquired on : "+oldFile);

								System.out.println("[thread replica server] comp:" + oldFile.isCompatibleWith(file) + " new:" + file.getGlobalVersion() + " old+1:" + (oldFile.getGlobalVersion() + 1));

								if (oldFile.isCompatibleWith(file) && (file.getGlobalVersion() >= (oldFile.getGlobalVersion() + 1)))
								{
									// not new, but no conflict
									// TODO verify conditions and locks
									// TODO unlock in commit
									FileManager.prepare(file); // store in temporary storage
									out.writeObject(UtilBobby.REPLICA_WRITE_OK);

									oldFile.unlock();
									System.out.println("[thread replica server] released the lock on : "+oldFile);
								} else {
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
										out.writeObject(UtilBobby.REPLICA_WRITE_OK);
									} else {
										// reject the file
										System.out.println("[thread replica server] I refuse the new file");
										out.writeObject(UtilBobby.REPLICA_WRITE_KO);
									}

									oldFile.unlock();
									System.out.println("[thread replica server] released the lock on : "+oldFile);
								}
							}
						} else {
							System.out.println("[thread replica server] metadata received "+file);
							// it's a meta-data file, no content
							// we just store the new value and say we're good
							file.setHasFile(false);
							FileManager.prepare(file); // store in temporary storage
							out.writeObject(UtilBobby.REPLICA_WRITE_OK);
						}
					} catch (Exception e) {
						System.out.println("[thread replica server] failed! " + e.getLocalizedMessage());
						out.writeObject(UtilBobby.REPLICA_WRITE_KO);
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
						out.writeObject(UtilBobby.ANSWER_TRUE);
					}else{
						out.writeObject(UtilBobby.ANSWER_FALSE);
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
						out.writeObject(UtilBobby.REPLICA_TRANSACTION_COMMITED);
					} else {
						System.out.println("[thread replica server] abort for " + cmd[3]);
						FileManager.abort(cmd[3]);
						out.writeObject(UtilBobby.REPLICA_TRANSACTION_ABORTED);
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
						if (FileManager.getFileOrMetadata(cmd[2]) != null){
							FileManager.removeFile(cmd[2]);
							System.out.println("[thread replica server] delete succeeded");
							out.writeObject(UtilBobby.REPLICA_DELETE_OK);
						}else{
							System.out.println("[thread replica server] delete failed: file not found.");
							out.writeObject(UtilBobby.REPLICA_DELETE_NOT_FOUND);
						}
					} catch (Exception e) {
						out.writeObject(UtilBobby.REPLICA_DELETE_KO);
					}
				}
/*
 *	----------------------------------------------------------------------------------------------------------------------
 *	READ
 */
				// send the file they are asking for
				if (cmd[1].equals(UtilBobby.REPLICA_READ_SYMBOL)) {
					
					System.out.println("[thread replica server] get file ready: " + cmd[2]);
					out.writeObject(FileManager.getFile(cmd[2]));
					System.out.println("[thread replica server] get file sent: " + cmd[2]);
				}
				
				
			}
		} catch (IOException e) {
			System.out.println("[thread replica server] failed, details: ");
			e.printStackTrace();
		}
		System.out.println("[thread replica server] end");
		System.out.println();
		close();
	}
}
