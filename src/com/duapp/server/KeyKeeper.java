package com.duapp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class KeyKeeper {
	
	private static String API_KEY = null;
	private static String SECRET_KEY = null;
	private static String APNS_FILE = null;
	private static String APNS_FILE_SECRET = null;
	private static boolean APNS_DEV = false;
	
	static {
		String confDirName = System.getProperty("conf.dir");
		File confDir = null;
		if (confDirName == null) {
			confDir = new File("conf");
		} else {
			confDir = new File(confDirName);
		}
			
		Properties props = new Properties();
		File serverPropFile = new File(confDir, "server.properties");
		if (confDir.exists() && serverPropFile.exists()) {
			FileInputStream fin = null;
			try {
				fin = new FileInputStream(serverPropFile);
				props.load(fin);
				SECRET_KEY = (String)props.get("secret.key");
				API_KEY = (String)props.get("api.key");
				APNS_FILE = (String)props.get("apns.file");
				APNS_FILE_SECRET = (String)props.get("apns.file.secret");
				APNS_DEV = Boolean.getBoolean( props.getProperty("apns.dev"));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fin != null) {
					try {
						fin.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static String api_key() {
		//String API_KEY = "aj3pCjOETKkdkE9dwV2cFcrK";
		return API_KEY;
	}

	public static String secret_key() {
		//String SECRET_KEY = "QiHxmD6CBkjz5nGdcaNIDOog7MAaSSN7";
		return SECRET_KEY;
	}
	
	public static String apnsFile(){
		return APNS_FILE;
	}
	
	public static String apnsFileSecret(){
		return APNS_FILE_SECRET;
	}
	
	public static boolean apnsDev(){
		return APNS_DEV;
	}
}
