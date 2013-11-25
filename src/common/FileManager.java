package common;

import java.util.HashMap;
import java.util.Map;

public class FileManager {
	
	private static Map<String, File> MapFichiers;

	private FileManager(){
		Map<String, File> mapFich = new HashMap<String, File>();
		setMapFichiers(mapFich);
	}
	
	public static Map<String, File> getMapFichiers() {
		return MapFichiers;
	}

	public static void setMapFichiers(Map<String, File> mapFichiers) {
		MapFichiers = mapFichiers;
	}
	
	public static File getFichier(String id){
		return MapFichiers.get(id);
	}
	
	public static void addFichier(String id, File fichier){
		getMapFichiers().put(id, fichier);
	}
	
	public static void replaceFichier(String id, File fichier){
		getMapFichiers().put(id, fichier);
		
	}

}
