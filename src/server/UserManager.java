package server;

import common.CypherManager;

/**
 * Abstract the management of users.
 * Store some useful information about the current user: username, cypherManager etc..
 */
public class UserManager {
	
	private UserManager(){}

	private static String username;

	private static CypherManager cypherManager;

	public static void init(String name, String passPhrase) throws Exception {
		username = name;
		cypherManager = new CypherManager(passPhrase, "this is a very lovely salt, my dear!");
	}

	public static String getUsername() {
		return username;
	}

	public static CypherManager getCypherManager() {
		return cypherManager;
	}
}
