package analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import artifact.Artifact;
import artifact.ArtifactRepository;
import git.GitRepository;
import git.GitUtil;
import pair.PairStudentArtifact;
import student.Student;
import student.StudentRepository;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

public class CreationAnalysis extends AbstractAnalysis {

	private static double DEFAULT_OWNERSHIP_VALUE = 100.0;

	public CreationAnalysis(StudentRepository studentRepository, ArtifactRepository artifactRepository) {
		super(studentRepository, artifactRepository);
	}

	@Override
	public List<PairStudentArtifact> makePairs(GitRepository git) throws GitAPIException, IOException {
		DiffFormatter diffFormatter = git.getDiffFormatter();
		
		List<PairStudentArtifact> pairs = new ArrayList<PairStudentArtifact>();

		for (RevCommit commit : git.getCommits()) {
			String studentName = commit.getAuthorIdent().getName();
			List<String> newArtifacts;
			
			if (GitUtil.isFirstCommit(commit)) {
				TreeWalk treeWalk = git.getFirstCommitTreeWalk(commit);
				newArtifacts = GitUtil.getArtifactsFromFirstCommit(treeWalk);
			} else {
				newArtifacts = GitUtil.getArtifactsFromCommit(diffFormatter, commit);
			}

            pairs.addAll(this.createCommitPairs(studentName, newArtifacts));
		}

		return pairs;
	}

	private List<PairStudentArtifact> createCommitPairs(String studentName, List<String> artifactPaths) {
		Student student = this.studentRepository.getStudent(studentName);
		List<PairStudentArtifact> pairs = new ArrayList<>();

		for (String path : artifactPaths) {
			Artifact artifact = this.artifactRepository.getArtifact(path);
			if (artifact != null)
				pairs.add(new PairStudentArtifact(student, artifact, DEFAULT_OWNERSHIP_VALUE));
		}

		return pairs;
	}

}
