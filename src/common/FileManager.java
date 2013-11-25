package common;

import java.util.HashMap;
import java.util.Map;

public class FileManager {
	
	private static Map<String, File> MapFiles;

	private FileManager(){
		Map<String, File> mapFich = new HashMap<String, File>();
		setMapFiles(mapFich);
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
	
	public static void addFile(String id, File file){
		getMapFiles().put(id, file);
	}
	
	public static void replaceFile(String id, File file){
		getMapFiles().put(id, file);
	}
}
