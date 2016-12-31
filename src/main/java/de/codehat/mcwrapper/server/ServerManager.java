package de.codehat.mcwrapper.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.codehat.mcwrapper.util.Constants;
import de.codehat.mcwrapper.web.Console;
import de.codehat.mcwrapper.web.ConsoleWebSocketHandler;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.*;

/**
 * Manages servers and their configs.
 */
public class ServerManager {

    private Map<String, Predicate<Object>> fieldTests = new HashMap<>();
    private Map<String, String> fieldDescriptions = new HashMap<>();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private SecureRandom random = new SecureRandom();
    private boolean subExit = false;

    public static BufferedWriter writer;
    public static String passwordHash = null;
    public static boolean restart = true;

    public ServerManager() {
        this.setupFieldTests();
        this.setupFieldDescriptions();
    }

    public void startServer(String name) throws ServerDoesNotExistException {
        Server server = this.getServer(name);
        passwordHash = server.getPassword();
        System.out.println("Starting server '" + name + "' with uuid '" + server.getUuid() + "'...");
        if (server.isWebInterface()) {
            ipAddress(server.getIpAddress());
            port(server.getPort());
            secure(Constants.SSL_DIR + server.getKeystore() + ".jks", server.getKeystorePass(), null, null);
            staticFiles.location("/public");
            webSocket("/console", ConsoleWebSocketHandler.class);
            init();
        }

        Scanner in = new Scanner(System.in);
        while (restart) {
            subExit = false;
            restart = server.isRestartOnCrash();
            ProcessBuilder processBuilder = new ProcessBuilder(server.getStartArgs());
            processBuilder.directory(new File(server.getDirectory()));
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process process = null;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                System.out.println("Could not start server!");
                e.printStackTrace();
                System.exit(1);
            }

            //Read out dir output
            InputStream is = process.getInputStream();
            OutputStream os = process.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(os));
            Thread inputThread = new Thread(() -> {
                //Scanner in = new Scanner(System.in);
                boolean exit = false;
                while (!exit && in.hasNext()) {
                    String cmd1 = in.nextLine();
                    if (subExit) {
                        exit = true;
                        return;
                    }
                    try {
                        if (cmd1.toLowerCase().equals("!stop")) {
                            restart = false;
                            writer.write("stop\n");
                            writer.flush();
                            exit = true;
                            return;
                        } else if (cmd1.toLowerCase().equals("stop")) {
                            writer.write("stop\n");
                            writer.flush();
                            exit = true;
                            return;
                        }
                        writer.write(cmd1 + "\n");
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            inputThread.start();

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            System.out.printf("Output:\n");
            try {
                while ((line = br.readLine()) != null) {
                    final String lineCopy = line;
                    System.out.println(line);
                    /*Console.userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(s -> Console.broadcastMessage(
                            Console.userUsernameMap.get(s), lineCopy));*/
                    if (server.isWebInterface()) Console.broadcastMessage("Server", lineCopy);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Wait to get exit value
            try {
                int exitValue = process.waitFor();
                System.out.println("\n\nExit Value is " + exitValue);
                subExit = true;
                os.close();
                is.close();
                writer.close();
                br.close();
                isr.close();
                System.out.println("Sleeping 2.5s to stop process...");
                Thread.sleep(2500);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        if (server.isWebInterface()) stop();
        in.close();
        System.out.println("Press ENTER to close application.");
    }

    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

    public Server getServer(String name) throws ServerDoesNotExistException {
        File serverConf = new File(Constants.SERVER_CONFIG_DIR + name + ".json");
        if (!serverConf.exists()) {
            throw new ServerDoesNotExistException("Server '" + name + "' does not exist!");
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(serverConf)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        Server server = gson.fromJson(reader, Server.class);
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return server;
    }

    public List<String> listServers() {
        File serverFolder = new File(Constants.SERVER_CONFIG_DIR);
        List<String> servers = new ArrayList<>();
        for (File file : serverFolder.listFiles()) {
            servers.add(file.getName());
        }
        return servers;
    }

    public boolean checkServerExists(String name) {
        return new File(Constants.SERVER_CONFIG_DIR + name + ".json").exists();
    }

    public boolean createServer() throws NoSuchFieldException, IllegalAccessException {
        Scanner in = new Scanner(System.in);
        Server server = new Server();
        System.out.println("You are creating a new Server. Please enter all necessary information.");
        for (String strField : Server.FIELDS) {
            Field field = Server.class.getDeclaredField(strField);
            field.setAccessible(true);
            boolean correct = false;
            do {
                // Check if 'webInterface' is false, then skip 'port', 'ipAddress', 'keystore' and 'keystorePass'
                if ((strField.equals("port") || strField.equals("ipAddress") || strField.equals("keystore")
                        || strField.equals("keystorePass")) && !server.isWebInterface()) {
                    correct = true;
                    continue;
                }

                System.out.println(strField.toUpperCase() + " [" + this.fieldDescriptions.get(strField) +"]:");
                Object input = in.nextLine();
                if (!this.fieldTests.containsKey(strField) || this.fieldTests.get(strField).test(input)) {
                    // Check if input is boolean
                    if (strField.equals("webInterface") || strField.equals("restartOnCrash")) {
                        if (input.equals("y")) input = true;
                        if (input.equals("n")) input = false;
                    } else if (strField.equals("port")) {
                        input = Integer.valueOf((String) input);
                    } else if (strField.equals("startArgs")) {
                        String replaced = ((String) input).replace("%jar", server.getJar());
                        input = replaced.split(" ");
                    }
                    System.out.println("-> Saved '" + strField.toUpperCase() + "'!");
                    field.set(server, input);
                    correct = true;
                } else {
                    System.out.println("Input '" + input + "' is not following the rules! Try again!");
                }
            } while (!correct);
        }
        System.out.println("-> You have entered following data");
        for (Field field : Server.class.getDeclaredFields()) {
            if (field.getName().equals("FIELDS") || field.getName().equals("uuid") || field.getName().equals("password"))
                continue;
            if (field.getName().equals("startArgs")) {
                field.setAccessible(true);
                List<String> argsList = Arrays.asList((String[]) field.get(server));
                System.out.println(" '" + field.getName().toUpperCase() + "': " + String.join(" ", argsList));
                continue;
            }
            field.setAccessible(true);
            System.out.println(" '" + field.getName().toUpperCase() + "': " + field.get(server));
        }
        System.out.print("Do you want to save this server? [yes (y), no (n)]: ");
        String input = in.nextLine();
        in.close();
        if (input.equals("y")) {
            String password = this.nextSessionId();
            server.setPassword(DigestUtils.sha256Hex(password));
            this.saveServer(server);
            System.out.printf("Saved server '%s' with uuid '%s' to\n'%s'", server.getName(), server.getUuid(),
                    Constants.SERVER_CONFIG_DIR + server.getName() + ".json\n");
            System.out.printf("Web interface password: '%s'", password);
            return true;
        } else {
            System.out.println("Server creation cancelled!");
            return false;
        }
    }

    private void saveServer(Server server) {
        try (Writer writer = new FileWriter(Constants.SERVER_CONFIG_DIR + server.getName() + ".json")) {
            this.gson.toJson(server, writer);
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void setupFieldDescriptions() {
        // Description for 'name' field
        this.fieldDescriptions.put("name", "lowercase, no spaces, unique");

        // Description for 'directory' field
        this.fieldDescriptions.put("directory", "absolute path");

        // Description for 'jar' field
        this.fieldDescriptions.put("jar", "absolute path, file name with '.jar'");

        // Description for 'startArgs' field
        this.fieldDescriptions.put("startArgs", "arguments to start the server, '%jar' for .jar file");

        // Description for 'webInterface' field
        this.fieldDescriptions.put("webInterface", "yes (y), no (n)");

        // Description for 'ipAddress' field
        this.fieldDescriptions.put("ipAddress", "valid IPv4 address");

        // Description for 'restartOnCrash' field
        this.fieldDescriptions.put("restartOnCrash", "yes (y), no (n)");

        // Description for 'port' field
        this.fieldDescriptions.put("port", "valid port in range of 1 - 65535");

        // Description for 'keystore' field
        this.fieldDescriptions.put("keystore", "Keystore file name (/.mcwrapper/ssl/<name>.jks)");

        // Description for 'keystorePass' field
        this.fieldDescriptions.put("keystorePass", "Keystore password");
    }

    private void setupFieldTests() {
        // Check if server named 'name' already exists and 'name' contains spaces
        this.fieldTests.put("name", i -> !this.checkServerExists((String) i) && !((String) i).contains(" "));

        // Check if 'directory' does exist
        this.fieldTests.put("directory", i -> new File((String) i).exists());

        // Check if 'jar' does exist
        this.fieldTests.put("jar", i -> new File((String) i).exists());

        // Check if 'startArgs' are no empty
        this.fieldTests.put("startArgs", i -> !((String) i).isEmpty());

        // Check if 'webInterface' is wanted
        this.fieldTests.put("webInterface", i -> i.equals("y") || i.equals("n"));

        // Check if 'ipAddress' is a valid IPv4 address
        this.fieldTests.put("ipAddress", i -> {
           Pattern pattern = Pattern.compile(Constants.IPADDRESS_PATTERN);
           Matcher matcher = pattern.matcher((String) i);
           return matcher.matches();
        });

        // Check if 'restartOnCrash' is wanted
        this.fieldTests.put("restartOnCrash", i -> i.equals("y") || i.equals("n"));

        // Check if 'port' is a valid port
        this.fieldTests.put("port", i -> Integer.valueOf((String) i) >= 0 && Integer.valueOf((String) i) <= 65535);

        // Check if 'keystore' does exist
        this.fieldTests.put("keystore", i -> new File(Constants.SSL_DIR + i + ".jks").exists());
    }

}
