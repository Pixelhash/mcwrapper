package de.codehat.mcwrapper.web;

import de.codehat.mcwrapper.server.ServerManager;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;

@WebSocket
public class ConsoleWebSocketHandler {

    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String username = "User" + Console.nextUserNumber++;
        Console.userUsernameMap.put(user, username);
        Console.broadcastMessage(sender = "Server", msg = (username + " joined the chat"));
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = Console.userUsernameMap.get(user);
        Console.userUsernameMap.remove(user);
        Console.broadcastMessage(sender = "Server", msg = (username + " left the chat"));
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        try {
            ServerManager.writer.write(message + "\n");
            ServerManager.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Console.broadcastMessage(sender = Console.userUsernameMap.get(user), msg = message);
    }

}