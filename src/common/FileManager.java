package common;

import java.util.HashMap;
import java.util.Map;

public class FileManager {

	// Files contain in memory
	private static HashMap<String, File> files = new HashMap<String, File>();

	// Files waiting commit
	private static HashMap<String, File> tmpFiles = new HashMap<String, File>();

	// Metadata of network
	private static HashMap<String, File> metadata = new HashMap<String, File>();

	static {
	}
	
	private FileManager(){}
	
	public static synchronized HashMap<String, File> getMetadata() {
		return metadata;
	}

	public static synchronized HashMap<String, File> getFiles() {
		return files;
	}

	public static synchronized void setMapFiles(HashMap<String, File> mapFiles) {
		files = mapFiles;
	}
	
	public static synchronized void setMetadata(HashMap<String, File> otherMetadata) {
		metadata = otherMetadata;
	}
	
	public static synchronized File getFile(String id){
		return files.get(id);
	}

	public static synchronized File getMetadata(String id){
		return metadata.get(id);
	}

	public static synchronized void commit(String fileId) {
		File file = tmpFiles.get(fileId);
		if(file != null) {
			System.out.println("[FileManager] commit for " + file);
			tmpFiles.remove(fileId);
			files.put(file.getId(), file);
			addOrUpdateMetadata(file.getMetadata());
			System.out.println(represent());
		} else {
			System.out.println("[FileManager] nothing to commit for " + fileId);
		}
	}

	public static synchronized void abord(String fileId) {
		tmpFiles.remove(fileId);
	}

	public static synchronized void addFile(File file){
		tmpFiles.put(file.getId(), file);
	}

	public static synchronized void replaceFile(File file){
		tmpFiles.put(file.getId(), file);
	}

	public static synchronized void removeFile(String id){
		files.remove(id);
		metadata.remove(id);
		System.out.println(represent());
	}
	
	private static synchronized void addOrUpdateMetadata(File file) {
		System.out.println("[FileManager] add or update metadata for " + file);
		File metadataTmp = new File(file);
		metadata.put(metadataTmp.getId(),metadataTmp);
		System.out.println(representMetadata());
	}
	
	public static synchronized void mergeMetadata(HashMap<String, File> others) {
		for (File file : others.values()) {
			addOrUpdateMetadata(file);
		}
	}

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
