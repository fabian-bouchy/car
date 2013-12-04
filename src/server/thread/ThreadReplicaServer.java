package server.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import common.File;
import common.FileManager;
import common.UtilBobby;

public class ThreadReplicaServer extends ThreadWorker{

	private String command;
	
	public ThreadReplicaServer(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in, String command){
		super(serverSocket, clientSocket, out, in);
		this.command = command;
		System.out.println("[thread replica server] init");
	}
	
	@Override
	public void run() {
		System.out.println("[thread replica server] run");
		try {
			String[] cmd = command.split(UtilBobby.SPLIT_REGEX);
			
			/**
			 * 0 : should be replica UtilBobby.REPLICA
			 * 1 : action - write/has/delete
			 * [2 : argument - id]
			 */
			System.out.println("[thread replica server] " + command + " [" + cmd.length + "]");
			if(cmd.length >= 2) {
				// Be sure that the message is for the replica thread!
				if(cmd[0].compareTo(UtilBobby.REPLICA) != 0)
					return;
				
				// check if the file exists on this node
				if(cmd[1].equals(UtilBobby.REPLICA_METADATA_SYMBOL)) {
					System.out.println("[thread replica server] get metadata");
					
					out.println(UtilBobby.REPLICA_METADATA_READY);
					System.out.println("[thread replica server] metadata ready send");
					ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
		        	outStream.writeObject(FileManager.getMetadata());
					// FIXME Sometimes block here
		        	if(in.readLine().equals(UtilBobby.REPLICA_METADATA_OK))
		        		System.out.println("[thread replica server] metadata send!");
		        	else
		        		System.out.println("[thread replica server] metadata not send!");
				}
				
				// store the file we receive
				if(cmd[1].equals(UtilBobby.REPLICA_WRITE_SYMBOL)) {
					
					System.out.println("[thread replica server] write");
					
					// send a "ready" message
					out.println(UtilBobby.REPLICA_WRITE_READY);
					
					// receive the file
					ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
					File file = (File) reader.readObject();
					System.out.println("File received: " + file);
					
					File oldFile = FileManager.getFile(file.getId());
					// Update issue?
					if(oldFile == null) {
						// TODO change later - storing the file on hdd
						// file.writeToFile(file.getId());
					} else if(oldFile != null && oldFile.getGlobalVersion() == file.getGlobalVersion() && !file.equals(oldFile)) {
						// TODO Manage Conflict on update!!!!
					}
					
					// TODO check support of FileManager
					// Add file in temp state
					FileManager.addFile(file);
					out.println(UtilBobby.REPLICA_WRITE_OK);
				}
				
				// check if the file exists on this node
				if(cmd[1].equals(UtilBobby.REPLICA_HAS_SYMBOL)) {
					System.out.println("[thread replica server] has");
					
					if (FileManager.getFile(cmd[2]) != null){
						out.println(UtilBobby.ANSWER_TRUE);
					}else{
						out.println(UtilBobby.ANSWER_FALSE);
					}
				}

				// commit file
				if(cmd[1].equals(UtilBobby.REPLICA_TRANSACTION_SYMBOL)){
					System.out.println("[thread replica server] commit");

					if(command.contains(UtilBobby.REPLICA_TRANSACTION_COMMIT)) {
						System.out.println("[thread replica server] commit for " + cmd[2]);
						FileManager.commit(cmd[3]);
						out.println(UtilBobby.REPLICA_TRANSACTION_COMMITED);
					} else {
						System.out.println("[thread replica server] abort");
						FileManager.abord(cmd[3]);
						out.println(UtilBobby.REPLICA_TRANSACTION_ABORTED);
					}
				}

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
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
