package de.codehat.mcwrapper.web;

import de.codehat.mcwrapper.server.Server;
import de.codehat.mcwrapper.server.ServerManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;

@WebSocket
public class ConsoleWebSocketHandler {

    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String password = user.getUpgradeRequest().getParameterMap().get("password").get(0);
        if (DigestUtils.sha256Hex(password).equals(ServerManager.passwordHash)) {
            String username = "User" + Console.nextUserNumber++;
            System.out.println(username + " (" + user.getRemoteAddress().getAddress() + ") connected!");
            Console.userUsernameMap.put(user, username);
            Console.broadcastMessage("Server", "");
        } else {
            System.out.println("Unsuccessful login from: '" + user.getRemoteAddress().getAddress() + "'!");
            user.close();
        }
        //Console.broadcastMessage(sender = "Server", msg = (username + " joined the chat"));
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = Console.userUsernameMap.get(user);
        Console.userUsernameMap.remove(user);
        Console.broadcastMessage(sender = "Server", msg = ("-> '" + username + "' left the console"));
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        if (message.toLowerCase().startsWith("op") || message.toLowerCase().startsWith("pex")) return;
        try {
            if (message.toLowerCase().equals("!stop")) {
                ServerManager.restart = false;
                ServerManager.writer.write("stop\n");
                ServerManager.writer.flush();
                return;
            }
            ServerManager.writer.write(message + "\n");
            ServerManager.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Console.broadcastMessage(sender = Console.userUsernameMap.get(user), msg = message);
    }

}