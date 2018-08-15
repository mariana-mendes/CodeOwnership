package util;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import pair.PairStudentArtifact;
import student.Student;

public class Util {

	public static final String LS = System.lineSeparator();
	private static final String TSV_SEPARATOR = "\t";

	public static boolean isJavaClass(String string) {
		String[] splitString = string.split("\\.");

		return splitString.length == 2 && splitString[1].equals("java");
	}

	public static List<Student> getStudentsFromJson(String jsonPath) {
		List<Student> students = new ArrayList<Student>();

		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(jsonPath));
			JSONArray jsonStudents = (JSONArray) jsonObject.get("students");

			for (int index = 0; index < jsonStudents.size(); index++) {
				JSONObject jsonStudent = (JSONObject) jsonStudents.get(index);
				String studentName = (String) jsonStudent.get("name");
				JSONArray studentAliasesJson = (JSONArray) jsonStudent.get("aliases");
				String[] studentAliases = convertToStringArray(studentAliasesJson);

				students.add(new Student(studentName, studentAliases));
			}
		} catch (Exception e) {
			System.err.printf("Error trying to open the file: %s.", e.getMessage());

		}

		return students;
	}

	public static boolean generateTSV(List<PairStudentArtifact> pairs) {
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("outputs/analysis-result.tsv"), "utf-8"));

			writer.write("STUDENT NAME" + TSV_SEPARATOR +
					"CLASS PATH" + TSV_SEPARATOR +
					"OWNERSHIP PERCENTAGE" + TSV_SEPARATOR +
					"EXPERTISE LIST" + LS);

			DecimalFormat df = new DecimalFormat("#.#");

			for (PairStudentArtifact pair : pairs) {
				String line = pair.getStudentName() + TSV_SEPARATOR +
						pair.getArtifactName() + TSV_SEPARATOR +
						df.format(pair.ownershipPercentage) + TSV_SEPARATOR +
						Arrays.toString(pair.getArtifact().getExpertiseArray()) + LS;
				writer.write(line);
			}

			writer.close();

			return true;
		} catch (IOException ex) {
			return false;
		}
	}

		private static String[] convertToStringArray(JSONArray jsonArray) {
		String[] convertedArray = new String[jsonArray.size()];

		for (int index = 0; index < jsonArray.size(); index++) {
			convertedArray[index] = (String) jsonArray.get(index);
		}

		return convertedArray;
	}

}
