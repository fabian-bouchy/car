package common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import server.Replicat;

public class ConfigManager {

	
	private static final String DEFAULT_CONFIG_FILE_NAME = "config.json";
	
	private static ArrayList<Replicat> replicats;
	
	static {
		replicats = new ArrayList<Replicat>();
	}
	
	private ConfigManager(){}

	public static void init() throws FileNotFoundException {
		init(DEFAULT_CONFIG_FILE_NAME);
	}
	
	public static void init(String sConfigFile) throws JSONException, FileNotFoundException{
		// Open file as tokener
		JSONTokener jsonTokener = new JSONTokener(new FileInputStream(sConfigFile));
		// Get main array
		JSONObject jsonFile = new JSONObject(jsonTokener);
		// Get the list of replicas
		JSONArray jsonArrayReplicas = jsonFile.getJSONArray("replicas");
		// Extract data and create local configuration
		for (int i = 0; i < jsonArrayReplicas.length(); i++) {
			JSONObject jsonObjReplica = jsonArrayReplicas.getJSONObject(i);
			String name = jsonObjReplica.getString("name");
			String ip 	= jsonObjReplica.getString("ip");
			String port	= jsonObjReplica.getString("port");
			System.out.println(name + " : " + ip + " : " + port);
		}
	}
}
