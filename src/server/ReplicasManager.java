package server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import common.ConfigManager;
import common.File;

public class ReplicasManager {
	
	private ArrayList<Replica> replicas;
	
	public ReplicasManager(){
		replicas = new ArrayList<Replica>(ConfigManager.getReplicas());
	}

	public void replicate(File file){
		for (Replica replica : replicas) {
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
