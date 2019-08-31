package javaparsermodule;

import java.util.HashMap;
import java.util.Map;

public class Repository {

	private HashMap<Integer, ComponentClass<Object>> currentLabs;

	public Repository() {
		this.currentLabs = new HashMap<Integer, ComponentClass<Object>>();
	}

	public HashMap<Integer, ComponentClass<Object>> getCurrentLabs() {
		return currentLabs;
	}

	public void setCurrentLabs(HashMap<Integer, ComponentClass<Object>> currentLabs) {
		this.currentLabs = currentLabs;
	}
	
	public void addNewLab(int key, ComponentClass<Object> lab) {
		this.currentLabs.put(key, lab);
	}
	
	
	
	
}
