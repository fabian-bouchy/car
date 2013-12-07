package common;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Semaphore;

import server.UserManager;

public class File implements java.io.Serializable{
	
	private static final long serialVersionUID = -970661273181400441L;

	private String id;
	private String fileName;
	private byte[] data;
	private long size;
	private int[] version;
	private boolean hasFile;

	private Semaphore lock;

	public File(String fileName, boolean init) throws IOException {
		
		// lock on the file
		this.lock = new Semaphore(1);
		
		// fill in fields
		this.fileName = fileName;
		this.id = UserManager.getUsername()+"_"+fileName;
		this.size = 0;
		this.version = new int[ConfigManager.getN()];
		this.data = null;
		this.hasFile = false;
		
		if(init) {
			// read the file
			RandomAccessFile f = new RandomAccessFile(fileName, "r");
			this.size = f.length();
			this.data = new byte[(int) f.length()];
			f.read(this.data);
			// Encrypt
			this.data = UserManager.getCypherManager().encrypt(this.data);
			f.close();
			this.hasFile = true;
		}
	}
	
	public void lock() throws InterruptedException {
		this.lock.acquire();
	}
	
	public void unlock() throws InterruptedException {
		this.lock.release();
	}
	

	/**
	 * Create meta-data for the file gives in parameters
	 * @param file 
	 */
	private File(File file) {
		// lock on the file
		this.lock = new Semaphore(1);
		// fill in fields for an empty meta-data
		this.id = file.getId();
		this.size = 0;
		this.version = new int[ConfigManager.getN()];
		this.setVersion(file.getVersion());
		this.data = null;
		this.hasFile = file.hasFile;
	}
	public boolean isFile() {
		return this.data != null;
	}
	public void setHasFile(boolean hasFile) {
		this.hasFile = hasFile;
	}
	public boolean hasFile() {
		return this.hasFile;
	}

	public File generateMetadata(){
		return new File(this);
	}

	public void writeToFile(String theFile) throws IOException {
		BufferedOutputStream bos = null;
		
		System.out.println("[file] Writing " + this.fileName + " => " + theFile);

		try {
			FileOutputStream fos = new FileOutputStream(theFile);
			bos = new BufferedOutputStream(fos);
			if(this.data != null){
				this.data = UserManager.getCypherManager().decrypt(this.data);
				bos.write(this.data);
			}
		} catch (Exception e) {
			System.out.println("[file] Error writing to a file " + e);
		} finally {
			if (bos != null) {
				try {
					bos.flush();
					bos.close();
				} catch (Exception e) {
					System.out.println("[file] Error closing file " + e);
				}
			}
		}
	}
	

	public long getSize() {
		return size;
	}
	
	public int[] getVersion() {
		return version;
	}
	
	public void setVersion(int[] version) {
		this.version = version;
	}
	
	public void incrementVersion(int index){
		this.version[index]++;
	}

	public int getGlobalVersion() {
		int globalVersion = 0;
		for(int i = 0; i < version.length; i++) {
			globalVersion += version[i];
		}
		return globalVersion;
	}
	
	public boolean isCompatibleWith(File newFile)
	{
		if (newFile == null){
			return false;
		}
		
		for (int i=0; i < ConfigManager.getN(); i++){
			if (newFile.getVersion()[i] < this.version[i]){
				return false;
			}
		}
		
		return true;
	}

	public String getId() {
		return id;
	}
	
	public byte[] getData() {
		return data;
	}
	
	private String getVersionToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i = 0; i < this.version.length; i++ ) {
			sb.append(this.version[i] + " ");
		}
		sb.append("]");
		return sb.toString();
	}

	public String getFileName() {
		return fileName;
	}

	public File getMetadata() {
		File metadata = new File(this);
		return metadata;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		File other = (File)obj;
		for(int i = 0; i < this.version.length; i++ ) {
			if(this.version[i] != other.version[i])
				return false;
		}
			
		return true;
	}
	
	public String toString(){
		if(isFile()){
			return "[file] " + id + " (" + fileName + "), " + size + "B" + " version " + getVersionToString();
		}
		return "[metadata]  (" + fileName + "), version " + getVersionToString();
	}
}
