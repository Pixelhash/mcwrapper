package de.codehat.mcwrapper;

import de.codehat.mcwrapper.arguments.*;
import de.codehat.mcwrapper.server.ServerManager;
import de.codehat.mcwrapper.util.Config;
import de.codehat.mcwrapper.util.Constants;
import de.codehat.mcwrapper.util.FileUtil;
import org.apache.commons.cli.*;

/**
 * MCWrapper instance.
 */
public class MCWrapper {

    private ArgumentManager argumentManager = new ArgumentManager();
    private ServerManager serverManager = new ServerManager();

    /**
     * MCWrapper instance takes the application arguments.
     *
     * @param args Application arguments.
     */
    public MCWrapper(String[] args) {
        // If there is no Config, create the default one
        if (!Config.check(Constants.CONFIG)) {
            // Create directories if necessary
            FileUtil.createDirs();
            // Load default Config
            Config.load(Constants.CONFIG);
            // Save default Config to disk
            Config.getInstance().toFile(Constants.CONFIG);
        } else {
            // Load stored Config
            Config.load(Constants.CONFIG);
        }

        this.registerArguments();
        Options options = new Options();
        options.addOption("h", false, "shows a better help page");
        options.addOption("c", false, "create a new server");
        options.addOption("s", true, "start a server");
        options.addOption("r", true, "remove a server");
        options.addOption("l", false, "list all servers");
        options.addOption("i", true, "get information about a server");
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("MCWrapper", options);
            System.exit(1);
        }
        boolean commandFound = false;
        for (Option option : options.getOptions()) {
            if (cmd.hasOption(option.getOpt())) {
                commandFound = true;
                this.argumentManager.run(option.getOpt(), cmd.getOptionValue(option.getOpt()));
                break;
            }
        }
        if (!commandFound) formatter.printHelp("MCWrapper", options);
    }

    public ServerManager getServerManager() {
        return this.serverManager;
    }

    private void registerArguments() {
        this.argumentManager.addArgument("h", new HelpArg(this));
        this.argumentManager.addArgument("c", new CreateServerArg(this));
        this.argumentManager.addArgument("l", new ListServerArg(this));
        this.argumentManager.addArgument("s", new StartServerArg(this));
    }


}
