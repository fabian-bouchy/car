package server;

import common.CypherManager;


public class UserManager {
	
	private UserManager(){}

	private static String username;

	private static CypherManager cypherManager;

	public static void init(String name, String passPhrase) throws Exception {
		username = name;
		cypherManager = new CypherManager(passPhrase, String.valueOf(Math.random()));
	}

	public static String getUsername() {
		return username;
	}

	public static CypherManager getCypherManager() {
		return cypherManager;
	}
}
