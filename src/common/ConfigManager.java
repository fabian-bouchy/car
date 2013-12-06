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

	private static ArrayList<String> serversPriority = new ArrayList<String>(N);

	private static HashMap<String, RemoteNode> sRemoteNodes;
	private static RemoteNode sMe;
	private static String sHostName;

	static {
		sRemoteNodes = new HashMap<String, RemoteNode>();
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
				sRemoteNodes.put(remoteNode.getName(), remoteNode);
			}
		}

		if (configType == ConfigType.SERVER && sMe == null) {
			throw new Exception(
					"[config manager] Error - could not establish who I am !");
		}

		if (configType == ConfigType.CLIENT && sRemoteNodes.size() == 0) {
			throw new Exception(
					"[config manager] Error - client could not find any servers");
		}

		System.out.println("[config manager] initialized with "
				+ sRemoteNodes.size() + " hosts");

		generatePriorityServers();
	}

	/**
	 * serversPriority contains all the servers in random order
	 */
	private static void generatePriorityServers() {

		for (RemoteNode node : getRemoteNodes().values()) {
			serversPriority.add(node.getName());
		}

		Collections.shuffle(serversPriority);

	}

	/**
	 * Get the instance of the current replica
	 * 
	 * @return The current instance of the replica
	 */
	public static RemoteNode getMe() {
		return sMe;
	}

	public static String getHostName() {
		return sHostName;
	}

	/**
	 * Get all RemoteNodes according to the configuration file
	 * 
	 * @return List of RemoteNode
	 */
	public static synchronized HashMap<String, RemoteNode> getRemoteNodes() {
		return sRemoteNodes;
	}

	public static HashMap<String, RemoteNode> getRemoteReplicas() {
		HashMap<String, RemoteNode> map = new HashMap<String, RemoteNode>();
		int k = K;
		for (int i = 0; i < k; i++) {
			String nameServer = serversPriority.get(i);
			RemoteNode server = getRemoteNodes().get(nameServer);

			if (server != null){
				map.put(nameServer, server);
			}else{
				k++;
			}

		}
		return map;
	}

	public static RemoteNode getOtherRemoteReplica(HashMap<String, RemoteNode> map) {
		if (map.size() >= getRemoteNodes().size()) {
			return null;
		} else {
			Iterator<RemoteNode> it = getRemoteNodes().values().iterator();

			while (it.hasNext()) {
				Iterator<RemoteNode> itMap = map.values().iterator();
				boolean found = false;
				RemoteNode node = (RemoteNode) it.next();

				while (itMap.hasNext() && !found) {

					RemoteNode nodeMap = (RemoteNode) itMap.next();
					if (node.equals(nodeMap))
						found = true;
				}
				if (!found)
					return node;
			}

		}
		return null;
	}

	public static synchronized int getN() {
		return N;
	}

	public static synchronized int getK() {
		return K;
	}

	public static synchronized RemoteNode getRemoteNode(String remoteNodeName) {
		return sRemoteNodes.get(remoteNodeName);
	}

}