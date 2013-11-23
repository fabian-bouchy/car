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
	
	private String name;
	private String id;
	private byte[] data;

	public File(String name, String id, String fileName) throws IOException {
		// fill in fields
		this.name = name;
		this.id = id;
		// read the file
		RandomAccessFile f = new RandomAccessFile(fileName, "r");
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
