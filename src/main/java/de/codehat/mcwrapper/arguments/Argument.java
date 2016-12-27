package de.codehat.mcwrapper.arguments;

import de.codehat.mcwrapper.MCWrapper;

/**
 * Represents an Argument.
 */
public abstract class Argument {

    private MCWrapper mcWrapper;

    public Argument(MCWrapper mcWrapper) {
        this.mcWrapper = mcWrapper;
    }

    public MCWrapper getMcWrapper() {
        return this.mcWrapper;
    }

    public abstract void run(String value);
}
