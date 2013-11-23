package common;

import java.io.IOException;
import java.io.RandomAccessFile;

public class File {
	
	private String name;
	private String id;
	private byte[] data;
	
	public File(String name, String id, String fileName) throws IOException{
		// fill in fields
		this.name = name;
		this.id = id;
		// read the file
		RandomAccessFile f = new RandomAccessFile(fileName, "r");
		this.data = new byte[(int)f.length()];
		f.read(this.data);
		f.close();
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
