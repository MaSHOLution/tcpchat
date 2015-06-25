/*
 * The MIT License
 *
 * Copyright 2015 Manuel Schmid.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.mash1t.chat.server.console;

import de.mash1t.chat.core.RoleType;
import de.mash1t.networklib.packets.KickPacket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.mash1t.chat.logging.*;
import de.mash1t.networklib.AbstractNetworkProtocol;
import de.mash1t.networklib.methods.NetworkProtocolType;
import de.mash1t.chat.config.ConfigController;
import de.mash1t.chat.config.ConfigParam;
import java.util.Scanner;

/**
 * Class ChatServer initializes threads and accepts new clients
 */
public final class ChatServer {

    // Setting up client
    // maxClientsCount = 0 means infinite clients
    protected static int maxClientsCount;
    protected static final List<ClientThread> threads = new ArrayList<>();
    protected static List<String> userList = new ArrayList<>();

    // Logging
    protected static Logger logConnection;
    protected static Logger logException;
    protected static Logger logGeneral;
    protected static LoggingController logControl = null;
    protected static NetworkProtocolType nwpType = NetworkProtocolType.TCP;

    // Config controller
    private static final ConfigController conf = new ConfigController("ServerConfig.ini");

    /**
     * Main method for server
     *
     * @param args
     */
    public static void main(String args[]) {
        System.out.println("Reading configuration file");
        if (conf.readConfigFile() && conf.validateConfig()) {
            runServer();
        } else {
            System.out.print("Server configuration file not found/readable/valid \nRestore default configuration? (y/n): ");
            Scanner sc = new Scanner(System.in);
            if (sc.nextLine().equals("y")) {
                if (conf.makeDefaultFile()) {
                    System.out.println("Restored default configuration");
                    conf.readConfigFile();
                    runServer();
                } else {
                    System.out.println("Error: Please check permissions");
                    System.out.println("Aborting Server");
                }
            } else {
                System.out.println("Aborting Server");
            }
        }
    }

    private static void runServer() {

        int portNumber = conf.getConfigValueInt(ConfigParam.Port);
        // maxClientsCount = 0 means infinite clients
        maxClientsCount = conf.getConfigValueInt(ConfigParam.MaxClients);
        boolean loggingEnabled = conf.getConfigValueBoolean(ConfigParam.LogFiles);
        boolean showOnConsole = conf.getConfigValueBoolean(ConfigParam.LogConsole);
        boolean cleanLogsOnStartup = conf.getConfigValueBoolean(ConfigParam.CleanLogsOnStartup);

        // Setting up LoggingController
        logControl = new LoggingController(loggingEnabled, showOnConsole, cleanLogsOnStartup);
        initLoggers();
        System.out.println("Server started on port " + portNumber);
        logControl.log(logGeneral, Level.INFO, "Server started on port " + portNumber);

        // Open a server socket on the portNumber (default 8000)
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);

            // Adding shutdown handle
            Runtime.getRuntime().addShutdownHook(new ShutdownHandle());

            Socket clientSocket = null;

            // Create client socket for each connection
            while (true) {
                try {
                    // Handle for new connection, put it into empty array-slot
                    clientSocket = serverSocket.accept();
                    Counters.connection();
                    // maxClientsCount = 0 means infinite clients
                    if (threads.size() < maxClientsCount || maxClientsCount == 0) {
                        ClientThread clientThread = new ClientThread(clientSocket);
                        threads.add(clientThread);
                        clientThread.start();
                        logControl.log(logConnection, Level.INFO, clientSocket.getRemoteSocketAddress() + ": accepted, thread started");
                        Counters.login();
                    } else {
                        // Only when maxclients is reached        
                        RejectionThread fThread = new RejectionThread(clientSocket);
                        fThread.start();
                    }
                } catch (IOException ex) {
                    logControl.log(logException, Level.SEVERE, "Could not start thread for " + clientSocket.getInetAddress().toString() + ": " + ex.getMessage());
                    de.mash1t.chat.logging.Counters.exception();
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
            logControl.log(logException, Level.SEVERE, "Could not open Server Socket");
            logControl.log(logException, Level.SEVERE, "Exiting Server");
            logControl.log(logGeneral, Level.SEVERE, "Exiting Server");
            de.mash1t.chat.logging.Counters.exception();
        }
    }

    /**
     * Initializes loggers with LoggingController
     */
    protected static void initLoggers() {
        logConnection = logControl.create(RoleType.Server, LogPath.CONNECTION);
        logException = logControl.create(RoleType.Server, LogPath.EXCEPTION);
        logGeneral = logControl.create(RoleType.Server, LogPath.GENERAL);
    }

    /**
     * Getter for the userlist
     *
     * @return
     */
    public static List<String> getUserList() {
        return userList;

    }
}

class ShutdownHandle extends Thread {

    @Override
    public void run() {

        System.out.println("Shutting down Server");

        ChatServer.logControl.log(ChatServer.logGeneral, Level.INFO, "*** SERVER IS GOING DOWN ***");
        ChatServer.logControl.log(ChatServer.logConnection, Level.INFO, "*** SERVER IS GOING DOWN ***");

        // Send closing of server to all clients
        for (ClientThread thread : ChatServer.threads) {
            if (thread.state == ConnectionState.Online) {
                AbstractNetworkProtocol.send(new KickPacket("*** SERVER IS GOING DOWN ***"), thread, ChatServer.nwpType);
            }
        }

        // Close loggers
        ChatServer.logControl.closeLoggers();

        System.out.println("Shut down successfully");
    }
}
