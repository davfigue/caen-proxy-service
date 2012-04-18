/**
 * 
 */
package com.asisoft.caen.host.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

/**
 * General Helper functions for the project
 * 
 * @author "David Figueroa Escalante"
 * 
 * TODO: Problems with paths in windows, apparently because of the spaces
 */
public class PipelineHelper {
	
	/**
	 * The logger instance
	 */
	private static Logger logger = Logger.getLogger(PipelineHelper.class);
	

	/**
	 * Returns the absolute path to the project's jar file
	 * 
	 * @param classToUse
	 * @return
	 */
	public static String getPathToJarFile(Object classToUse) {

		String url = classToUse.getClass()
				.getResource("/" + classToUse.getClass().getName().replaceAll("\\.", "/") + ".class").toString();

		if (url.contains(".jar!")) {

			url = url.substring(5).replaceFirst("/[^/]+\\.jar!.*$", "/");
		}

		try {
			File dir = new File(new URL(url).toURI());
			url = dir.getAbsolutePath();

		} catch (MalformedURLException mue) {
			url = null;

		} catch (URISyntaxException ue) {
			url = null;
		}

		return url;
	}

	/**
	 * This method returns the absolute path of the resources defined in the
	 * classpath of the project because some dependencies use paths instead of
	 * resources for the initialization of parameters.
	 * 
	 * When the maven project is built with dependencies, all the resources
	 * defined in the classpath (contained in the jar) are copied to the
	 * filesystem in the same level as the generated jar, so is possible to
	 * overwrite the configuration parameters
	 * 
	 * @param path
	 * @return
	 */
	public static String getResourcePath(String path) {

		String newPath = PipelineHelper.class.getResource(path).getPath();

		if (newPath.contains(".jar!")) {
			/*
			 * substring for deleting "file:" and replacing the jar name till
			 * the end
			 */
			newPath = newPath.substring(5).replaceFirst("/[^/]+\\.jar!.*$", "");
			newPath += path;
		}
		
		if(SystemUtils.IS_OS_WINDOWS) {
			// Removing the first "/" ocurrence if it's windows
			newPath = newPath.replaceFirst("/", "");
		}
		
		logger.debug("Absolute path: " + newPath);

		return newPath;
	}
}
