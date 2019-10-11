package javaparsermodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

	public Extractor() throws ParseException, IOException {
		this.componentClass = new ComponentClass<>();
		this.extendedType = new HashMap<>();
		this.methodsFromClass = new HashMap<>();
		this.repo = new Repository();
		this.persistLabs();
		this.configScore();
	}

	private void configScore() {
		Operations op = new Operations();
		this.score = new HashMap<String, Function<ClassOrInterfaceDeclaration, Boolean>>();
		this.score.put("hasFacade", op.hasFacade);
		this.score.put("hasController", op.hasController);
		this.score.put("useInheritance", op.useInheritance);
		this.score.put("useInterface", op.useInterface);
		this.score.put("useAbstractClass", op.useAbstractClass);
		this.score.put("useException", op.useException);
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
		String filename = "/home/mariana/Documents/tcc/CodeOwnership/names.txt";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					System.out.println(line);
					this.componentClass = new ComponentClass<Object>();
					file = new File("/home/mariana/Documents/tcc/source/" + line);
					this.componentClass.register(file);
					this.repo.addNewLab(line, this.componentClass);

				}
			}
			reader.close();
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
		}
		
		
		while (count <= 30) {

		}
	}

	public void checkRequeridStatements() throws ParseException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Set<String> labs = this.repo.getCurrentLabs().keySet();
		for (String lab : labs) {
			Set<ClassOrInterfaceDeclaration> classes = this.repo.getCurrentLabs().get(lab).getClasses();
			System.out.println("lab " + lab);
			FileWriter fw = new FileWriter("../results/required" + lab + ".txt");
			Set<String> keys = this.score.keySet();

			for (String key : keys) {
				boolean findCase = false;
				boolean ans = false;
				for (ClassOrInterfaceDeclaration c : classes) {
					ans = this.score.get(key).apply(c);
					if (ans && !findCase) {
						findCase = true;
						fw.write(LS + "* " + key + ": true" + LS);
						fw.write("- " + c.getNameAsString() + LS);
					} else if (ans && findCase) {
						fw.write("- " + c.getNameAsString() + LS);
					}
				}
				if (!ans && !findCase) {
					fw.write(LS + "* " + key + ": false" + LS);
				}
			}
			checkTests(this.repo.getCurrentLabs().get(lab), fw);
			checkCollections(this.repo.getCurrentLabs().get(lab), fw);
			fw.close();

		}
	}

	private void checkTests(ComponentClass<Object> component, FileWriter fw) throws IOException {
		Set<String> tests = component.getTestClass();
		fw.write(LS + "* " + "hasTests: ");
		if (!tests.isEmpty()) {
			fw.write("true (");
			for (String string : tests) {
				fw.write(string + ", ");
			}
			fw.write(")");
		} else {
			fw.write("false");
		}
		fw.write(LS);
	}

	private void checkCollections(ComponentClass<Object> lab, FileWriter fw) throws IOException {

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
			fw.write("true " + LS);
			for (String string : sets) {
				fw.write("- " + string + LS);
			}

		} else {
			fw.write("false " + LS);
		}

		fw.write(LS + "* " + "usedHashMap: ");
		if (!maps.isEmpty()) {
			fw.write("true " + LS);
			for (String string : maps) {
				fw.write("- " + string + LS);
			}

		} else {
			fw.write("false " + LS);
		}

		fw.write(LS + "* " + "usedArrayList: ");
		if (!arrays.isEmpty()) {
			fw.write("true " + LS);
			for (String string : arrays) {
				fw.write("- " + string + LS);
			}

		} else {
			fw.write("false " + LS);
		}
	}

}