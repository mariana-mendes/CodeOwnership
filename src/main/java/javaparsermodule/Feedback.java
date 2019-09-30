package javaparsermodule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Feedback {

	private static final String DIR = "home/marianamendes/tcc/results/";
	private HashMap<String, String> resultsKeys;
	private HashMap<String, String> artifacts;

	public Feedback() {
		this.resultsKeys = new HashMap<String, String>();
		this.artifacts = new HashMap<String, String>();
	}

	public void findLabResult() {
		List<String> records;
		for (int i = 1; i < 4; i++) {
			records = new ArrayList<String>();
			readFile(DIR + i + ".txt", records);
			writeFeedback(records);
		}

	}

	private void writeFeedback(List<String> results) {
		for (int i = 0; i < results.size(); i++) {
		}
	}

	private List<String> readFile(String filename, List<String> records) {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					System.out.println();
					char asterisco = '*';
					if (line.charAt(0) == asterisco) {
						String[] parts = line.split(":");
						System.out.println("part 1: " + parts[0]);
						System.out.println("part 2: " + parts[1]);
					}

					records.add(line);

				}
			}
			reader.close();
			return records;
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
			return null;
		}
	}

}
