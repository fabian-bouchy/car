package server.thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import common.File;
import common.FileManager;
import common.UtilBobby;

public class ThreadListFiles extends ThreadWorker{
	private String username;

	public ThreadListFiles(ServerSocket serverSocket, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in, String command){
		super(serverSocket, clientSocket, out, in);
		System.out.println("[ThreadListFiles] init");
		this.username = command.split(UtilBobby.SPLIT_REGEX)[2];
	}

	@Override
	public void run() {
		System.out.println("[ThreadListFiles] running...");
		try {
			// send to client that the server is ready
			out.writeObject(UtilBobby.SERVER_LIST_READY);
			// Building metadata associate to username:
			HashMap<String, File> userMetadata = FileManager.getMetadataByUsername(username);

			// Send metadata
			out.writeObject(userMetadata);
		} catch (IOException e )  {
			e.printStackTrace();
		}
	}
}
