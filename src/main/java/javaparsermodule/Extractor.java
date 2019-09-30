package javaparsermodule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class Extractor {

	public static final String LS = System.lineSeparator();
	public static final ArrayList<String> collections = new ArrayList<String>(
			Arrays.asList("ArrayList", "List", "Set", "HashSet", "Map", "HashMap"));
	private ComponentClass<Object> componentClass;
	private Map<String, String> extendedType;
	private Map<String, List<String>> methodsFromClass;
	private Repository repo;

	private Map<String, Function<ClassOrInterfaceDeclaration, Boolean>> score;
	FileWriter csvWriter = new FileWriter("output.csv");

	public Extractor() throws ParseException, IOException {
		this.componentClass = new ComponentClass<>();
		this.extendedType = new HashMap<>();
		this.methodsFromClass = new HashMap<>();
		this.repo = new Repository();
		this.persistLabs();
		this.configScore();
	}

	private void configScore() throws IOException {
		Operations op = new Operations();
		this.score = new HashMap<String, Function<ClassOrInterfaceDeclaration, Boolean>>();
		csvWriter.append("hasFacade");
		csvWriter.append(",");
		this.score.put("hasFacade", op.hasFacade);

		csvWriter.append("hasController");
		csvWriter.append(",");
		this.score.put("hasController", op.hasController);

		csvWriter.append("useInheritance");
		csvWriter.append(",");
		this.score.put("useInheritance", op.useInheritance);

		csvWriter.append("useInterface");
		csvWriter.append(",");
		this.score.put("useInterface", op.useInterface);

		csvWriter.append("useAbstractClass");
		csvWriter.append(",");
		this.score.put("useAbstractClass", op.useAbstractClass);

		csvWriter.append("useException");
		csvWriter.append(",");
		this.score.put("useException", op.useException);

		csvWriter.append("hasTests");
		csvWriter.append(",");
		csvWriter.append("usedHashSet");
		csvWriter.append(",");
		csvWriter.append("usedHashMap");
		csvWriter.append(",");
		csvWriter.append("usedArrayList");
		csvWriter.append("\n");
	}

	public void getMethodsFromProject() throws ParseException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		this.linkClassToMethods();
		this.linkExtendTypes();
		this.printCommonMethods();
		this.checkRequeridStatements();

	}

	private void linkClassToMethods() {
		Set<Object> methods = this.componentClass.getMethods().keySet();
		for (Object key : methods) {
			this.methodsFromClass.put((String) key.toString(), new ArrayList<>());
			for (MethodDeclaration method : this.componentClass.getMethods().get(key)) {
				this.methodsFromClass.get(key).add(method.getDeclarationAsString(false, false, false));

			}
		}
	}

	private void linkExtendTypes() {
		Map<Object, NodeList<ClassOrInterfaceType>> classes = this.componentClass.getInheritance();
		Set<Object> c = classes.keySet();
		for (Object object : c) {
			if (classes.get(object).size() > 0) {
				this.extendedType.put((String) object,
						classes.get(object).toString().substring(1, classes.get(object).toString().length() - 1));
			}
		}
	}

	private void printCommonMethods() {
		Set<String> classes = this.extendedType.keySet();

		for (String classe : classes) {
			int total = 0;
			int inheritance = 0;
			System.out.print(LS + "CLASS  >>>" + classe);
			System.out.println("   EXTENDED  >>>" + this.extendedType.get(classe));

			List<String> superMethods = this.methodsFromClass.get(this.extendedType.get(classe));

			List<String> methods = this.methodsFromClass.get(classe);
			total = methods.size();

			if (superMethods != null && methods != null) {
				for (String superMethod : superMethods) {
					for (String method : methods) {
						if (superMethod.equals(method)) {
							inheritance += 1;
						}
					}
				}
				System.out
						.println("Proporção métodos herdados/total de métodos da classe: " + inheritance + "/" + total);
			}
		}
	}

	public void persistLabs() throws ParseException, IOException {
		int count = 1;
		File file;
		while (count <= 23) {
			this.componentClass = new ComponentClass<Object>();
			file = new File("/home/marianamendes/tcc/labs/src" + count);
			this.componentClass.register(file);
			this.repo.addNewLab(count, this.componentClass);
			count++;
		}
	}

	public void checkRequeridStatements() throws ParseException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Set<Integer> labs = this.repo.getCurrentLabs().keySet();
		for (Integer lab : labs) {
			List<String> output = new ArrayList<String>();
			Set<ClassOrInterfaceDeclaration> classes = this.repo.getCurrentLabs().get(lab).getClasses();
			System.out.println("lab " + lab);
			FileWriter fw = new FileWriter("../results/required" + lab + ".txt");
			List<String> keys = new ArrayList<String>(Arrays.asList("hasFacade",  "hasController",
					"useInheritance", "useInterface", "useAbstractClass", "useException"));

			for (String key : keys) {
				boolean findCase = false;
				boolean ans = false;
				for (ClassOrInterfaceDeclaration c : classes) {
					ans = this.score.get(key).apply(c);
					if (ans && !findCase) {
						findCase = true;
						output.add("true");
						fw.write(LS + "* " + key + ": true" + LS);
						fw.write("- " + c.getNameAsString() + LS);
					} else if (ans && findCase) {
						fw.write("- " + c.getNameAsString() + LS);
					}
				}
				if (!ans && !findCase) {
					output.add("false");
					fw.write(LS + "* " + key + ": false" + LS);
				}
			}

			output.add(checkTests(this.repo.getCurrentLabs().get(lab), fw));
			checkCollections(this.repo.getCurrentLabs().get(lab), fw, output);
			fw.close();

			csvWriter.append(String.join(",", output));
			csvWriter.append("\n");
		}
		csvWriter.flush();
		csvWriter.close();
	}

	private String checkTests(ComponentClass<Object> component, FileWriter fw) throws IOException {
		String ans = "";
		Set<String> tests = component.getTestClass();
		fw.write(LS + "* " + "hasTests: ");
		if (!tests.isEmpty()) {
			ans = "true";
			fw.write("true (");
			for (String string : tests) {
				fw.write(string + ", ");
			}
			fw.write(")");
		} else {
			ans = "false";
			fw.write("false");
		}
		fw.write(LS);
		return ans;
	}

	private void checkCollections(ComponentClass<Object> lab, FileWriter fw, List<String> output) throws IOException {

		List<String> sets = new ArrayList<String>();
		List<String> maps = new ArrayList<String>();
		List<String> arrays = new ArrayList<String>();
		for (ClassOrInterfaceDeclaration cl : lab.getClasses()) {
			List<FieldDeclaration> fields = cl.getFields();
			for (FieldDeclaration fieldDeclaration : fields) {
				if (!fieldDeclaration.getElementType().isPrimitiveType()
						&& !fieldDeclaration.getElementType().asString().equals("String")) {
					if (fieldDeclaration.getElementType().toString().contains("Map")) {
						maps.add(fieldDeclaration.getElementType().toString());
					}
					if (fieldDeclaration.getElementType().toString().contains("Set")) {
						sets.add(fieldDeclaration.getElementType().toString());
					}
					if (fieldDeclaration.getElementType().toString().contains("List")) {
						arrays.add(fieldDeclaration.getElementType().toString());
					}

				}
			}
		}

		fw.write(LS + "* " + "usedHashSet: ");
		if (!sets.isEmpty()) {
			output.add("true");
			fw.write("true " + LS);
			for (String string : sets) {
				fw.write("- " + string + LS);
			}

		} else {
			output.add("false");
			fw.write("false " + LS);
		}

		fw.write(LS + "* " + "usedHashMap: ");
		if (!maps.isEmpty()) {
			output.add("true");
			fw.write("true " + LS);
			for (String string : maps) {
				fw.write("- " + string + LS);
			}

		} else {
			output.add("false");
			fw.write("false " + LS);
		}

		fw.write(LS + "* " + "usedArrayList: ");
		if (!arrays.isEmpty()) {
			output.add("true");
			fw.write("true " + LS);
			for (String string : arrays) {
				fw.write("- " + string + LS);
			}

		} else {
			output.add("false");
			fw.write("false " + LS);
		}
	}

}