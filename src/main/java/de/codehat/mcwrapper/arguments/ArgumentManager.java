package de.codehat.mcwrapper.arguments;

import java.util.HashMap;
import java.util.Map;

/**
 * Managers all Arguments.
 */
public class ArgumentManager {

    private Map<String, Argument> argumentRegistry = new HashMap<>();

    public ArgumentManager() {

    }

    public boolean exists(String option) {
        return this.argumentRegistry.containsKey(option);
    }

    public void addArgument(String option, Argument argument) {
        if (!exists(option)) {
            this.argumentRegistry.put(option, argument);
        }
    }

    public Argument getArgument(String option) {
        if (this.argumentRegistry.containsKey(option)) {
            return this.argumentRegistry.get(option);
        }
        return null;
    }

    public boolean run(String option, String value) {
        if (exists(option)) {
            getArgument(option).run(value);
            return true;
        }
        return false;
    }

}
