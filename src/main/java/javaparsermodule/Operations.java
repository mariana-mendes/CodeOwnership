package javaparsermodule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;


public class Operations {
	
	public static final List<String> controller = new ArrayList<String>( 
			Arrays.asList("Controller", "Controlador"));
	
	public static final List<String> facade = new ArrayList<String>( 
			Arrays.asList("Facade", "Fachada"));
	
	
	 Function<ClassOrInterfaceDeclaration, Boolean> hasController = classe -> {
		boolean satisfied = false;
		for (String name : controller) {
			if(classe.getNameAsString().contains(name)) {
				satisfied = true;
				return satisfied;
			}
		}
		return satisfied;
	};
	
	 Function<ClassOrInterfaceDeclaration, Boolean>  hasFacade =  classe -> {
		boolean satisfied = false;
		for (String name : facade) {
			if(classe.getNameAsString().contains(name)) {
				satisfied = true;
				return satisfied;
			}
		}
		return satisfied;
	};
	
	 Function<ClassOrInterfaceDeclaration, Boolean> useInheritance = classe -> {
			return (!classe.getExtendedTypes().isEmpty());
	};
	
	 Function<ClassOrInterfaceDeclaration, Boolean> useAbstractClass = classe -> {
			return (classe.isAbstract());
	};
	
	
	 Function<ClassOrInterfaceDeclaration, Boolean> useInterface = classe -> {
			return (classe.isInterface());
	};
	
	 Function<ClassOrInterfaceDeclaration, Boolean> useException = classe -> {
		boolean satisfied = false;
		List<MethodDeclaration> methods = classe.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			if(!methodDeclaration.getThrownExceptions().isEmpty()) {
				satisfied = true;
				return satisfied;
			}
		}
		return satisfied;
	};
	
}
