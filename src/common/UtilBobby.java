package common;

public class UtilBobby {
	
	public final static String SPLIT_REGEX			= ":";
	
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
	public final static String REPLICA_WRITE		= "replica:write";
	public final static String REPLICA_WRITE_READY  = "replica:write:ready";
	public final static String REPLICA_WRITE_OK		= "replica:write:ok";
	public final static String REPLICA_WRITE_KO		= "replica:write:KO";
	
	// Has
	public final static String REPLICA_HAS			= "replica:has";
	public final static String REPLICA_HAS_READY	= "replica:has:ready";
	public final static String REPLICA_HAS_OK		= "replica:has:OK";
	public final static String REPLICA_HAS_K0		= "replica:has:KO";
	
	// Delete
	public final static String REPLICA_DELETE		= "replica:delete";
	public final static String REPLICA_DELETE_READY = "replica:delete:ready";
	public final static String REPLICA_DELETE_OK	= "replica:delete:ok";
	public final static String REPLICA_DELETE_KO	= "replica:delete:KO";
}
