package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import common.ConfigManager;
import common.File;
import common.RemoteNode;

public class ReplicaManager {
	
	private HashMap<String, RemoteNode> replicas;
	
	public ReplicaManager(){
		replicas = new HashMap<String, RemoteNode>(ConfigManager.getRemoteNodes());
	}

	public void replicate(File file){
		// TODO need to be change
		for (RemoteNode remoteReplica : replicas.values()) {
			try {
				remoteReplica.write(file);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void delete(File file){
		// TODO need to be change
		for (RemoteNode remoteReplica : replicas.values()) {
			try {
				remoteReplica.delete(file);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public RemoteNode has(File file){
		// TODO need to be change
		for (RemoteNode remoteReplica : replicas.values()) {
			try {
				if(remoteReplica.has(file))
					return remoteReplica;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
