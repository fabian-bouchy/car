package server;

import common.CipherManager;

/**
 * Abstract the managment of users.
 * Store some useful information about the current user: username, cypherManager etc..
 */
public class UserManager {
	
	private UserManager(){}

	private static String username;

	private static CipherManager cypherManager;

	public static void init(String name, String passPhrase) throws Exception {
		username = name;
		cypherManager = new CipherManager(passPhrase, "this is a very lovely salt, my dear!");
	}

	public static String getUsername() {
		return username;
	}

	public static CipherManager getCypherManager() {
		return cypherManager;
	}
}
