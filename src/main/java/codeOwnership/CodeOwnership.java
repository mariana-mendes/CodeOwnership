package codeOwnership;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import analysis.AnalysisFactory;
import analysis.AnalysisType;
import analysis.AbstractAnalysis;
import artifact.ArtifactRepository;
import exception.StudentNotFoundException;
import git.GitRepository;
import pair.PairRepository;
import pair.PairStudentArtifact;
import student.StudentRepository;
import student.Student;
import util.Util;

import org.eclipse.jgit.api.errors.GitAPIException;

public class CodeOwnership {

    private String repoPath;
	private GitRepository git;
	private AbstractAnalysis analysis;
	private StudentRepository studentRepository;
	private ArtifactRepository artifactRepository;
	private PairRepository pairRepository;

	public CodeOwnership(AnalysisType analysisType, String repoPath) throws IOException {
	    this.repoPath = repoPath;
		this.git = new GitRepository(repoPath + "/.git");

		this.createStudentRepository();
		this.createArtifactRepository();
		this.instantiateAnalysis(analysisType);
		this.createPairRepository();
	}

	public void createStudentRepository() {
	    this.studentRepository = new StudentRepository();
        List<Student> students = Util.getStudentsFromJson(this.repoPath + "/students.json");
        this.studentRepository.setStudents(students);
	}

	public void createArtifactRepository() throws IOException {
        this.artifactRepository = new ArtifactRepository();
        List<String> classes = this.git.listRepositoryJavaClasses();
        this.artifactRepository.createArtifacts(this.repoPath, classes);
    }

    public void instantiateAnalysis(AnalysisType analysisType) {
		this.analysis = AnalysisFactory.getAnalysis(analysisType, this.studentRepository, this.artifactRepository);
	}
	public void createPairRepository() {
		try {
		    this.pairRepository = new PairRepository();
			List<PairStudentArtifact> pairs = this.analysis.makePairs(git);
			this.pairRepository.setPairs(pairs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PairRepository getPairRepository() {
		System.out.println("uhu");
		return this.pairRepository;
	}

	/**
	 * Lists all the students names in the system; it is used for writing the .txt
	 * file.
	 */
	public HashSet<String> listAllStudentsNames() throws GitAPIException, IOException {
		return git.listAllStudentsNames();
	}

	public String listStudents() {
		return this.studentRepository.toString();
	}

	public Student getStudentByIndex(int index) {
		return this.studentRepository.getStudents().get(index);
	}

	public String getStudentContributionInfo(String studentName) throws StudentNotFoundException {
		return this.pairRepository.getStudentContributionInfo(studentName);
	}
}