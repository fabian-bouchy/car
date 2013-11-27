package common;

public class UtilBobby {
	
	public final static String SPLIT_REGEX			= ":";
	public final static String ANSWER_TRUE		 	= "true";
	public final static String ANSWER_FALSE		 	= "false";
	public final static String ANSWER_OK		 	= "ok";
	
	
	/** Server */
	public final static String CLIENT		 		= "client";
	//write
	public final static String CLIENT_WRITE 		= "client:write";
	
	// Delete
	public final static String CLIENT_DELETE 		= "client:delete";
	
	// Delete
	public final static String CLIENT_READ	 		= "client:read";
	
	/** Server */
	public final static String SERVER		 		= "server";
	//write
	public final static String SERVER_WRITE 		= "server:write";
	public final static String SERVER_WRITE_READY 	= "server:write:ready";
	public final static String SERVER_WRITE_OK		= "server:write:ok";
	public final static String SERVER_WRITE_KO		= "server:write:KO";
	
	// Has
	public final static String SERVER_HAS 			= "server:has";
	public final static String SERVER_HAS_READY 	= "server:has:ready";
	public final static String SERVER_HAS_OK		= "server:has:ok";
	public final static String SERVER_HAS_KO		= "server:has:KO";
	
	// Delete
	public final static String SERVER_DELETE 		= "server:delete";
	public final static String SERVER_DELETE_READY 	= "server:delete:ready";
	public final static String SERVER_DELETE_OK		= "server:delete:ok";
	public final static String SERVER_DELETE_KO		= "server:delete:KO";
	
	/** Replica */
	public final static String REPLICA		 		= "replica";
	// Write
	public final static String REPLICA_WRITE_SYMBOL	= "write";
	public final static String REPLICA_WRITE		= REPLICA + SPLIT_REGEX + REPLICA_WRITE_SYMBOL;
	public final static String REPLICA_WRITE_READY  = REPLICA_WRITE + SPLIT_REGEX + "ready";
	public final static String REPLICA_WRITE_OK		= REPLICA_WRITE + SPLIT_REGEX + "ok";
	public final static String REPLICA_WRITE_KO		= REPLICA_WRITE + SPLIT_REGEX + "KO";
	
	// Has
	public final static String REPLICA_HAS_SYMBOL	 = "has";
	public final static String REPLICA_HAS			 = REPLICA + SPLIT_REGEX + REPLICA_HAS_SYMBOL;
	public final static String REPLICA_HAS_K0		 = REPLICA_HAS + SPLIT_REGEX + "KO";
	
	// Delete
	public final static String REPLICA_DELETE_SYMBOL = "delete";
	public final static String REPLICA_DELETE		 = REPLICA + SPLIT_REGEX + REPLICA_DELETE_SYMBOL;
	public final static String REPLICA_DELETE_OK	 = REPLICA_DELETE + SPLIT_REGEX + "ok";
	public final static String REPLICA_DELETE_KO	 = REPLICA_DELETE + SPLIT_REGEX + "KO";
}
