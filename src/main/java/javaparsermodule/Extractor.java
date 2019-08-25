package javaparsermodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	public static final String APOSTA = "Aposta";
	public static final String SEGURO = "Seguro";
	public static final String FACADE = "Facade";
	public static final String CONTROLLER = "Controller";
	private ComponentClass<Object> componentClass;
	private Map<String, String> extendedType;
	private Map<String, List<String>> methodsFromClass;

	private List<Function<ClassOrInterfaceDeclaration, Boolean>> score;

	public Extractor() {
		this.componentClass = new ComponentClass<>();
		this.extendedType = new HashMap<>();
		this.methodsFromClass = new HashMap<>();
		this.configScore();
	}

	private void configScore() {
		Operations op = new Operations();
		this.score = new ArrayList<Function<ClassOrInterfaceDeclaration, Boolean>>();
		this.score.add(op.hasFacade);
		this.score.add(op.hasController);
		this.score.add(op.useInheritance);
	}

	public void getMethodsFromProject() throws ParseException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		this.linkClassToMethods();
		this.linkExtendTypes();
		this.printCommonMethods();
		this.printClasses();

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

	public void printClasses() throws ParseException, IOException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		int count = 1;
		this.componentClass = new ComponentClass<Object>();
		File file;
		while (count < 5) {
			file = new File("/home/mariana/Documents/tcc/labs/lab" + count);
			this.componentClass.getAllMethods(file);

			Set<ClassOrInterfaceDeclaration> classes = this.componentClass.getClasses();

			FileWriter fw = new FileWriter("../results/required" + count + ".txt");

			for (int i = 0; i < this.score.size(); i++) {
				String a = "";
				if (i == 0) {
					a = "hasFacade";
				} else if (i == 1) {
					a = "hasController";
				} else {
					a = "useInheritance";
				}

				for (ClassOrInterfaceDeclaration c : classes) {
					if ((this.score.get(i).apply(c))) {
						System.out.println("hi");
						fw.write(a + ": true" + " (" + c.getNameAsString() + ") " + LS);
						
						break;
					}
				}
				

			}
			fw.close();
			count++;

		}
	}
}