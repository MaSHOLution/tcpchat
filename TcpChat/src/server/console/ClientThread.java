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
package server.console;

import common.networking.Packet;
import common.networking.PacketType;
import common.networking.packets.*;
import security.basics.CryptoBasics;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import logging.Counters;
import security.cryptography.*;
import static server.console.ChatServer.*;

public final class ClientThread extends Thread {

    protected String clientName = null;
    protected ObjectInputStream inStream = null;
    protected ObjectOutputStream outStream = null;
    protected Socket clientSocket = null;
    protected int maxClientsCount;
    protected SocketAddress ip;
    protected String name;
    protected static final String quitString = "/quit";
    protected EncryptionMethod encMethod = CryptoBasics.makeEncryptionObject();
    protected final String sessionId = encMethod.sessionId;

    /**
     * Constructor
     *
     * @param clientSocket Sochet where the connection was accepted
     */
    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.maxClientsCount = threads.length;
        this.ip = clientSocket.getRemoteSocketAddress();
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
            ConnectPacket cPacket = this.setName();
            if (cPacket != null) {
                this.linkNameToThread(cPacket.getName());

                // Broadcasts welcome message to all clients
                this.broadcastExceptMe("*** User \"" + name + "\" joined ***");
                this.sendPacket(new InfoPacket("Welcome " + name + " to our chat room.\n")); //To leave, enter \"" + this.quitString + "\" in a new line.");
                logControl.log(logGeneral, Level.INFO, name + " joined");

                // Start conversation
                while (true) {
                    Packet packet = this.readPacket(inStream);
                    PacketType ptype = packet.getIdentifier();
                    if(ptype == PacketType.DISCONNECT){
                        break;
                    } else if (ptype == PacketType.PRIVATEMESSAGE) {
                        // If the message is private sent it to the given client 
                        // words[0] == name of receiver
                        // words[1] == message
                        PrivateMessagePacket pmPacket = (PrivateMessagePacket) packet;
                        // TODO PM PACKAGE
                        String[] words = pmPacket.getMessage().split("\\s", 2);
                        if (words.length > 1 && words[1] != null) {
                            words[1] = words[1].trim();
                            if (!words[1].isEmpty()) {
                                this.sendPrivateMessage(words[0], pmPacket);
                            }
                        }
                    } else {
                        // Broadcast message to all other clients
                        // this.broadcast("<" + name + "> " + line);
                        this.broadcast((GroupMessagePacket) packet);
                    }
                }

                // Tell every client, that the current client is going offline
                this.broadcastExceptMe("*** " + name + " has left ***");
                this.sendPacket(new DisconnectPacket());

                // Remove client from threads array and close connections
                disconnect(false);
            } else {
                disconnect(true);
            }

        } catch (IOException e) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.name + "): " + e.getMessage());
        }
    }

    /**
     * Creates input and output streams for this client
     *
     * @throws IOException
     */
    protected synchronized void initStreams() throws IOException {
        this.inStream = new ObjectInputStream(this.clientSocket.getInputStream());
        this.outStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
    }

    /**
     * Sends a message to all clients
     *
     * @param message message to send
     */
    protected synchronized void broadcast(String message) {
        for (int i = 0; i < this.maxClientsCount; i++) {
            if (threads[i] != null && threads[i].clientName != null) {
                this.sendMessage(new GroupMessagePacket(message), threads[i]);
            }
        }
        logControl.log(logGeneral, Level.INFO, "GM #" + Counters.Totals.Messages.gmTotal + " from " + this.clientName);
        Counters.gm();
    }
    
    /**
     * Sends a message to all clients
     *
     * @param gmPacket GroupMessagePacket to send
     */
    protected synchronized void broadcast(GroupMessagePacket gmPacket) {
        for (int i = 0; i < this.maxClientsCount; i++) {
            if (threads[i] != null && threads[i].clientName != null) {
                this.sendMessage(gmPacket, threads[i]);
            }
        }
        logControl.log(logGeneral, Level.INFO, "GM #" + Counters.Totals.Messages.gmTotal + " from " + this.clientName);
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
                sendMessage(new GroupMessagePacket(message), threads[i]);
            }
        }
        // Counters.gm(); is normally no group but system shoutout
    }

    /**
     * Sends a private privatePacket to one client
     *
     * @param receiver name of the receiver
     * @param privatePacket privatePacket to send
     */
    protected synchronized void sendPrivateMessage(String receiver, PrivateMessagePacket privatePacket) {

        // Check if sender wants to send privatePacket to himself
        if (receiver.equals(this.clientName)) {
            this.sendInfo("You can't send private messages to yourself");
            logControl.log(logGeneral, Level.INFO, this.clientName + " wanted to send himself a private message");
        } else {
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null
                        && threads[i] != this
                        && threads[i].clientName != null
                        && threads[i].clientName.equals(receiver)) {

                    // Send privatePacket to receiver
                    this.sendMessage(privatePacket, threads[i]);

                    // Send privatePacket to sender
                    this.sendPacket(privatePacket);
                    Counters.pm();
                    logControl.log(logGeneral, Level.INFO, "PM #" + Counters.Totals.Messages.pmTotal + " from " + this.clientName + " to " + receiver);
                    break;
                }
            }
        }
    }

    /**
     * Writes a packet to a specific PrintStream
     *
     * @param packet stands for itself
     * @return result of sending
     */
    protected synchronized boolean sendPacket(Packet packet) {
        try {
            Counters.connection();
            this.outStream.writeObject(packet);
            return true;
        } catch (IOException e) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.name + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Writes a packet to a specific PrintStream
     *
     * @param packet stands for itself
     * @param thread ClientThread to send packet to
     * @return result of sending
     */
    protected synchronized boolean sendMessage(Packet packet, ClientThread thread) {
        try {
            Counters.connection();
            thread.outStream.writeObject(packet);
            return true;
        } catch (Exception e) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.name + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Writes a packet to a specific PrintStream
     *
     * @param packet stands for itself
     * @param thread ClientThread to send packet to
     * @return result of sending
     */
    protected synchronized boolean sendInfo(String message) {
        try {
            Counters.connection();
            this.outStream.writeObject(new InfoPacket(message));
            return true;
        } catch (Exception e) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.name + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads a message from a specific inStream
     *
     * @param inStream
     * @return
     */
    protected synchronized Packet readPacket(ObjectInputStream inStream) {
        try {
            Packet readPacket = (Packet) inStream.readObject();
            Counters.connection();
            return readPacket;
        } catch (IOException | ClassNotFoundException ex) {
           // TODO handle exception
        }
        return null;
    }

    /**
     * Let the user choose a nickname
     *
     * @return name
     * @throws java.io.IOException
     */
    protected ConnectPacket setName() throws IOException {
        ConnectPacket clientAnswer;
        // TODO make packet secure
        while (true) {
            clientAnswer = (ConnectPacket) this.readPacket(inStream);
            if (clientAnswer.getIdentifier() == PacketType.DISCONNECT) {
                return null;
            } else if (clientAnswer.getName().contains("@")) {
                this.sendPacket(new InfoPacket("The name needn't contain '@' character."));
            } else {
                this.name = clientAnswer.getName();
                break;
            }
        }
        return (ConnectPacket) clientAnswer;
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
                logControl.log(logConnection, Level.INFO, this.ip + ": is now " + name);
                break;
            }
        }
    }

    /**
     * Disconnects the client
     *
     * @param closeOnly close streams without deleting client from threads
     * @throws java.io.IOException
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
            logControl.log(logConnection, Level.INFO, this.ip + ": " + this.name + " has disconnected");
        }
        Counters.disconnect();
    }
}
