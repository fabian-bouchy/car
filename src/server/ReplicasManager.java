package server;

import java.util.ArrayList;

import common.ConfigManager;
import common.File;

public class ReplicasManager {
	
	private ArrayList<Replica> replicas;
	
	public ReplicasManager(){
		replicas = new ArrayList<Replica>(ConfigManager.getReplicas());
	}
	
	public void replicate(File file){
		
	}
}
