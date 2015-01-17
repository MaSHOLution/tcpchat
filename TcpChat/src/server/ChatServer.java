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
package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import logging.Counters;
import logging.CustomLogging;
import logging.LoggingController;
import logging.enums.LogName;
import logging.enums.LogPath;
import static server.ChatServer.*;

/**
 * Class ChatServer initializes threads and accepts new clients
 */

    // Sockets
    protected static ServerSocket serverSocket = null;
    protected static Socket clientSocket = null;
public final class ChatServer {

    // Setting up client
    protected static final int maxClientsCount = 10;
    protected static final ClientThread[] threads = new ClientThread[maxClientsCount];

    // Logging
    protected static Logger logConnection = null;
    protected static Logger logException = null;
    protected static Logger logGeneral = null;

    protected static LoggingController logControl = null;

    public static void main(String args[]) {

        // Default values
        int portNumber = 0;
        boolean loggingEnabled = false;
        boolean showOnConsole = false;
        boolean init = false;

        // Switch command line arguments
        switch (args.length) {
            case 2:
                portNumber = Integer.parseInt(args[0]);
                if (args[1].equals("yes")) {
                    loggingEnabled = true;
                }
                init = true;
                break;
            default:
                System.out.println("Usage: java ChatServer <portNumber> <logging yes/NO>");
        }

        // Check if everything is set up successfully
        if (init) {
            // Setting up LoggingController
            logControl = new LoggingController(loggingEnabled, showOnConsole);
            initLoggers();
            System.out.println("Server started");
            logControl.log(logGeneral, Level.INFO, "Server started on port " + portNumber);

            // Open a server socket on the portNumber (default 8000)
            try {
                serverSocket = new ServerSocket(portNumber);

                // Adding shutdown handle
                Runtime.getRuntime().addShutdownHook(new ShutdownHandle());

                // Create client socket for each connection
                while (true) {
                    try {
                        // Handle for new connection, put it into empty array-slot
                        clientSocket = serverSocket.accept();
                        Counters.connection();
                        int i;
                        for (i = 0; i < maxClientsCount; i++) {
                            if (threads[i] == null) {
                                (threads[i] = new ClientThread(clientSocket, logControl)).start();
                                logControl.log(logConnection, Level.INFO, clientSocket.getRemoteSocketAddress() + ": accepted, thread started");
                                Counters.login();
                                break;
                            }
                        }

                        // Only when maxclients is reached
                        if (i == maxClientsCount) {
                            PrintStream pStream = new PrintStream(clientSocket.getOutputStream());
                            pStream.println("Too many clients. Please try later.");
                            pStream.close();
                            Counters.connection();
                            logControl.log(logConnection, Level.INFO, clientSocket.getRemoteSocketAddress() + ": rejected, server is full");
                            clientSocket.close();
                        }
                    } catch (IOException e) {
                        System.out.println(e);
                        logControl.log(logException, Level.SEVERE, clientSocket.getRemoteSocketAddress() + ": error while logging in (" + e.getMessage() + ")");
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
                logControl.log(logException, Level.SEVERE, "Could not open Server Socket");
                logControl.log(logException, Level.SEVERE, "Exiting Server");
                logControl.log(logGeneral, Level.SEVERE, "Exiting Server");
            }
        }

    }

    /**
     * Initializes loggers with LoggingController
     */
    protected static void initLoggers() {
        logConnection = logControl.create(LogName.SERVER, LogPath.CONNECTION);
        logException = logControl.create(LogName.SERVER, LogPath.EXCEPTION);
        logGeneral = logControl.create(LogName.SERVER, LogPath.GENERAL);
    }
}

class ShutdownHandle extends Thread {

    @Override
    public void run() {
        logControl.log(logGeneral, Level.INFO, "*** SERVER IS GOING DOWN ***");
        logControl.log(logConnection, Level.INFO, "*** SERVER IS GOING DOWN ***");
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && threads[i].clientName != null) {
                sendMessage(threads[i].outStream, "*** SERVER IS GOING DOWN ***");
                sendMessage(threads[i].outStream, "*** Bye " + threads[i].clientName + " ***");
            }
        }
        // TODO Logger schließen
        CustomLogging.resetAllLoggers();
    }

    /**
     * Writes a message to a specific PrintStream
     *
     * @param printStream stream to write message to
     * @param message stands for itself
     */
    private boolean sendMessage(PrintStream printStream, String message) {
        // TODO Encrypt
        try {
            Counters.connection();
            printStream.println(message);
            return true;
        } catch (Exception e) {
            ChatServer.logControl.log(CustomLogging.get(LogName.SERVER, LogPath.EXCEPTION), Level.INFO, e.getMessage());
            return false;
        }
    }
}
