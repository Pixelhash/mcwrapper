package de.codehat.mcwrapper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

/**
 * MCWrapper's config class representation.
 */
public class Config {

    // Application version
    public int version;

    //public ArrayList<String> NAMES;

    /**
     * Config instance.
     */
    public Config() {
        this.version = 1;
        /*this.NAMES = new ArrayList<String>();
        this.NAMES.add("Peter");
        this.NAMES.add("Paul");*/
    }

    // DON'T TOUCH THE FOLLOWING CODE
    private static Config instance;

    /**
     * Get the currently loaded Config instance.
     *
     * @return Current Config instance.
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = fromDefaults();
        }
        return instance;
    }

    /**
     * Loads the Config from the specified file.
     *
     * @param file File to load.
     */
    public static void load(File file) {
        instance = fromFile(file);

        // No config file found
        if (instance == null) {
            instance = fromDefaults();
        }
    }

    /**
     * Loads the Config from the specified file path.
     *
     * @param file File to load.
     */
    public static void load(String file) {
        load(new File(file));
    }

    /**
     * Loads a new Config based on its defaults.
     *
     * @return The generated Config.
     */
    private static Config fromDefaults() {
        Config config = new Config();
        return config;
    }

    /**
     * Checks if the given file exists.
     *
     * @param file File to check.
     * @return true if exists, false if not.
     */
    public static boolean check(File file) {
        return file.exists();
    }

    /**
     * Checks if the given file exists.
     *
     * @param file File to check.
     * @return true if exists, false if not.
     */
    public static boolean check(String file) {
        return check(new File(file));
    }

    /**
     * Saves the Config file to disk.
     *
     * @param file File to save.
     */
    public void toFile(String file) {
        toFile(new File(file));
    }

    /**
     * Saves the Config file to disk.
     *
     * @param file File to save.
     */
    public void toFile(File file) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonConfig = gson.toJson(this);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            writer.write(jsonConfig);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load a Config from a given file.
     *
     * @param configFile File to load.
     * @return Loaded Config file.
     */
    private static Config fromFile(File configFile) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
            return gson.fromJson(reader, Config.class);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}