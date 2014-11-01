/*
 * The MIT License
 *
 * Copyright 2014 Manuel Schmid.
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
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

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

    public static void main(String args[]) {

        // Default port
        int portNumber = 8000;
        if (args.length < 1) {
            System.out.println("Usage: java ChatServer <portNumber>\n"
                    + "Now using port number=" + portNumber);
        } else {
            portNumber = Integer.valueOf(args[0]);
        }

        // Open a server socket on the portNumber (default 8000)
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        // Create client socket for each connection
        while (true) {
            try {
                // Handle for new connection, put it into empty array-slot
                clientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClientThread(clientSocket, threads)).start();
                        break;
                    }
                }
                // Only when maxclients is reached
                if (i == maxClientsCount) {
                    PrintStream pStream = new PrintStream(clientSocket.getOutputStream());
                    pStream.println("Too many clients. Please try later.");
                    pStream.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}

class ClientThread extends Thread {

    protected String clientName = null;
    protected DataInputStream inStream = null;
    protected PrintStream outStream = null;
    protected Socket clientSocket = null;
    protected final ClientThread[] threads;
    protected int maxClientsCount;

    /**
     * Constructor
     *
     * @param clientSocket Sochet where the connection was accepted
     * @param threads
     */
    public ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    /**
     * Called when thread is started
     */
    public void run() {
        int maxClientsCount = this.maxClientsCount;
        ClientThread[] threads = this.threads;
        String name;
        try {
            // Create input and output streams for this client.
            inStream = new DataInputStream(clientSocket.getInputStream());
            outStream = new PrintStream(clientSocket.getOutputStream());

            // Choose name
            while (true) {
                outStream.println("Enter your name.");
                name = inStream.readLine().trim();
                if (name.indexOf('@') == -1) {
                    break;
                } else {
                    outStream.println("The name should not contain '@' character.");
                }
            }

            // Setting up name
            for (int i = 0; i < this.maxClientsCount; i++) {
                if (this.threads[i] != null && threads[i] == this) {
                    this.clientName = "@" + name;
                    break;
                }
            }

            // Broadcasts welcome message to all clients
            broadcastExceptMe("*** A new user " + name + " entered the chat room !!! ***");
            outStream.println("Welcome " + name + " to our chat room.\nTo leave enter /quit in a new line.");

            // Start conversation
            while (true) {
                String line = inStream.readLine();
                // TODO DECODE
                if (line.startsWith("/quit")) {
                    break;
                }
                // If the message is private sent it to the given client
                if (line.startsWith("@")) {
                    // words[0] == name of receiver
                    // words[1] == message
                    String[] words = line.split("\\s", 2);
                    if (words.length > 1 && words[1] != null) {
                        words[1] = words[1].trim();
                        if (!words[1].isEmpty()) {
                            sendPrivateMessage(words[0], words[1]);
                        }
                    }
                } else {
                    // Broadcast message to all other clients
                    broadcast("<" + name + "> " + line);
                }
            }
            broadcastExceptMe("*** The user " + name + " is leaving the chat room !!! ***");
            outStream.println("*** Bye " + name + " ***");

            disconnect();

            // Close the output stream, close input stream, close the socket.
            inStream.close();
            outStream.close();
            clientSocket.close();
        } catch (IOException e) {
        }
    }

    /**
     * Sends a message to all other clients
     *
     * @param message message to send
     */
    protected synchronized void broadcast(String message) {
        // TODO ENCODE
        for (int i = 0; i < this.maxClientsCount; i++) {
            if (threads[i] != null && threads[i].clientName != null) {
                threads[i].outStream.println(message);
            }
        }
    }

    /**
     * Sends a welcome message to all other clients other than the current
     * client
     *
     * @param message message to send
     */
    protected synchronized void broadcastExceptMe(String message) {
        // TODO ENCODE
        for (int i = 0; i < this.maxClientsCount; i++) {
            if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
                threads[i].outStream.println(message);
            }
        }
    }

    protected synchronized void sendPrivateMessage(String receiver, String message) {
        // TODO ENCODE
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null
                    && threads[i] != this
                    && threads[i].clientName != null
                    && threads[i].clientName.equals(receiver)) {
                threads[i].outStream.println("<" + this.clientName + "> " + message);
                /*
                 * Echo this message to let the client know the private
                 * message was sent.
                 */
                this.outStream.println(">" + this.clientName + "> " + message);
                break;
            }
        }
    }

    /**
     * Clean up. Set the current thread variable to null so that a new client
     * could be accepted by the server.
     */
    protected synchronized void disconnect() {
        for (int i = 0; i < this.maxClientsCount; i++) {
            if (this.threads[i] == this) {
                this.threads[i] = null;
            }
        }
    }
}

