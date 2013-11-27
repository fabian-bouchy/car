package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import common.ConfigManager;
import common.File;
import common.RemoteNode;

public class ReplicasManager {
	
	private ArrayList<RemoteNode> replicas;
	
	public ReplicasManager(){
		replicas = new ArrayList<RemoteNode>(ConfigManager.getRemoteNodes());
	}

	public void replicate(File file){
		// TODO need to be change
		for (int i = 0; i < replicas.size(); i++) {
			Replica replica = (Replica)replicas.get(i);
			try {
				replica.write(file);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
