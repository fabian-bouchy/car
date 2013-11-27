package common;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class RemoteNode {

	private String 	sName;
	private String 	sIpAddress;
	private String 	sInterface;
	private int 	iPriority;
	private int 	iPort;

	public RemoteNode(String sName, String sIpAddress, String sInterface, int iPriority, int iPort) {
		super();
		this.sName 		= sName;
		this.sIpAddress = sIpAddress;
		this.sInterface = sInterface;
		this.iPriority 	= iPriority;
		this.iPort 		= iPort;
	}
	
	protected Socket connect() throws UnknownHostException, IOException{
		return new Socket(sIpAddress, iPort);
	}

	public abstract void 	write(File file) throws UnknownHostException, IOException;
	
	public abstract boolean has(String fileId) throws UnknownHostException, IOException;
	
	public abstract File read(String fileId)throws UnknownHostException, IOException;
	
	public abstract boolean delete(String id)throws UnknownHostException, IOException;
	
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