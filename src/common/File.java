package common;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import server.UserManager;

public class File implements java.io.Serializable{
	
	// http://www.java2s.com/Code/Java/File-Input-Output/Readfiletobytearrayandsavebytearraytofile.htm

	/**
	 * 
	 */
	private static final long serialVersionUID = -970661273181400441L;

	private String id;
	private String fileName;
	private byte[] data;
	private long size;
	private int[] version;

	private int globalVersion = 0;

	public File(String fileName, boolean init) throws IOException {
		// fill in fields
		this.fileName = fileName;
		this.id = UserManager.getUsername()+"_"+fileName;
		this.size = 0;
		this.version = new int[ConfigManager.getN()];
		this.data = null;
		
		if(init) {
			// read the file
			RandomAccessFile f = new RandomAccessFile(fileName, "r");
			this.size = f.length();
			this.data = new byte[(int) f.length()];
			f.read(this.data);
			f.close();
		}
	}

	/**
	 * Create metadata for the file gives in parameters
	 * @param file 
	 */
	public File(File file) {
		// fill in fields
		this.id = file.getId();
		this.size = 0;
		this.version = new int[ConfigManager.getN()];
		this.setVersion(file.getVersion());
		this.data = null;
	}

	public void writeToFile(String theFile) throws IOException {
		BufferedOutputStream bos = null;
		
		System.out.println("[file] Writing " + this.fileName + " => " + theFile);

		try {
			FileOutputStream fos = new FileOutputStream(theFile);
			bos = new BufferedOutputStream(fos);
			if(this.data != null){
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

	public void setSize(long size) {
		this.size = size;
	}

	public int getGlobalVersion() {
		return globalVersion;
	}
	
	public int[] getVersion() {
		return version;
	}

	public void setVersion(int[] version) {
		this.version = version;
		computeGlobalVersion();
	}

	private void computeGlobalVersion() {
		globalVersion = 0;
		for(int i = 0; i < version.length; i++) {
			globalVersion += version[i];
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String toString(){
		return "[file] " + id + " (" + fileName + "), " + size + "B" + " version " + getVersionToString();
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

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		
		File other = (File)obj;
		for(int i = 0; i < this.version.length; i++ ) {
			if(this.version[i] != other.version[i])
				return false;
		}
			
		return true;
	}
	
	public File getMetadata() {
		File metadata = new File(this);
		return metadata;
	}
}
