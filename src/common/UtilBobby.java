package common;

/**
 * Common variable declarations.
 * Represent all sent and received message accross protocol communications.
 * Allow fast and stable modifications.  
 */
public class UtilBobby {
	
	public final static String SPLIT_REGEX			= ":";
	public final static String SPLIT_FILE			= "+";
	public final static String ANSWER_TRUE		 	= "true";
	public final static String ANSWER_FALSE		 	= "false";
	public final static String ANSWER_OK		 	= "ok";
	
	
	/** Server */
	public final static String CLIENT		 		= "client";

	// Write
	public final static String CLIENT_WRITE_SYMBOL	= "write";
	public final static String CLIENT_WRITE			= CLIENT + SPLIT_REGEX + CLIENT_WRITE_SYMBOL;
	
	// Delete
	public final static String CLIENT_DELETE_SYMBOL	= "delete";
	public final static String CLIENT_DELETE		= CLIENT + SPLIT_REGEX + CLIENT_DELETE_SYMBOL;
	
	// Read
	public final static String CLIENT_READ_SYMBOL	= "read";
	public final static String CLIENT_READ			= CLIENT + SPLIT_REGEX + CLIENT_READ_SYMBOL;

	// List
	public final static String CLIENT_LIST_SYMBOL	= "list";
	public final static String CLIENT_LIST			= CLIENT + SPLIT_REGEX + CLIENT_LIST_SYMBOL;

	/** Server */
	public final static String SERVER		 		= "server";
	// Write
	public final static String SERVER_WRITE_SYMBOL		= "write";
	public final static String SERVER_WRITE 			= SERVER + SPLIT_REGEX + SERVER_WRITE_SYMBOL;
	public final static String SERVER_WRITE_READY 		= SERVER_WRITE + SPLIT_REGEX + "ready";
	public final static String SERVER_WRITE_OK			= SERVER_WRITE + SPLIT_REGEX + "ok";
	public final static String SERVER_WRITE_KO			= SERVER_WRITE + SPLIT_REGEX + "KO";

	// List
	public final static String SERVER_LIST_SYMBOL	= "list";
	public final static String SERVER_LIST 			= SERVER + SPLIT_REGEX + SERVER_LIST_SYMBOL;
	public final static String SERVER_LIST_READY 	= SERVER_LIST + SPLIT_REGEX + "ready";

	// Has
	public final static String SERVER_HAS_SYMBOL	= "has";
	public final static String SERVER_HAS 			= SERVER + SPLIT_REGEX + SERVER_HAS_SYMBOL;
	public final static String SERVER_HAS_READY 	= SERVER_HAS + SPLIT_REGEX + "ready";
	public final static String SERVER_HAS_OK		= SERVER_HAS + SPLIT_REGEX + "ok";
	public final static String SERVER_HAS_KO		= SERVER_HAS + SPLIT_REGEX + "KO";

	// Delete
	public final static String SERVER_DELETE_SYMBOL	= "delete";
	public final static String SERVER_DELETE 		= SERVER + SPLIT_REGEX + SERVER_DELETE_SYMBOL;
	public final static String SERVER_DELETE_READY 	= SERVER_DELETE + SPLIT_REGEX + "ready";
	public final static String SERVER_DELETE_OK		= SERVER_DELETE + SPLIT_REGEX + "ok";
	public final static String SERVER_DELETE_KO		= SERVER_DELETE + SPLIT_REGEX + "KO";

	// Read
	public final static String SERVER_READ_SYMBOL			= "read";
	public final static String SERVER_READ					= SERVER + SPLIT_REGEX + SERVER_READ_SYMBOL;
	public final static String SERVER_READ_READY  			= SERVER_READ + SPLIT_REGEX + "ready";
	public final static String SERVER_READ_FILE_FOUND		= SERVER_READ + SPLIT_REGEX + "file_found";
	public final static String SERVER_READ_FILE_NOT_FOUND	= SERVER_READ + SPLIT_REGEX + "file_not_found";
	public final static String SERVER_READ_REDIRECT_TO		= SERVER_READ + SPLIT_REGEX + "redirect_to";
	public final static String SERVER_READ_KO				= SERVER_READ + SPLIT_REGEX + "KO";

	/** Replica */
	public final static String REPLICA		 		= "replica";
	// Write
	public final static String REPLICA_WRITE_SYMBOL			= "write";
	public final static String REPLICA_WRITE				= REPLICA + SPLIT_REGEX + REPLICA_WRITE_SYMBOL;
	public final static String REPLICA_WRITE_READY  		= REPLICA_WRITE + SPLIT_REGEX + "ready";
	public final static String REPLICA_WRITE_READY_FILE  	= REPLICA_WRITE_READY + SPLIT_REGEX + "file";
	public final static String REPLICA_WRITE_READY_META  	= REPLICA_WRITE_READY + SPLIT_REGEX + "meta";
	public final static String REPLICA_WRITE_OK				= REPLICA_WRITE + SPLIT_REGEX + "ok";
	public final static String REPLICA_WRITE_KO				= REPLICA_WRITE + SPLIT_REGEX + "KO";

	// TRANSACTION
	public final static String REPLICA_TRANSACTION_SYMBOL	= "transaction";
	public final static String REPLICA_TRANSACTION          = REPLICA + SPLIT_REGEX + REPLICA_TRANSACTION_SYMBOL;
	public final static String REPLICA_TRANSACTION_COMMIT   = REPLICA_TRANSACTION + SPLIT_REGEX + "commit";
	public final static String REPLICA_TRANSACTION_COMMITED = REPLICA_TRANSACTION + SPLIT_REGEX + "commited";
	public final static String REPLICA_TRANSACTION_ABORT   	= REPLICA_TRANSACTION + SPLIT_REGEX + "abort";
	public final static String REPLICA_TRANSACTION_ABORTED 	= REPLICA_TRANSACTION + SPLIT_REGEX + "aborted";

	// getMetadata
	public final static String REPLICA_METADATA_SYMBOL			= "metadata";
	public final static String REPLICA_METADATA_GET_SYMBOL		= "get";
	public final static String REPLICA_METADATA_ADD_SYMBOL		= "add";
	public final static String REPLICA_METADATA_DELETE_SYMBOL	= "delete";
	public final static String REPLICA_METADATA			= REPLICA + SPLIT_REGEX + REPLICA_METADATA_SYMBOL;
	public final static String REPLICA_METADATA_GET		= REPLICA_METADATA + SPLIT_REGEX + REPLICA_METADATA_GET_SYMBOL;
	public final static String REPLICA_METADATA_READY  	= REPLICA_METADATA + SPLIT_REGEX + "ready";
	public final static String REPLICA_METADATA_OK		= REPLICA_METADATA + SPLIT_REGEX + "ok";
	public final static String REPLICA_METADATA_KO		= REPLICA_METADATA + SPLIT_REGEX + "KO";
	public final static String REPLICA_METADATA_ADD		= REPLICA_METADATA + SPLIT_REGEX + REPLICA_METADATA_ADD_SYMBOL;
	public final static String REPLICA_METADATA_DELETE	= REPLICA_METADATA + SPLIT_REGEX + REPLICA_METADATA_DELETE_SYMBOL;
	public final static String REPLICA_METADATA_ADDED		= REPLICA_METADATA + SPLIT_REGEX + "added";
	public final static String REPLICA_METADATA_DELETED		= REPLICA_METADATA + SPLIT_REGEX + "deleted";
	public final static String REPLICA_METADATA_ADD_READY		= REPLICA_METADATA_ADD + SPLIT_REGEX + "ready";
	public final static String REPLICA_METADATA_DELETE_READY	= REPLICA_METADATA_DELETE + SPLIT_REGEX + "ready";

	// Has
	public final static String REPLICA_HAS_SYMBOL	 = "has";
	public final static String REPLICA_HAS			 = REPLICA + SPLIT_REGEX + REPLICA_HAS_SYMBOL;
	public final static String REPLICA_HAS_K0		 = REPLICA_HAS + SPLIT_REGEX + "KO";

	// Delete
	public final static String REPLICA_DELETE_SYMBOL 	= "delete";
	public final static String REPLICA_DELETE		 	= REPLICA + SPLIT_REGEX + REPLICA_DELETE_SYMBOL;
	public final static String REPLICA_DELETE_OK	 	= REPLICA_DELETE + SPLIT_REGEX + "ok";
	public final static String REPLICA_DELETE_NOT_FOUND	= REPLICA_DELETE + SPLIT_REGEX + "file_not_found";
	public final static String REPLICA_DELETE_KO	 	= REPLICA_DELETE + SPLIT_REGEX + "KO";

	// Read
	public final static String REPLICA_READ_SYMBOL	= "read";
	public final static String REPLICA_READ			= REPLICA + SPLIT_REGEX + REPLICA_READ_SYMBOL;
	public final static String REPLICA_READ_READY  	= REPLICA_READ + SPLIT_REGEX + "ready";
	public final static String REPLICA_READ_OK		= REPLICA_READ + SPLIT_REGEX + "ok";
	public final static String REPLICA_READ_KO		= REPLICA_READ + SPLIT_REGEX + "KO";
}
