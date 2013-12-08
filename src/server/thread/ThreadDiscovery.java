package server.thread;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import server.RemoteReplica;

import common.ConfigManager;
import common.File;
import common.FileManager;
import common.RemoteNode;

public class ThreadDiscovery implements Runnable {
	
	public ThreadDiscovery() {
		System.out.println("[ThreadDiscovery] init");
	}

	@Override
	public void run() {
		System.out.println("[ThreadDiscovery] run");
		
		int K = ConfigManager.getK();
		
		HashMap<File, Integer> files = new HashMap<>();
		HashMap<File, Integer> metadata = new HashMap<>();
		HashMap<File, RemoteReplica> where = new HashMap<>();
		
		
		// discover files on the network
		for(RemoteNode server : ConfigManager.getRemoteNodesList()){
			
			RemoteReplica rserver = (RemoteReplica) server;
			System.out.println("[ThreadDiscovery] Querying " + rserver + " ... ");
			try{
				
				HashMap<String, File> remoteFiles = rserver.getMetadata();
				for(File file : remoteFiles.values()){
					
					// store the number of occurences
					if (file.hasFile()){
						Integer occurences;
						occurences = files.get(file);
						if (occurences == null){
							occurences = new Integer(1);
						}else{
							occurences = new Integer(occurences.intValue() + 1);
						}
						files.put(file, occurences);
						
						// store the server where the file belongs
						where.put(file, rserver);
					}else{
						file.setHasFile(false);
						metadata.put(file, new Integer(1));
					}
				}
				System.out.println("[ThreadDiscovery] " + files.size() + " files processed");
			}catch(Exception e){
				// server doesn't respond, try another one
				System.out.println("[ThreadDiscovery] Server unavailable");
			}
		}
		
		System.out.println("[ThreadDiscovery] " + files.size() + " files to check");
		
		// download the files which don't have K+1 occurences
		for(Entry<File, Integer> entry: files.entrySet()){
			
			// if there are not enough copies
			if (entry.getValue().intValue() < K + 1){
				RemoteReplica server = where.get(entry.getKey());
				
				System.out.print("[ThreadDiscovery] Downloading " + entry.getKey() + " from " + server);
				
				try {
					File realFile = server.read(entry.getKey());
					
					if (realFile != null && FileManager.getFile(realFile.getId()) == null){
						FileManager.addOrReplaceFile(realFile);
						// if any, remove from metadata
						metadata.remove(entry.getKey());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				// if there are enough copies, we want to store the meta-data
				entry.getKey().setHasFile(false);
				metadata.put(entry.getKey(), new Integer(1));
			}
		}
		
		// add meta-data
		System.out.println("[ThreadDiscovery] " + metadata.size() + " meta-data to add");
		
		for(Entry<File, Integer> entry: metadata.entrySet()){
			System.out.print("[ThreadDiscovery] Adding meta-data " + entry.getKey());
			FileManager.addOrReplaceFile(entry.getKey());
		}
	}
}
