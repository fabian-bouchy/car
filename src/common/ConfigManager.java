package common;

import java.util.ArrayList;


import server.Replicat;

public class ConfigManager {

	
	private static final String DEFAULT_CONFIG_FILE_NAME = "config.json";
	
	private static ArrayList<Replicat> replicats;
	
	static {
		replicats = new ArrayList<Replicat>();
	}
	
	private ConfigManager(){}

	public static void init() {
		init(DEFAULT_CONFIG_FILE_NAME);
	}
	
	public static void init(String sConfigFile) {
				
	}
	

	
	
}
