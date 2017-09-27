package net.meta.cefconnector;

import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;


public class ResourceBundleUtil extends ResourceBundle {

	private Properties properties;
    
    public ResourceBundleUtil(Properties properties) {
                this.properties = properties;
    }

	@Override
	protected Object handleGetObject(String key) {
		// TODO Auto-generated method stub
		return properties.get(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}
}
