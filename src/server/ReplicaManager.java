package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import common.ConfigManager;
import common.File;
import common.RemoteNode;

public class ReplicaManager {
	
	private ArrayList<RemoteNode> replicas;
	
	public ReplicaManager(){
		replicas = new ArrayList<RemoteNode>(ConfigManager.getRemoteNodes());
	}

	public void replicate(File file){
		// TODO need to be change
		for (int i = 0; i < replicas.size(); i++) {
			RemoteReplica remoteReplica = (RemoteReplica)replicas.get(i);
			try {
				remoteReplica.write(file);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void delete(File file){
		// TODO need to be change
		for (int i = 0; i < replicas.size(); i++) {
			RemoteReplica remoteReplica = (RemoteReplica)replicas.get(i);
			try {
				remoteReplica.delete(file);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
