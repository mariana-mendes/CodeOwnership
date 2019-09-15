package app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Scanner;

import analysis.AnalysisType;
import artifact.Artifact;
import codeOwnership.CodeOwnership;
import exception.StudentNotFoundException;
import expertise.Expertise;
import javaparsermodule.Extractor;
import javaparsermodule.Feedback;
import pair.PairStudentArtifact;
import student.Student;
import student.StudentExpertise;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.github.javaparser.ParseException;

import util.Util;

import static util.Util.LS;

public class App {

	private static CodeOwnership co;
	private static Scanner in = new Scanner(System.in);
	private static Extractor ext;
	private static Feedback fd;
	
	public static void main(String[] args) throws Exception {
		ext = new Extractor();
		fd = new Feedback();
		useParser();
		fd.findLabResult();
		
	}

	
	private static void useParser() throws ParseException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ext.checkRequeridStatements();
	}

}
