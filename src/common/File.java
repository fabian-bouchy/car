package common;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

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

	public File(String id, String fileName) throws IOException {
		
		// read the file
		RandomAccessFile f = new RandomAccessFile(fileName, "r");
		
		// fill in fields
		this.id = id;
		this.size = f.length();
		this.version = new int[ConfigManager.getN()];
		this.data = new byte[(int) f.length()];
		
		f.read(this.data);
		f.close();
	}

	public void writeToFile(String theFile) throws IOException {
		BufferedOutputStream bos = null;

		try {
			FileOutputStream fos = new FileOutputStream(theFile);
			bos = new BufferedOutputStream(fos);
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

	public int[] getVersion() {
		return version;
	}

	public void setVersion(int[] version) {
		this.version = version;
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
		return "[file] " + id + ", " + size + "B";
	}
}
