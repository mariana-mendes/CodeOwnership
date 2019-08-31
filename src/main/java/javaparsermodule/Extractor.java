package javaparsermodule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class Extractor {

	public static final String LS = System.lineSeparator();
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
		while (count <= 10) {
			this.componentClass = new ComponentClass<Object>();
			file = new File("/home/mariana/Documents/tcc/labs-otimizado/" + count);
			this.componentClass.register(file);
			this.repo.addNewLab(count, this.componentClass);
			count++;
		}
	}

	public void checkRequeridStatements() throws ParseException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Set<Integer> labs = this.repo.getCurrentLabs().keySet();
		System.out.println(labs);
		for (Integer lab : labs) {
			Set<ClassOrInterfaceDeclaration> classes = this.repo.getCurrentLabs().get(lab).getClasses();

			FileWriter fw = new FileWriter("../results/required" + lab + ".txt");
			Set<String> keys = this.score.keySet();

			for (String key : keys) {
				boolean ans = false;
				for (ClassOrInterfaceDeclaration c : classes) {
					ans = this.score.get(key).apply(c);
					if (ans) {
						fw.write(key + ": true" + " (" + c.getNameAsString() + ") " + LS);
						break;
					}
				}
				if (!ans) {
					fw.write(key + ": false" + LS);
				}
			}
			fw.close();

		}
	}
}