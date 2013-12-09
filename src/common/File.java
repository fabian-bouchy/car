package common;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import server.UserManager;

/**
 * Represent a file in the filesystem.
 */
public class File implements java.io.Serializable{
	
	private static final long serialVersionUID = -970661273181400441L;

	private String id;
	private String fileName;
	private byte[] data;
	private long size;
	private int[] version;
	private boolean hasFile;

	private Semaphore lock;

	public File(String fileName, boolean init, boolean zip) throws IOException {
		
		// lock on the file
		this.lock = new Semaphore(1);
		
		// fill in fields
		this.fileName = fileName;
		this.id = UserManager.getUsername() + UtilBobby.SPLIT_FILE + fileName;
		this.size = 0;
		this.version = new int[ConfigManager.getN()];
		this.data = null;
		this.hasFile = false;
		
		if(init) {
			// read the file from hard drive.
			System.out.println("[File] Reading file from storage...");
			RandomAccessFile f = new RandomAccessFile(fileName, "r");
			
			java.io.File file = new java.io.File(fileName);
			this.fileName = file.getName();
			this.id = UserManager.getUsername() + UtilBobby.SPLIT_FILE + this.fileName;
			
			this.size = f.length();
			this.data = new byte[(int) f.length()];
			f.read(this.data);
			
			// zip and change it's name/id
			if (zip){
				byte[] zipped = zipBytes(this.fileName, this.data);
				this.data = zipped;
				this.size = zipped.length;
				this.fileName += ".zip";
				this.id = UserManager.getUsername() + UtilBobby.SPLIT_FILE + this.fileName;
			}
			
			// Encrypt
			if(UserManager.isCypherEnabled()) {
				this.data = UserManager.getCypherManager().encrypt(this.data);
			}
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
		this.id = file.id;
		this.fileName = file.fileName;
		this.size = 0;
		this.version = new int[ConfigManager.getN()];
		this.setVersion(file.getVersion());
		this.data = null;
		this.hasFile = file.hasFile;
	}

	/**
	 * Persiste file to hard drive
	 * @param outputPathName The output file path
	 */
	public void writeToFile(String outputPathName) throws IOException {
		BufferedOutputStream bos = null;
		
		System.out.println("[File] Writing " + this.fileName + " => " + outputPathName);

		try {
			FileOutputStream fos = new FileOutputStream(outputPathName);
			bos = new BufferedOutputStream(fos);
			if(this.data != null){
				if(UserManager.isCypherEnabled()) {
					this.data = UserManager.getCypherManager().decrypt(this.data);
				}
				bos.write(this.data);
			}
		} catch (Exception e) {
			System.out.println("[File] Error writing to a file " + e);
		} finally {
			if (bos != null) {
				try {
					bos.flush();
					bos.close();
				} catch (Exception e) {
					System.out.println("[File] Error closing file " + e);
				}
			}
		}
	}
	
	public void reinitializeVector(){
		this.version = new int[ConfigManager.getN()];
		for (int i = 0; i < this.version.length; i++){
			this.version[i] = 0;
		}
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
	
	public static byte[] zipBytes(String filename, byte[] input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		ZipEntry entry = new ZipEntry(filename);
		entry.setSize(input.length);
		zos.putNextEntry(entry);
		zos.write(input);
		zos.closeEntry();
		zos.close();
		return baos.toByteArray();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		File other = (File)obj;
		
		if (!id.equals(other.id)){
			return false;
		}
		
		for(int i = 0; i < this.version.length; i++ ) {
			if(this.version[i] != other.version[i])
				return false;
		}
			
		return true;
	}
	
	public int hashCode(){
		return id.charAt(0);
	}
	
	public String toString(){
		if(isFile()){
			return "[File] " + id + " (" + fileName + "), " + size + "B" + " version " + getVersionToString();
		}
		return "[File] is a metadata " + id + " (" + fileName + "), version " + getVersionToString();
	}
}
