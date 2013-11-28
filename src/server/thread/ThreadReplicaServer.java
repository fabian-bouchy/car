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

				// store the file we receive
				if(cmd[1].equals(UtilBobby.REPLICA_WRITE_SYMBOL)) {
					
					System.out.println("[thread replica server] write");
					
					// send a "ready" message
					out.println(UtilBobby.REPLICA_WRITE_READY);
					
					// receive the file
					ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
					File file = (File) reader.readObject();
					System.out.println("File received: " + file);
					
					
					// TODO change later - storing the file on hdd
					file.writeToFile(file.getId());

					// TODO check support of FileManager
					// Persist object in memory
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
				
				// delete the file
				if(cmd[1].equals(UtilBobby.REPLICA_DELETE_SYMBOL)){
					System.out.println("[thread replica server] delete");
					
					if (FileManager.getFile(cmd[2]) != null){
						FileManager.removeFile(cmd[2]);
						System.out.println("[thread replica server] delete succeed");
						out.println(UtilBobby.REPLICA_DELETE_OK);
					}else{
						System.out.println("[thread replica server] delete failed: file not found.");
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
