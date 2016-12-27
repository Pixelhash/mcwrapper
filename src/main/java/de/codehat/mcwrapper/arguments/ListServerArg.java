package de.codehat.mcwrapper.arguments;

import de.codehat.mcwrapper.MCWrapper;

public class ListServerArg extends Argument {

    public ListServerArg(MCWrapper mcWrapper) {
        super(mcWrapper);
    }

    @Override
    public void run(String value) {
        System.out.println("Following servers are available:");
        this.getMcWrapper().getServerManager().listServers().forEach(i -> System.out.println(" - " + i.substring(0, i.length() - 5)));
    }
}
