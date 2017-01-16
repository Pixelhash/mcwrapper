package de.codehat.mcwrapper.util;

import java.io.File;

/**
 * Holds all important constants.
 */
public class Constants {

    // Application directory
    public static final String APP_DIR = System.getProperty("user.home") + File.separator + ".mcwrapper"
            + File.separator;

    // Logs directory
    public static final String LOG_DIR = APP_DIR + "logs" + File.separator;

    // SSL directory
    public static final String SSL_DIR = APP_DIR + "ssl" + File.separator;

    // Server configurations directory
    public static final String SERVER_CONFIG_DIR = APP_DIR + "servers" + File.separator;

    // Application version String
    public static final String VERSION_STRING = "0.0.1";

    // Config path
    public static final String CONFIG = APP_DIR + "config.json";

    // Application version
    public static final int VERSION = 1;

    public static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
}
