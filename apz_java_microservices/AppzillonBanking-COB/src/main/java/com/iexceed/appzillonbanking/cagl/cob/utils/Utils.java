package com.iexceed.appzillonbanking.cagl.cob.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Utils {
	
	public static Map<String, String> getPropertiesFromClassPath(String fileName) {
		Map<String, String> map = new HashMap<>();
		try (InputStream input = Utils.class.getClassLoader().getResourceAsStream(fileName)) {
			if (input != null) {
				Properties prop = new Properties();
				prop.load(input);
				if (!prop.isEmpty()) {
					Set<Object> objs = prop.keySet();
					for (Object obj : objs) {
						map.put(obj.toString(), prop.getProperty(obj.toString()));
					}
				}
			}
		} catch (IOException ex) {
		}
		return map;
	}

}
