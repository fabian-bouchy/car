package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;

public class Benchmark {
	
	// In kb unit
	private static final int[] fileSizes = {1000, 10000, 20000, 50000, 100000};
	private static final String outputPath = "/tmp/test_files";
	private static final String testFilePrefix = outputPath + "/test_";
	private static final String outputFilePrefix = outputPath + "/output_";
	private static final String outputFileExtension = ".csv";
	private static final String testFileExtension = ".txt";
	
	private static File currentOutputLogFile;
	
	
	// Init benchmark ei: create needed files
	public static void init() {
		// Créating output directory
		File outputDir = new File(outputPath);

		if (!outputDir.exists()) {
			System.out.println("creating directory: " + outputPath);
			if(outputDir.mkdir()) {  
				System.out.println("DIR created");  
			}
		}
		for (int size : fileSizes) {
			try {
//				RandomAccessFile file = new RandomAccessFile(testFilePrefix + size + testFileExtension,"rw");
//				file.setLength(size);
//				file.close();
				Runtime.getRuntime().exec("dd if=/dev/zero of="+ testFilePrefix + size + testFileExtension + " bs=1kB count=" + size);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void startTest() {
		currentOutputLogFile = new File(outputFilePrefix + Math.random() + outputFileExtension);
		try {
			currentOutputLogFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Current output file: " + currentOutputLogFile.getName());
		System.out.println("Starting writing test...");
		writeTest();
		System.out.println("Starting updating test...");
		updateTest();
		System.out.println("Starting reading test...");
		readTest();
		System.out.println("Starting deleting test...");
		deleteTest();
		
		displayResult();
		System.out.println("Test finish output file: " + currentOutputLogFile.getAbsolutePath());
	}
	
	private static void writeTest() {
		// Prepare output file
		log("-------------- Test write -----------------");
		// Header
		log("size(kB);time(ns)");
		for (int size : fileSizes) {
			try {
				common.File file = new common.File(testFilePrefix + size + testFileExtension, true);
				System.out.println(file);
				// start
				long startTime = System.nanoTime();
				ServerManager.write(file);
				long elapsedTime = System.nanoTime() - startTime;
				// end
				log(size + ";" + elapsedTime);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				log(size + ";" + e.getLocalizedMessage());
			} catch (IOException e) {
				e.printStackTrace();
				log(size + ";" + e.getLocalizedMessage());
			}
		}
	}
	
	private static void updateTest() {
		log("-------------- Test Update -----------------");
		// Header
		log("size(kB);time(ns)");
		for (int size : fileSizes) {
			try {
				common.File file = new common.File(testFilePrefix + size + testFileExtension, true);
				System.out.println(file);
				long startTime = System.nanoTime();
				ServerManager.write(file);
				long elapsedTime = System.nanoTime() - startTime;
				log(size + ";" + elapsedTime);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				log(size + ";" + e.getLocalizedMessage());
			} catch (IOException e) {
				e.printStackTrace();
				log(size + ";" + e.getLocalizedMessage());
			}
		}
	}

	private static void readTest() {
		log("-------------- Test Read -----------------");
		// Header
		log("size(kB);time(ns)");
		for (int size : fileSizes) {
			try {
				long startTime = System.nanoTime();
				common.File file = new common.File(testFilePrefix + size + testFileExtension, false);
				System.out.println(file);
				ServerManager.read(file);
				long elapsedTime = System.nanoTime() - startTime;
				log(size + ";" + elapsedTime);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				log(size + ";" + e.getLocalizedMessage());
			} catch (IOException e) {
				e.printStackTrace();
				log(size + ";" + e.getLocalizedMessage());
			}
		}
	}
	
	private static void deleteTest() {
		log("-------------- Test Delete -----------------");
		// Header
		log("size(kB);time(ns)");
		for (int size : fileSizes) {
			try {
				long startTime = System.nanoTime();
				common.File file = new common.File(testFilePrefix + size + testFileExtension, false);
				System.out.println(file);
				ServerManager.delete(file);
				long elapsedTime = System.nanoTime() - startTime;
				log(size + ";" + elapsedTime);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				log(size + ";" + e.getLocalizedMessage());
			} catch (IOException e) {
				e.printStackTrace();
				log(size + ";" + e.getLocalizedMessage());
			}
		}
	}
	
	private static void log(String line) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(currentOutputLogFile, true));
			bw.write(line );
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void displayResult() {
		System.out.println();
		System.out.println("Test results:");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(currentOutputLogFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

