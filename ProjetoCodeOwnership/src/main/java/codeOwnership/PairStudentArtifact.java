package codeOwnership;

import java.text.DecimalFormat;

import artifact.Artifact;
import student.Student;
import util.Util;

public class PairStudentArtifact {

	private Student student;
	private Artifact artifact;
	public double ownershipPercentage;

	public PairStudentArtifact(Student student, Artifact artifact, double ownershipPercentage) {
		this.student = student;
		this.artifact = artifact;
		this.ownershipPercentage = ownershipPercentage;
	}

	public Artifact getArtifact() {
		return this.artifact;
	}

	public String getStudentName() {
		return this.student.getName();
	}

	public String getArtifactName() {
		return this.artifact.getName();
	}

	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.#");
		return student.getName() + " is the owner of: " + artifact.getName() + " (" + df.format(this.ownershipPercentage) + "%) "
				+ Util.LS + "Artifact details:" + Util.LS + artifact.toString();
	}
}
