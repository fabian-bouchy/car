package common;

import java.util.HashMap;
import java.util.Map;

public class FileManager {

	private static Map<String, File> MapFiles;

	static {
		Map<String, File> mapFich = new HashMap<String, File>();
		setMapFiles(mapFich);
	}

	private FileManager(){}
	
	public static Map<String, File> getMetadata() {
		HashMap<String, File> metadata = new HashMap<String, File>(MapFiles);
		for (File file : metadata.values()) {
			file.setData(null);
			file.setSize(0);
			metadata.put(file.getId(), file);
		}
		return metadata;
	}

	public static Map<String, File> getMapFiles() {
		return MapFiles;
	}

	public static void setMapFiles(Map<String, File> mapFiles) {
		MapFiles = mapFiles;
	}
	
	public static File getFile(String id){
		return MapFiles.get(id);
	}
	
	public static void addFile(File file){
		getMapFiles().put(file.getId(), file);
		System.out.println(represent());
	}
	
	public static void replaceFile(File file){
		getMapFiles().put(file.getId(), file);
		System.out.println(represent());
	}
	
	public static void removeFile(String id){
		MapFiles.remove(id);
		System.out.println(represent());
	}
	
	public static String represent(){
		String out = System.getProperty("line.separator") + "Current file configuration:" + System.getProperty("line.separator");
		for (Map.Entry<String, File> entry : MapFiles.entrySet()){
			out += entry.getKey() + " => " + entry.getValue().toString() + System.getProperty("line.separator");
		}
		return out;
	}
}
