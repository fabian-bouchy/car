package common;

import java.util.ArrayList;


import server.Replica;

public class ConfigManager {

	
	private static final String DEFAULT_CONFIG_FILE_NAME = "config.json";
	
	private static ArrayList<Replica> replicas;
	
	static {
		replicas = new ArrayList<Replica>();
	}
	
	private ConfigManager(){}

	public static void init() {
		init(DEFAULT_CONFIG_FILE_NAME);
	}
	
	public static void init(String sConfigFile) {
				
	}
	

	
	
}
