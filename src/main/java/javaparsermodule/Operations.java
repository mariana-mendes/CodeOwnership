package javaparsermodule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;


public class Operations {
	
	public static final List<String> controller = new ArrayList<String>( 
			Arrays.asList("Controller", "Controlador"));
	
	public static final List<String> facade = new ArrayList<String>( 
			Arrays.asList("Facade", "Fachada", "Sistema"));
	
	
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
			return (classe.getExtendedTypes() != null);
	};
	
	 Function<ClassOrInterfaceDeclaration, Boolean> useAbstractClass = classe -> {
			return (classe.isAbstract());
	};
	
	
	 Function<ClassOrInterfaceDeclaration, Boolean> useInterface = classe -> {
			return (classe.isInterface());
	};

}
