package javaparsermodule;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public Extractor() {
		this.componentClass = new ComponentClass<>();
		this.extendedType = new HashMap<>();
		this.methodsFromClass = new HashMap<>();
	}

	public void getMethodsFromProject() throws ParseException, IOException {
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
				System.out.println("Proporção métodos herdados/total de métodos da classe: " + inheritance + "/" + total);
			}
		}
	}
	
	
	public void printClasses() throws ParseException, IOException {
		this.componentClass.getAllMethods(new File("/home/mariana/Documents/tcc/LAB5/src"));
		Set<ClassOrInterfaceDeclaration> classes = this.componentClass.getClasses();
		 FileWriter fw=new FileWriter("./requiredclasses.txt");    
		for (ClassOrInterfaceDeclaration c : classes) {
			String classe = c.getNameAsString();
			
			if(!classe.contains("Test") && (classe.contains(CONTROLLER) || classe.contains(APOSTA) || classe.contains(SEGURO) || classe.contains(FACADE))) {
		           fw.write(classe);    
		           fw.write(LS);
		           
			}
	      
		}
		 fw.close(); 
	}
}