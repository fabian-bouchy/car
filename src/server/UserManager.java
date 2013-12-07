package server;

import common.CipherManager;


public class UserManager {
	
	private UserManager(){}

	private static String username;

	private static CipherManager cypherManager;

	public static void init(String name, String passPhrase) throws Exception {
		username = name;
		cypherManager = new CipherManager(passPhrase, String.valueOf(Math.random()));
	}

	public static String getUsername() {
		return username;
	}

	public static CipherManager getCypherManager() {
		return cypherManager;
	}
}
