package common;

import java.util.HashMap;
import java.util.Map;

public class FileManager {

	private static HashMap<String, File> files = new HashMap<String, File>();
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
		System.out.println("[FileManager getFile] Thread id: " + Thread.currentThread().getId());
		System.out.println("[FileManager getFile] Looking for " + id);
		return getFiles().get(id);
	}
	
	public static synchronized void addFile(File file){
		System.out.println("[FileManager addFile] Thread id: " + Thread.currentThread().getId());
		getFiles().put(file.getId(), file);
		addOrUpdateMetadata(file);
		System.out.println(represent());
	}
	
	public static synchronized void replaceFile(File file){
		System.out.println("[FileManager replaceFile] Thread id: " + Thread.currentThread().getId());
		getFiles().put(file.getId(), file);
		addOrUpdateMetadata(file);
		System.out.println(represent());
	}
	
	public static synchronized void removeFile(String id){
		getFiles().remove(id);
		System.out.println(represent());
	}
	
	private static synchronized void addOrUpdateMetadata(File file) {
		metadata.put(file.getId(),file);
	}
	
	public static synchronized void mergeMetadata(HashMap<String, File> others) {
		for (File file : others.values()) {
			addOrUpdateMetadata(file);
		}
	}

	public static synchronized String represent(){
		System.out.println("[FileManager represent] Thread id: " + Thread.currentThread().getId());
		if(FileManager.files == null)
			return "List of file is empty";
		
		String out = System.getProperty("line.separator") + "Current file configuration:" + System.getProperty("line.separator");
		for (Map.Entry<String, File> entry : FileManager.files.entrySet()){
			out += entry.getKey() + " => " + entry.getValue().toString() + System.getProperty("line.separator");
		}
		return out;
	}
	
	public static synchronized String representMetadata(){
		System.out.println("[FileManager representMetadata] Thread id: " + Thread.currentThread().getId());
		if(FileManager.metadata == null)
			return "List of metadata is empty";
		
		String out = System.getProperty("line.separator") + "Current file configuration:" + System.getProperty("line.separator");
		for (Map.Entry<String, File> entry : FileManager.metadata.entrySet()){
			out += entry.getKey() + " => " + entry.getValue().toString() + System.getProperty("line.separator");
		}
		return out;
	}
}
