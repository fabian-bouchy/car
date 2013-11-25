package common;

import java.io.FileInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


import server.Replica;

public class ConfigManager {
	
	private static final String DEFAULT_CONFIG_FILE_NAME = "config.json";
	private static final String DEFAULT_INTERFACE_NAME   = "eth1";
	private static final int 	N  = 3;
	
	private static ArrayList<Replica> sReplicas;
	private static Replica sMe;
	
	static {
		sReplicas = new ArrayList<Replica>();
	}

	private ConfigManager(){}

	/**
	 * @brief First method that need to be called
	 *
	 * Initialize the ConfigManager and the current replica with default values.
	 *
	 * @throws Exception
	 */
	public static void init() throws Exception {
		init(DEFAULT_CONFIG_FILE_NAME, DEFAULT_INTERFACE_NAME);
	}

	/**
	 * @brief First method that need to be called
	 *
	 * Initialize the ConfigManager and the current replica.
	 *
	 * @param sConfigFile		ConfigFile name
	 * @param sInterfaceName	Interface name
	 * @throws Exception
	 */
	public static void init(String sConfigFile, String sInterfaceName) throws Exception{
		// Open file as tokener
		JSONTokener jsonTokener = new JSONTokener(new FileInputStream(sConfigFile));

		// Get main array
		JSONObject jsonFile = new JSONObject(jsonTokener);

		// Get the list of replicas
		JSONArray jsonArrayReplicas = jsonFile.getJSONArray("replicas");

		// Get local host address to identify the current replica
		String currentReplicaIP = getInet4Address(sInterfaceName);
		if(currentReplicaIP == null)
			throw new Exception("Current ip address is not found. Verify network interface.");

		// Extract data and create local configuration
		for (int i = 0; i < jsonArrayReplicas.length(); i++) {
			JSONObject jsonObjReplica = jsonArrayReplicas.getJSONObject(i);

			// Read data form json object
			String 	sName 		= jsonObjReplica.getString("name");
			String 	sIp 		= jsonObjReplica.getString("ip");
			int 	iPort		= jsonObjReplica.getInt("port");
			String	sInterface 	= jsonObjReplica.getString("interface");

			// Create replica instance
			Replica tmpReplica = new Replica(sName, sIp, sInterface, i, iPort);
			if(sIp.compareTo(currentReplicaIP) == 0)
				sMe = tmpReplica;
			else
				sReplicas.add(tmpReplica);
		}
	}

	/**
	 * Use to retreive the ip address of the current replica
	 * @param sInterfaceName Network interface for which we are looking for
	 * @return The ip address or null instead
	 */
	private static String getInet4Address(String sInterfaceName) {
		try {
			// Go through all network interfaces
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while(networkInterfaces.hasMoreElements()) {
				NetworkInterface ni = networkInterfaces.nextElement();
				if(ni.getDisplayName().compareTo(sInterfaceName) == 0) {
					Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
					// Go through all ip address identify, find the ipv4 one
	                while(inetAddresses.hasMoreElements()) {
	                    InetAddress i= (InetAddress) inetAddresses.nextElement();
	                    if(i instanceof Inet4Address)
	                    	return i.getHostAddress();
	                }
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the intance of the current replica
	 * @return The current instance of the replica
	 */
	public static Replica getMe() {
		return sMe;
	}

	/**
	 * Get all replica according to the configuration file
	 * @return List of replicas
	 */
	public static synchronized ArrayList<Replica> getReplicas() {
		return sReplicas;
	}
	
	
	public static synchronized int getN() {
		return N;
	}

}