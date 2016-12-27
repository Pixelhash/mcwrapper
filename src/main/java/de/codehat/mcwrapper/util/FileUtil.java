package de.codehat.mcwrapper.util;

import java.io.File;
import java.util.Arrays;

/**
 * Util class for File operations.
 */
public class FileUtil {

    /**
     * Creates all necessary folders for MCWrapper.
     */
    public static void createDirs() {
        Arrays.asList(
                Constants.APP_DIR, Constants.SERVER_CONFIG_DIR, Constants.LOG_DIR
        ).forEach(f -> new File(f).mkdir());
    }

}
