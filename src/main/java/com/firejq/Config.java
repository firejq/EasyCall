package com.firejq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author <a href="mailto:firejq@outlook.com">firejq</a>
 */
public class Config {

	static {
		try (InputStream inputStream = Config.class
				.getClassLoader()
				.getResourceAsStream("config.properties")) {
			Properties properties = new Properties();
			properties.load(inputStream);
			Config.INPUT_PORT = Integer.parseInt(
					(String) properties.getOrDefault("input-port",																   2333));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int INPUT_PORT;
}
