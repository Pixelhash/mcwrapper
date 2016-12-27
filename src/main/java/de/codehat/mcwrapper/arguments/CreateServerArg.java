package de.codehat.mcwrapper.arguments;

import de.codehat.mcwrapper.MCWrapper;

/**
 * Argument, which handles the server creation.
 */
public class CreateServerArg extends Argument {

    public CreateServerArg(MCWrapper mcWrapper) {
        super(mcWrapper);
    }

    @Override
    public void run(String value) {
        try {
            this.getMcWrapper().getServerManager().createServer();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
