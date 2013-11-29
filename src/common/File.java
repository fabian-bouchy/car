package common;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

public class File implements java.io.Serializable{
	
	// http://www.java2s.com/Code/Java/File-Input-Output/Readfiletobytearrayandsavebytearraytofile.htm

	/**
	 * 
	 */
	private static final long serialVersionUID = -970661273181400441L;

	private String id;
	private byte[] data;
	private long size;
	private int[] version;

	private int globalVersion = 0;

	public File(String id, String fileName, boolean init) throws IOException {
		// fill in fields
		this.id = id;
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

	public void writeToFile(String theFile) throws IOException {
		BufferedOutputStream bos = null;

		try {
			FileOutputStream fos = new FileOutputStream(theFile);
			bos = new BufferedOutputStream(fos);
			if(this.data != null)
				bos.write(this.data);
		} catch (Exception e) {
			System.out.println("Error writing to a file " + e);
		} finally {
			if (bos != null) {
				try {
					bos.flush();
					bos.close();
				} catch (Exception e) {
					System.out.println("Error closing file " + e);
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
		return "[file] " + id + ", " + size + "B" + " version:" + getVersionToString();
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
}
