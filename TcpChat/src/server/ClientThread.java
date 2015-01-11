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
    @Override
    public void run() {

        try {
            // Create input and output streams for this client.
            this.inStream = new DataInputStream(this.clientSocket.getInputStream());
            this.outStream = new PrintStream(this.clientSocket.getOutputStream());

            // Setting up name
            String name = this.setName();
            this.linkNameToThread(name);

            // Broadcasts welcome message to all clients
            broadcastExceptMe("*** A new user " + name + " entered the chat room !!! ***");
            this.outStream.println("Welcome " + name + " to our chat room.\nTo leave, enter \"/quit\" in a new line.");

            // Start conversation
            while (true) {
                String line = this.inStream.readLine();
                // TODO DECODE
                if (line.startsWith("/quit")) {
                    break;
                } else if (line.startsWith("*** Bye")) {
                    this.outStream.println("*** WARNING: String not allowed ***");
                } else if (line.startsWith("@")) {
                    // If the message is private sent it to the given client 
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
            broadcastExceptMe("*** " + name + " has left ***");
            this.outStream.println("*** Bye " + name + " ***");

            disconnect();

            // Close the output stream, close input stream, close the socket.
            this.inStream.close();
            this.outStream.close();
            this.clientSocket.close();
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
     * Sends a message to all other clients except the current client
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

    /**
     * Sends a private message to one client
     *
     * @param receiver name of the receiver
     * @param message message to send
     */
    protected synchronized void sendPrivateMessage(String receiver, String message) {
        // TODO ENCODE
        for (int i = 0; i < maxClientsCount; i++) {
            if (this.threads[i] != null
                    && this.threads[i] != this
                    && this.threads[i].clientName != null
                    && this.threads[i].clientName.equals(receiver)) {
                this.threads[i].outStream.println("<" + this.clientName + "> " + message);
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
     * Let the user choose a nickname
     *
     * @return name
     */
    protected String setName() throws IOException {

        String name;
        while (true) {
            this.outStream.println("Please enter a nickname:");
            name = this.inStream.readLine().trim();
            if (name.contains("@")) {
                this.outStream.println("The name needn't contain '@' character.");
            } else {
                break;
            }
        }

        return name;
    }

    /**
     * Adds name to clientThread at index of this client
     *
     * @param name name of the client
     */
    protected void linkNameToThread(String name) {
        for (int i = 0; i < this.maxClientsCount; i++) {
            if (this.threads[i] != null && this.threads[i] == this) {
                this.clientName = "@" + name;
                break;
            }
        }
    }

    /**
     * Disconnect the client, set slot in clientArray free for new client
     */
    protected synchronized void disconnect() {
        for (int i = 0; i < this.maxClientsCount; i++) {
            if (this.threads[i] == this) {
                this.threads[i] = null;
            }
        }
    }
}
