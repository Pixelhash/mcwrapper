package de.codehat.mcwrapper.arguments;

import de.codehat.mcwrapper.MCWrapper;

public class HelpArg extends Argument {

    public HelpArg(MCWrapper mcWrapper) {
        super(mcWrapper);
    }

    @Override
    public void run(String value) {
        System.out.print("Help page of 'MCWrapper':");
    }
}
