package de.codehat.mcwrapper.arguments;

import de.codehat.mcwrapper.MCWrapper;
import de.codehat.mcwrapper.server.ServerDoesNotExistException;

import java.io.IOException;

/**
 * Starts the chosen Server.
 */
public class StartServerArg extends Argument {

    public StartServerArg(MCWrapper mcWrapper) {
        super(mcWrapper);
    }

    @Override
    public void run(String value) {
        try {
            this.getMcWrapper().getServerManager().startServer(value);
        } catch (ServerDoesNotExistException | IOException e) {
            e.printStackTrace();
        }
    }
}
