package common;

import java.util.HashMap;
import java.util.Map;

public class FileManager {
	
	// Files contain in memory
	private static HashMap<String, File> files = new HashMap<String, File>();
	// Files waiting commit
	private static HashMap<String, File> tmpFiles = new HashMap<String, File>();
	// Meta-data of network
	private static HashMap<String, File> metadata = new HashMap<String, File>();

	static {
	}
	
	private FileManager(){}
	
	
	// GETTERS
	public static synchronized HashMap<String, File> getMetadata() {
		return metadata;
	}

	public static synchronized HashMap<String, File> getFiles() {
		return files;
	}
	
	public static synchronized HashMap<String, File> getMetadataByUsername(String username) {
		HashMap<String, File> tmp = new HashMap<String, File>();
		for (File file : metadata.values()) {
			if(file.getId().startsWith(username + UtilBobby.SPLIT_REGEX)) {
				tmp.put(file.getId(), file);
			}
		}
		for (File file : files.values()) {
			if(file.getId().startsWith(username + UtilBobby.SPLIT_REGEX)) {
				tmp.put(file.getId(), file);
			}
		}
		return tmp;
	}

	public static synchronized File getFileOrMetadata(String id){
		if (files.get(id) != null){
			return files.get(id);
		}else{
			return metadata.get(id);
		}
	}
	
	public static synchronized File getTempFile(String id){
		return tmpFiles.get(id);
	}
	
	public static synchronized File getFile(String id){
		return files.get(id);
	}

	public static synchronized File getMetadata(String id){
		return metadata.get(id);
	}
	
	// ADD AND REMOVE
	public static synchronized void addOrReplaceFile(File file){
		if (file.isFile()){
			files.put(file.getId(), file);
			System.out.println(represent());
		} else {
			metadata.put(file.getId(), file);
			System.out.println(representMetadata());
		}
	}

	public static synchronized void removeFile(String id){
		files.remove(id);
		metadata.remove(id);
		System.out.println(represent());
	}

	// TRANSACTIONS
	public static synchronized void prepare(File file){
		tmpFiles.put(file.getId(), file);
	}

	public static synchronized void commit(String fileId) {
		File file = tmpFiles.get(fileId);
		if(file != null) {
//			try {
//				file.unlock();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			System.out.println("[FileManager] commit for " + file);
			tmpFiles.remove(fileId);
			addOrReplaceFile(file);
		} else {
			System.out.println("[FileManager] nothing to commit for " + fileId);
		}
	}

	public static synchronized void abort(String fileId) {
		tmpFiles.remove(fileId);
	}
	
	
	// DISPLAYING
	public static synchronized String represent(){
		if(FileManager.files.isEmpty())
			return "List of file is empty";
		
		String out = System.getProperty("line.separator") + "Current file configuration:" + System.getProperty("line.separator");
		for (Map.Entry<String, File> entry : FileManager.files.entrySet()){
			out += entry.getKey() + " => " + entry.getValue().toString() + System.getProperty("line.separator");
		}
		return out;
	}
	
	public static synchronized String representMetadata(){
		if(FileManager.metadata.isEmpty())
			return "List of metadata is empty";
		
		String out = System.getProperty("line.separator") + "Current metadata configuration:" + System.getProperty("line.separator");
		for (Map.Entry<String, File> entry : FileManager.metadata.entrySet()){
			out += entry.getKey() + " => " + entry.getValue().toString() + System.getProperty("line.separator");
		}
		return out;
	}
}
