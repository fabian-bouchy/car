package server;


public class UserManager {
	
	private UserManager(){}
	
	private static String username;

	public static void init(String name) throws Exception {
		username = name;
	}

	public static String getUsername() {
		return username;
	}
}
