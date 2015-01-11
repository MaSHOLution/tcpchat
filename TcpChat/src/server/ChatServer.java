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

import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import logging.*;

/**
 * Class ChatServer initializes threads and accepts new clients
 */
public class ChatServer {

    // Sockets
    protected static ServerSocket serverSocket = null;
    protected static Socket clientSocket = null;

    // Setting up client
    protected static final int maxClientsCount = 10;
    protected static final ClientThread[] threads = new ClientThread[maxClientsCount];

    // Logging
    private static final Logger logConnection = CustomLogger.create(LogName.SERVER, LogPath.CONNECTION);
    private static final Logger logException = CustomLogger.create(LogName.SERVER, LogPath.EXCEPTION);
    private static final Logger logGeneral = CustomLogger.create(LogName.SERVER, LogPath.GENERAL);

    public static void main(String args[]) {

        // Default port
        int portNumber = 8000;
        if (args.length < 1) {
            System.out.println("Usage: java ChatServer <portNumber>\n"
                    + "Now using port number " + portNumber);
        } else {
            portNumber = Integer.valueOf(args[0]);
        }

        System.out.println("Server started");
        logGeneral.log(Level.INFO, "Server started on port " + portNumber);

        // Open a server socket on the portNumber (default 8000)
        try {
            serverSocket = new ServerSocket(portNumber);

            // Create client socket for each connection
            while (true) {
                try {
                    // Handle for new connection, put it into empty array-slot
                    clientSocket = serverSocket.accept();
                    Counters.connection();
                    int i;
                    for (i = 0; i < maxClientsCount; i++) {
                        if (threads[i] == null) {
                            (threads[i] = new ClientThread(clientSocket, threads)).start();
                            logConnection.log(Level.INFO, clientSocket.getRemoteSocketAddress() + ": accepted, thread started");
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
                        logConnection.log(Level.INFO, clientSocket.getRemoteSocketAddress() + ": rejected, server is full");
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.out.println(e);
                    logException.log(Level.SEVERE, clientSocket.getRemoteSocketAddress() + ": error while logging in (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println(e);
            logException.log(Level.SEVERE, "Could not open Server Socket");
            logException.log(Level.SEVERE, "Exiting Server");
            logGeneral.log(Level.SEVERE, "Exiting Server");
        }
    }
}
