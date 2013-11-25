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
	}
	
	public static void replaceFile(File file){
		getMapFiles().put(file.getId(), file);
	}
}
