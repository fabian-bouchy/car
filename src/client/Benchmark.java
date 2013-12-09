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

import server.UserManager;
import common.ConfigManager;
import common.ConfigManager.ConfigType;

/**
 * @class Benchmark
 * Use to benchmark the project. Provide tests for Write/Update/Read with
 * different size files.
 */
public class Benchmark {
	// Benchmark configuration: file size in kb unit.
	private static final int[] fileSizes = {50, 100, 500, 1000, 5000, 25000};
	private static final String outputPath = "./tmp/test_files/";
	private static final String testFilePrefix = "test_";
	private static final String testFilePrefixPath = outputPath + testFilePrefix;
	private static final String outputFilePrefix = "./tmp/output_";
	private static final String outputFileExtension = ".csv";
	private static final String testFileExtension = ".txt";

	private static File currentOutputLogFile;

	public static void run(String[] args){
		// Global initialization
		try {
			if (args.length == 2){
				// config name specified
				ConfigManager.init(ConfigType.CLIENT, args[1]);
			}else{
				// all default parameters
				ConfigManager.init(ConfigType.CLIENT);
			}
			// init the user manager
			String passWord = null;
			if(args[0].endsWith("+crypto")) {
				passWord = "testpwd";
			}
			UserManager.init("benchmarker" + Math.random(), passWord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("[client] Benchmark starting...");
		Benchmark.init();
		Benchmark.startTest();
		System.out.println("[client] Benchmark ended!");
	}
	
	
	// Init benchmark ei: create needed files
	public static void init() {
		// Creating output directory
		File outputDir = new File(outputPath);
		if (!outputDir.exists()) {
			System.out.println("creating directory: " + outputPath);
			if(outputDir.mkdirs()) {  
				System.out.println("DIR created");  
			}
		}
		// Creating files of the right sizes
		for (int size : fileSizes) {
			try {
				RandomAccessFile file = new RandomAccessFile(testFilePrefixPath + size + testFileExtension,"rw");
				file.setLength(size*1024);
				file.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Main function, start test procedure.
	 * Init() should be called before.
	 */
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
		log("size(kb);time(ns);bandwidth(MB/s)");
		// Do the job for each file
		for (int size : fileSizes) {
			try {
				common.File file = new common.File(testFilePrefixPath + size + testFileExtension, true, false);
				System.out.println(file);
				// start
				long startTime = System.nanoTime();
				ServerManager.write(file);
				long elapsedTime = System.nanoTime() - startTime;
				double bandwidth = (((long)size / (elapsedTime / 1000000000.0))/1024.0)*8;
				// end
				log(size + ";" + elapsedTime + ";" + bandwidth);
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
		log("size(kb);time(ns);bandwidth(MB/s)");
		// Do the job for each file
		for (int size : fileSizes) {
			try {
				common.File file = new common.File(testFilePrefixPath + size + testFileExtension, true, false);
				System.out.println(file);
				long startTime = System.nanoTime();
				ServerManager.write(file);
				long elapsedTime = System.nanoTime() - startTime;
				double bandwidth = (((long)size / (elapsedTime / 1000000000.0))/1024.0)*8;
				// end
				log(size + ";" + elapsedTime + ";" + bandwidth);
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
		log("size(kb);time(ns);bandwidth(MB/s)");
		// Do the job for each file
		for (int size : fileSizes) {
			try {
				common.File file = new common.File(testFilePrefix + size + testFileExtension, false, false);
				System.out.println(file);
				long startTime = System.nanoTime();
				ServerManager.read(file);
				long elapsedTime = System.nanoTime() - startTime;
				double bandwidth = (((long)size / (elapsedTime / 1000000000.0))/1024.0)*8;
				// end
				log(size + ";" + elapsedTime + ";" + bandwidth);
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
		log("size(kb);time(ns)");
		// Do the job for each file
		for (int size : fileSizes) {
			try {
				common.File file = new common.File(testFilePrefix + size + testFileExtension, false, false);
				System.out.println(file);
				long startTime = System.nanoTime();
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

	/**
	 * Use to add line in the output file
	 * @param line Input line.
	 */
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

	/**
	 * Display in the console the output file content.
	 */
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