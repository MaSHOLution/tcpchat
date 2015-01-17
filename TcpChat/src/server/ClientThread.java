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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import logging.Counters;
import logging.LoggingController;
import static server.ChatServer.*;

class ClientThread extends Thread {

    protected String clientName = null;
    protected DataInputStream inStream = null;
    protected PrintStream outStream = null;
    protected Socket clientSocket = null;
    protected int maxClientsCount;
    protected SocketAddress ip;
    protected String name;
    protected LoggingController logControl = null;

    /**
     * Constructor
     *
     * @param clientSocket Sochet where the connection was accepted
     * @param threads
     */
    public ClientThread(Socket clientSocket, LoggingController logControl) {
        this.clientSocket = clientSocket;
        this.maxClientsCount = threads.length;
        this.ip = clientSocket.getRemoteSocketAddress();
        this.logControl = logControl;
    }

    /**
     * Called when thread is started
     */
    @Override
    public void run() {

        try {
            // Setting up streams
            this.initStreams();

            // Setting up name
            if (this.setName()) {
                this.linkNameToThread(name);

                // Broadcasts welcome message to all clients
                this.broadcastExceptMe("*** User \"" + name + "\" joined ***");
                this.sendMessage(this.outStream, "Welcome " + name + " to our chat room.\nTo leave, enter \"/quit\" in a new line.");
                this.logControl.log(logGeneral, Level.INFO, name + " joined");

                // Start conversation
                while (true) {
                    String line = this.readMessage(inStream);
                    if (line.equals("/quit")) {
                        break;
                    } else if (line.startsWith("*** Bye")) {
                        this.sendMessage(this.outStream, "*** WARNING: String not allowed ***");
                        this.logControl.log(logGeneral, Level.WARNING, name + " wanted to send \"*** Bye\", rejected message");
                    } else if (line.startsWith("@")) {
                        // If the message is private sent it to the given client 
                        // words[0] == name of receiver
                        // words[1] == message
                        String[] words = line.split("\\s", 2);
                        if (words.length > 1 && words[1] != null) {
                            words[1] = words[1].trim();
                            if (!words[1].isEmpty()) {
                                this.sendPrivateMessage(words[0], words[1]);
                            }
                        }
                    } else {
                        // Broadcast message to all other clients
                        this.broadcast("<" + name + "> " + line);
                    }
                }

                // Tell every client, that the current client is going offline
                this.broadcastExceptMe("*** " + name + " has left ***");
                this.sendMessage(this.outStream, "*** Bye " + name + " ***");

                // Remove client from threads array and close connections
                disconnect(false);
            } else {
                disconnect(true);
            }

        } catch (IOException e) {
            // TODO Exception-Handling
            this.logControl.log(logException, Level.INFO, this.ip + "(" + this.name + "): " + e.getMessage());
        }
    }

    /**
     * Creates input and output streams for this client
     *
     * @throws IOException
     */
    protected synchronized void initStreams() throws IOException {
        this.inStream = new DataInputStream(this.clientSocket.getInputStream());
        this.outStream = new PrintStream(this.clientSocket.getOutputStream());
    }

    /**
     * Sends a message to all clients
     *
     * @param message message to send
     */
    protected synchronized void broadcast(String message) {
        for (int i = 0; i < this.maxClientsCount; i++) {
            if (threads[i] != null && threads[i].clientName != null) {
                this.sendMessage(threads[i].outStream, message);
            }
        }
        this.logControl.log(logGeneral, Level.INFO, "GM #" + Counters.Totals.Messages.gmTotal + " from " + this.clientName);
        Counters.gm();
    }

    /**
     * Sends a message to all other clients except the current client (this)
     *
     * @param message message to send
     */
    protected synchronized void broadcastExceptMe(String message) {
        for (int i = 0; i < this.maxClientsCount; i++) {

            if (threads[i] != null
                    && threads[i].clientName != null
                    && threads[i] != this) {
                this.sendMessage(threads[i].outStream, message);
            }
        }
        // Counters.gm(); is normally no group but system shoutout
    }

    /**
     * Sends a private message to one client
     *
     * @param receiver name of the receiver
     * @param message message to send
     */
    protected synchronized void sendPrivateMessage(String receiver, String message) {

        // Check if sender wants to send message to himself
        if (receiver.equals(this.clientName)) {
            this.outStream.println("You can't send private messages to yourself");
            this.logControl.log(logGeneral, Level.INFO, this.clientName + " wanted to send himself a private message");
        } else {
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null
                        && threads[i] != this
                        && threads[i].clientName != null
                        && threads[i].clientName.equals(receiver)) {

                    // Send message to receiver
                    this.sendMessage(threads[i].outStream, "<" + this.clientName + "> " + message);

                    // Send message to sender
                    this.sendMessage(this.outStream, ">" + receiver + "> " + message);
                    Counters.pm();
                    this.logControl.log(logGeneral, Level.INFO, "PM #" + Counters.Totals.Messages.pmTotal + " from " + this.clientName + " to " + receiver);
                    break;
                }
            }
        }
    }

    /**
     * Writes a message to a specific PrintStream
     *
     * @param printStream stream to write message to
     * @param message stands for itself
     */
    protected synchronized boolean sendMessage(PrintStream printStream, String message) {
        // TODO Encrypt
        try {
            Counters.connection();
            printStream.println(message);
            return true;
        } catch (Exception e) {
            this.logControl.log(logException, Level.INFO, this.ip + "(" + this.name + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads a message to a specific PrintStream
     *
     * @param printStream stream to write message to
     * @param message stands for itself
     */
    protected synchronized String readMessage(DataInputStream inStream) throws IOException {
        // TODO Decrypt
        String readLine = inStream.readLine();
        Counters.connection();
        return readLine;
    }

    /**
     * Let the user choose a nickname
     *
     * @return name
     */
    protected boolean setName() throws IOException {
        String name;
        while (true) {
            this.sendMessage(this.outStream, "Please enter a nickname:");
            name = this.readMessage(inStream);
            if (name.equals("/quit")) {
                return false;
            } else if (name.contains("@")) {
                this.sendMessage(this.outStream, "The name needn't contain '@' character.");
            } else {
                this.name = name;
                break;
            }
        }
        return true;
    }

    /**
     * Adds name to clientThread at index of this client
     *
     * @param name name of the client
     */
    protected void linkNameToThread(String name) {
        for (int i = 0; i < this.maxClientsCount; i++) {
            if (threads[i] != null && threads[i] == this) {
                this.clientName = "@" + name;
                this.logControl.log(logConnection, Level.INFO, this.ip + ": is now " + name);
                break;
            }
        }
    }

    /**
     * Disconnects the client
     */
    protected synchronized void disconnect(boolean closeOnly) throws IOException {
        if (!closeOnly) {
            for (int i = 0; i < this.maxClientsCount; i++) {
                if (threads[i] == this) {
                    threads[i] = null;
                }
            }
        }

        // Close streams and socket
        this.inStream.close();
        this.outStream.close();
        this.clientSocket.close();

        if (!closeOnly) {
            this.logControl.log(logConnection, Level.INFO, this.ip + ": " + this.name + " has disconnected");
        }
        Counters.disconnect();
    }
}
