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
import common.UtilPrinter;

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
		System.out.println("[ThreadReplicaServer] init for other replica "+clientSocket.getInetAddress());
	}
	
	@Override
	public void run() {
		System.out.println("[ThreadReplicaServer] run");
		try {
			String[] cmd = command.split(UtilBobby.SPLIT_REGEX);
			System.out.println("[ThreadReplicaServer] " + command + " (" + cmd.length + ")");
			
			if(cmd.length >= 2) {
				
				// Be sure that the message is for the replica thread!
				if(cmd[0].compareTo(UtilBobby.REPLICA) != 0){
					UtilPrinter.printlnError("[ThreadReplicaServer] wrong command");
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
							System.out.println("[ThreadReplicaServer] get metadata ready");

							// external meta-data
							HashMap<String, File> metadata = FileManager.getMetadata();
							// internal meta-data
							for (File file : FileManager.getFiles().values()){
								metadata.put(file.getId(), file.generateMetadata());
							}

							out.writeObject(metadata);

							if (((String)in.readObject()).equals(UtilBobby.REPLICA_METADATA_OK)){
								System.out.println("[ThreadReplicaServer] metadata sent!");
							}else{
								UtilPrinter.printlnError("[ThreadReplicaServer] metadata not sent!");
							}						
						}
					} catch (Exception e) {
						out.writeObject(UtilBobby.REPLICA_METADATA_KO);
						UtilPrinter.printlnError("[ThreadReplicaServer] metadata failed! " + e.getLocalizedMessage());
					}
				}

/*
 *	----------------------------------------------------------------------------------------------------------------------
 *	WRITE
 */

				// store the files/metadata we receive
				if(cmd[1].equals(UtilBobby.REPLICA_WRITE_SYMBOL)) {
					try {

						System.out.println("[ThreadReplicaServer] write");

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

						System.out.println("[ThreadReplicaServer] file received: "+file);

						if (file.isFile()) {
							System.out.println("[ThreadReplicaServer] is a file");
							// let's verify if there is a conflict
							File oldFile = FileManager.getFileOrMetadata(file.getId());

							if (oldFile == null) {
								// check if it's not already in the temporary storage waiting for a commit
								// new file
								System.out.println("[ThreadReplicaServer] new file");
								FileManager.prepare(file); // store in temporary storage
								out.writeObject(UtilBobby.REPLICA_WRITE_OK);
								System.out.println("[ThreadReplicaServer] file prepared");
							} else {
								System.out.println("[ThreadReplicaServer] update");
								// TODO acquire a lock on the old file ?
								//System.out.println("[ThreadReplicaServer] acquiring a lock on : "+oldFile);
								//oldFile.lock();
								//System.out.println("[ThreadReplicaServer] lock acquired on : "+oldFile);

								System.out.println("[ThreadReplicaServer] comp:" + oldFile.isCompatibleWith(file) + " new:" + file.getGlobalVersion() + " old+1:" + (oldFile.getGlobalVersion() + 1));

								if (oldFile.isCompatibleWith(file) && (file.getGlobalVersion() > oldFile.getGlobalVersion()))
								{
									// not new, but no conflict
									FileManager.prepare(file); // store in temporary storage
									out.writeObject(UtilBobby.REPLICA_WRITE_OK);

									//oldFile.unlock();
									//System.out.println("[ThreadReplicaServer] released the lock on : "+oldFile);
								} else {
									// there is a conflicted copy
									int myPriority = ConfigManager.getMe().getPriority();
									int theirPriority = Integer.MIN_VALUE;
									RemoteNode them = ConfigManager.getRemoteNodeByIp(clientSocket.getInetAddress().getHostAddress());
									if (them != null){
										theirPriority = them.getPriority();
									}

									System.out.println("[ThreadReplicaServer] my priority is " + myPriority + " theirs is " +theirPriority);

									if (myPriority > theirPriority){
										// TODO verify this is correct
										// oldFile.lock();
										if (FileManager.getFileOrMetadata(file.getId()).getGlobalVersion() < file.getGlobalVersion()){
											System.out.println("[ThreadReplicaServer] I obey and store the new file");
											FileManager.addOrReplaceFile(file);
										}else{
											System.out.println("[ThreadReplicaServer] I should obey, but they have the same version, so I ignore it");
										}
										out.writeObject(UtilBobby.REPLICA_WRITE_OK);
									} else {
										// reject the file
										System.out.println("[ThreadReplicaServer] I refuse the new file");
										out.writeObject(UtilBobby.REPLICA_WRITE_KO);
									}

									oldFile.unlock();
									System.out.println("[ThreadReplicaServer] released the lock on : "+oldFile);
								}
							}
						} else {
							System.out.println("[ThreadReplicaServer] metadata received "+file);
							// it's a meta-data file, no content
							// we just store the new value and say we're good
							file.setHasFile(false);
							FileManager.prepare(file); // store in temporary storage
							out.writeObject(UtilBobby.REPLICA_WRITE_OK);
						}
					} catch (Exception e) {
						System.out.println("[ThreadReplicaServer] failed! " + e.getLocalizedMessage());
						out.writeObject(UtilBobby.REPLICA_WRITE_KO);
					}
					
				}
				
/*
 *	---------------------------------------------------------------------------------------------------------------------- 
 *	HAS
 */
				
				// check if the file exists on this node
				if(cmd[1].equals(UtilBobby.REPLICA_HAS_SYMBOL)) {
					System.out.println("[ThreadReplicaServer] has");
					
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
						System.out.println("[ThreadReplicaServer] commit for " + cmd[3]);
						FileManager.commit(cmd[3]);
						out.writeObject(UtilBobby.REPLICA_TRANSACTION_COMMITED);
					} else {
						System.out.println("[ThreadReplicaServer] abort for " + cmd[3]);
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
						System.out.println("[ThreadReplicaServer] delete");
						if (FileManager.getFileOrMetadata(cmd[2]) != null){
							FileManager.removeFile(cmd[2]);
							System.out.println("[ThreadReplicaServer] delete succeeded");
							out.writeObject(UtilBobby.REPLICA_DELETE_OK);
						}else{
							System.out.println("[ThreadReplicaServer] delete failed: file not found.");
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
					
					System.out.println("[ThreadReplicaServer] get file ready: " + cmd[2]);
					out.writeObject(FileManager.getFile(cmd[2]));
					System.out.println("[ThreadReplicaServer] get file sent: " + cmd[2]);
				}
				
				
			}
		} catch (IOException e) {
			System.out.println("[ThreadReplicaServer] failed, details: ");
			e.printStackTrace();
		}
		System.out.println("[ThreadReplicaServer] end");
		System.out.println();
		close();
	}
}
