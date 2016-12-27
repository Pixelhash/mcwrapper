package de.codehat.mcwrapper.server;

import java.util.UUID;

public class Server {

    public static final String[] FIELDS = { "name", "directory", "jar", "startArgs", "webInterface",
            "ipAddress", "port", "restartOnCrash" };

    private String name;
    private UUID uuid = UUID.randomUUID();
    private String directory;
    private String jar;
    private String[] startArgs;
    private String ipAddress = "0.0.0.0";
    private String password;
    private boolean webInterface;
    private boolean restartOnCrash = true;
    private int port = 23843;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getJar() {
        return jar;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public String[] getStartArgs() {
        return startArgs;
    }

    public void setStartArgs(String[] startArgs) {
        this.startArgs = startArgs;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isWebInterface() {
        return webInterface;
    }

    public void setWebInterface(boolean webInterface) {
        this.webInterface = webInterface;
    }

    public boolean isRestartOnCrash() {
        return restartOnCrash;
    }

    public void setRestartOnCrash(boolean restartOnCrash) {
        this.restartOnCrash = restartOnCrash;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
