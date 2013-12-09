package common;

import java.util.HashMap;
import java.util.Map;

/**
 * Manage and store files and metadata in memory.
 * All provide temp storage to commit or abort file transaction.
 */
public class FileManager {
	
	// Files contain in memory
	private static HashMap<String, File> files = new HashMap<String, File>();
	// Files waiting commit
	private static HashMap<String, File> tmpFiles = new HashMap<String, File>();
	// Meta-data of network
	private static HashMap<String, File> metadata = new HashMap<String, File>();

	private FileManager(){}
	
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
		tmpFiles.put(file.getId()+UtilBobby.SPLIT_FILE+file.getVersionString(), file);
	}

	public static synchronized void commit(File file) {
		File fileTmp = tmpFiles.get(file.getId()+UtilBobby.SPLIT_FILE+file.getVersionString());
		if(fileTmp != null) {
//			try {
//				file.unlock();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			System.out.println("[FileManager] commit for " + fileTmp);
			tmpFiles.remove(file.getId()+UtilBobby.SPLIT_FILE+file.getVersionString());
			addOrReplaceFile(fileTmp);
		} else {
			System.out.println("[FileManager] nothing to commit for " + file.getId()+UtilBobby.SPLIT_FILE+file.getVersionString());
		}
	}

	public static synchronized void abort(File file) {
		tmpFiles.remove(file.getId()+UtilBobby.SPLIT_FILE+file.getVersionString());
	}

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
			if(file.getId().startsWith(username + UtilBobby.SPLIT_FILE)) {
				tmp.put(file.getId(), file);
			}
		}
		for (File file : files.values()) {
			if(file.getId().startsWith(username + UtilBobby.SPLIT_FILE)) {
				tmp.put(file.getId(), file.generateMetadata());
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
