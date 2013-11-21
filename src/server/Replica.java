package server;

public class Replica {
	
	private String 	sName;
	private String 	sIpAdresse;
	private int 	iPriority;
	private int 	iPort;

	public Replica(String sName, String sIpAdresse, int iPriority, int iPort) {
		super();
		this.sName 		= sName;
		this.sIpAdresse = sIpAdresse;
		this.iPriority 	= iPriority;
		this.iPort 		= iPort;
	}

	public String getName() {
		return sName;
	}

	public void setName(String sName) {
		this.sName = sName;
	}

	public String getIpAdresse() {
		return sIpAdresse;
	}

	public void setIpAdresse(String sIpAdresse) {
		this.sIpAdresse = sIpAdresse;
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
