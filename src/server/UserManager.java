package server;

import common.CypherManager;

/**
 * Abstract the management of users.
 * Store some useful information about the current user: username, cypherManager etc..
 */
public class UserManager {
	
	private UserManager(){}

	private static String username;

	private static CypherManager cypherManager = null;

	private static boolean cypherEnabled = false;

	/**
	 * Init the UserManager. If passPhrase != null => enable crypto
	 * @param name UserName
	 * @param passPhrase PassPhrase if crypto is wanted
	 */
	public static void init(String name, String passPhrase) throws Exception {
		username = name;
		if(passPhrase != null) {
			System.out.println("[UserManager] Encryption enabled!");
			cypherEnabled = true;
			cypherManager = new CypherManager(passPhrase, "this is a very lovely salt, my dear!");
		}
	}

	public static String getUsername() {
		return username;
	}

	public static CypherManager getCypherManager() {
		return cypherManager;
	}

	public static boolean isCypherEnabled() {
		return cypherEnabled;
	}
}
