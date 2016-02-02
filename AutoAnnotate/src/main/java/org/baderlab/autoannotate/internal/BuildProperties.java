package org.baderlab.autoannotate.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildProperties {

	private static final String PROPS_FILE = "app.props";
	
	public static final String APP_VERSION;
	public static final String APP_NAME;
	public static final String APP_ID;
	public static final String APP_URL;
	public static final String MANUAL_URL;
	public static final String BUILD_ID;

	private BuildProperties() {}
	
	static {
		Properties props;
		try {
			props = getPropertiesFromClasspath(PROPS_FILE, false);
		} catch (IOException e) {
			e.printStackTrace();
			props = new Properties();
		}

		APP_VERSION = props.getProperty("project.version", "unknown");
		APP_NAME    = props.getProperty("project.name", "AutoAnnotate");
		APP_ID      = props.getProperty("bundle.namespace", "org.baderlab.autoannotate");
		APP_URL     = props.getProperty("app.url", "");
		MANUAL_URL  = props.getProperty("app.url.manual", "");
		
		String buildUser   = props.getProperty("build.user", "user");
		String gitCommitId = props.getProperty("git.commit.id", "0");
		BUILD_ID = String.format("Build from GIT: %s by: %s", gitCommitId, buildUser);
	}
	

	private static Properties getPropertiesFromClasspath(String propFileName, boolean inMaindir) throws IOException {
		InputStream inputStream;
		if(inMaindir)
			inputStream = BuildProperties.class.getClassLoader().getResourceAsStream(propFileName);
		else
			inputStream = BuildProperties.class.getResourceAsStream(propFileName);

		if (inputStream == null)
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");

		Properties props = new Properties();
		props.load(inputStream);
		inputStream.close();
		return props;
	}
	

	/**
	 * Debug output.
	 */
	public static void print() {
		Properties props;
		try {
			props = getPropertiesFromClasspath(PROPS_FILE, false);
		} catch (IOException e) {
			e.printStackTrace();
			props = new Properties();
		}
		
		try {
			props.store(System.out, "Contents of " + PROPS_FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
