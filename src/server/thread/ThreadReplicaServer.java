package server.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.rmi.CORBA.Util;

import common.File;
import common.FileManager;
import common.UtilBobby;

public class ThreadReplicaServer implements Runnable{

	private Socket clientSocket;
	private ServerSocket serverSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	public ThreadReplicaServer(ServerSocket serverSocket, Socket clientSocket, PrintWriter out, BufferedReader in){
		this.serverSocket = serverSocket;
		this.clientSocket = clientSocket;
		this.out = out;
		this.in = in;
		
		System.out.println("[thread replica server] init");
	}
	
	@Override
	public void run() {
		System.out.println("[thread replica server] run");
		try {
			String input = in.readLine();
			String[] cmd = input.split(UtilBobby.SPLIT_REGEX);
			
			/**
			 * 0 : should be replica UtilBobby.REPLICA
			 * 1 : action - write/has/delete
			 * [2 : argument - id]
			 */
			if(cmd.length >= 2) {
				// Be sure that the message is for the replica thread!
				if(cmd[0].compareTo(UtilBobby.REPLICA) != 0)
					return;

				// store the file we receive
				if(cmd[1].equals(UtilBobby.REPLICA_WRITE_SYMBOL)) {
					
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
					if (FileManager.getFile(cmd[2]) != null){
						out.println(UtilBobby.ANSWER_TRUE);
					}else{
						out.println(UtilBobby.ANSWER_FALSE);
					}
				}
				
				// delete the file
				if(cmd[1].equals(UtilBobby.REPLICA_DELETE_SYMBOL)){
					if (FileManager.getFile(cmd[2]) != null){
						FileManager.removeFile(cmd[2]);
						out.println(UtilBobby.REPLICA_DELETE_OK);
					}else{
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
