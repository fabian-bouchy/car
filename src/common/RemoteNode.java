package common;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public abstract class RemoteNode {

	private String 	sName;
	private String 	sIpAddress;
	private int 	iPriority;
	private int 	iPort;

	public RemoteNode(String sName, String sIpAddress, int iPriority, int iPort) {
		super();
		this.sName 		= sName;
		this.sIpAddress = sIpAddress;
		this.iPriority 	= iPriority;
		this.iPort 		= iPort;
	}
	
	protected Socket connect() throws UnknownHostException, IOException{
		return new Socket(sIpAddress, iPort);
	}

	public abstract void 	write(File file) throws Exception;
	public abstract boolean	commitWrite(File file) throws Exception;
	public abstract boolean	abortWrite(File file) throws Exception;
	public abstract File 	read(File file) throws Exception;
	public abstract boolean has(File file) throws Exception;
	public abstract boolean delete(File file) throws Exception;
	
	public abstract HashMap<String, File> getMetadata() throws Exception;
	public abstract HashMap<String, File> listFiles(String username) throws Exception;
	
	public String getName() {
		return sName;
	}

	public void setName(String sName) {
		this.sName = sName;
	}

	public String getIpAddress() {
		return sIpAddress;
	}

	public void setIpAddress(String sIpAddress) {
		this.sIpAddress = sIpAddress;
	}

	public int getPriority() {
		return iPriority;
	}

	public void setPriority(int iPriority) {
		this.iPriority = iPriority;
	}
	
	public int getPort() {
		return iPort;
	}
	
	public void setPort(int iPort) {
		this.iPort = iPort;
	}
}
