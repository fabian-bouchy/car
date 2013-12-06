package common;

import java.io.FileInputStream;
import java.net.Inet4Address;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import server.RemoteReplica;
import client.RemoteServer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

public class ConfigManager {

	public enum ConfigType {
		SERVER, CLIENT
	}

	private static final String DEFAULT_CONFIG_FILE_NAME = "config.json";
	private static final int N = 3;
	private static final int K = 1;

	private static HashMap<String, RemoteNode> serversMap;
	private static ArrayList<RemoteNode> serversList;
	private static RemoteNode sMe;
	private static String sHostName;

	static {
		serversMap = new HashMap<String, RemoteNode>();
		serversList = new ArrayList<RemoteNode>(N);
		sMe = null;
	}

	private ConfigManager() {
	}

	/**
	 * @brief First method that need to be called
	 * 
	 *        Initialize the ConfigManager and the current replica with default
	 *        values.
	 * 
	 * @throws Exception
	 */
	public static void init(ConfigType configType) throws Exception {
		init(configType, DEFAULT_CONFIG_FILE_NAME, null);
	}

	public static void init(ConfigType configType, String sConfigFile)
			throws Exception {
		init(configType, sConfigFile, null);
	}

	/**
	 * @brief First method that need to be called
	 * 
	 *        Initialize the ConfigManager and the current replica.
	 * 
	 * @param sConfigFile
	 *            ConfigFile name
	 * @param sInterfaceName
	 *            Interface name
	 * @throws Exception
	 */
	public static void init(ConfigType configType, String sConfigFile, String hostname) throws Exception {

		String myIP = Inet4Address.getLocalHost().getHostAddress();

		String myHost = Inet4Address.getLocalHost().getHostName();
		sHostName = myHost;

		System.out.println("[config manager] init on " + myHost + " (" + myIP + ")");

		// prepare JSON
		JSONTokener jsonTokener = new JSONTokener(new FileInputStream(sConfigFile));
		JSONObject jsonFile = new JSONObject(jsonTokener);
		JSONArray jsonArrayReplicas = jsonFile.getJSONArray("replicas");

		// Extract data and create local configuration
		for (int i = 0; i < jsonArrayReplicas.length(); i++) {
			JSONObject jsonObjReplica = jsonArrayReplicas.getJSONObject(i);

			// Read data form json object
			String sName = jsonObjReplica.getString("name");
			String sIp = jsonObjReplica.getString("ip");
			int iPort = jsonObjReplica.getInt("port");
			String sInterface = jsonObjReplica.getString("interface");

			// Create replica instance
			RemoteNode remoteNode;
			switch (configType) {
			case SERVER:
				remoteNode = new RemoteReplica(sName, sIp, sInterface, i, iPort);
				break;
			case CLIENT:
				remoteNode = new RemoteServer(sName, sIp, sInterface, i, iPort);
				break;
			default:
				remoteNode = null;
				break;
			}

			if (hostname == null && (sIp.equals(myIP) || sName.equals(myHost))) {
				// when desired hostname not specified, we look by IP
				sMe = remoteNode;
			} else if (hostname != null && hostname.equals(sName)) {
				// otherwise, we just pick the one with the desired hostname
				sMe = remoteNode;
			} else {
				// store in a hash (to be gotten by name) and a list (for random order)
				serversMap.put(remoteNode.getName(), remoteNode);
				serversList.add(remoteNode);
			}
		}

		if (configType == ConfigType.SERVER && sMe == null) {
			throw new Exception(
					"[config manager] Error - could not establish who I am !");
		}

		if (configType == ConfigType.CLIENT && serversMap.size() == 0) {
			throw new Exception(
					"[config manager] Error - client could not find any servers");
		}

		System.out.println("[config manager] initialized with " + serversMap.size() + " hosts");
	}
	
	public static RemoteNode getMe() {
		return sMe;
	}

	public static String getHostName() {
		return sHostName;
	}
	
	public static synchronized int getN() {
		return N;
	}

	public static synchronized int getK() {
		return K;
	}

	public static synchronized RemoteNode getRemoteNode(String remoteNodeName) {
		return serversMap.get(remoteNodeName);
	}
	
	public static synchronized HashMap<String, RemoteNode> getRemoteNodes() {
		return serversMap;
	}
	
	public static synchronized ArrayList<RemoteNode> getRemoteNodesList() {
		return serversList;
	}
}