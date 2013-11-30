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
		return files.get(id);
	}
	
	public static synchronized void addFile(File file){
		files.put(file.getId(), file);
		addOrUpdateMetadata(file);
		System.out.println(represent());
	}
	
	public static synchronized void replaceFile(File file){
		files.put(file.getId(), file);
		addOrUpdateMetadata(file);
		System.out.println(represent());
	}
	
	public static synchronized void removeFile(String id){
		files.remove(id);
		metadata.remove(id);
		System.out.println(represent());
	}
	
	private static synchronized void addOrUpdateMetadata(File file) {
		File metadataTmp = new File(file);
		metadata.put(metadataTmp.getId(),metadataTmp);
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
