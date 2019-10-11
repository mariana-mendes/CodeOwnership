package javaparsermodule;

import java.util.HashMap;

public class Repository {

	private HashMap<String, ComponentClass<Object>> currentLabs;

	public Repository() {
		this.currentLabs = new HashMap<String, ComponentClass<Object>>();
	}

	public HashMap<String, ComponentClass<Object>> getCurrentLabs() {
		return currentLabs;
	}

	public void setCurrentLabs(HashMap<String, ComponentClass<Object>> currentLabs) {
		this.currentLabs = currentLabs;
	}
	
	public void addNewLab(String key, ComponentClass<Object> lab) {
		this.currentLabs.put(key, lab);
	}
	
	
	
	
}
