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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import logging.Counters;
import security.cryptography.*;
import static server.console.ChatServer.*;

/**
 * Class for a seperate thread for a client
 *
 * @author Manuel Schmid
 */
public final class ClientThread extends Thread {

    protected String clientName = null;
    protected ObjectInputStream inStream = null;
    protected ObjectOutputStream outStream = null;
    protected Socket clientSocket = null;
    protected SocketAddress ip;
    protected EncryptionMethod encMethod = CryptoBasics.makeEncryptionObject();
    protected final String sessionId = encMethod.sessionId;

    /**
     * Constructor
     *
     * @param clientSocket Sochet where the connection was accepted
     */
    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
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
                this.broadcastUserList(false);
                this.broadcastExceptMe(new InfoPacket("*** User \"" + this.clientName + "\" joined ***"));
                this.send(new InfoPacket("Welcome " + this.clientName + " to our chat room.\n")); //To leave, enter \"" + this.quitString + "\" in a new line.");
                logControl.log(logGeneral, Level.INFO, this.clientName + " joined");

                // Start conversation
                while (true) {
                    // TODO handle null return
                    Packet packet = this.read();
                    PacketType ptype = packet.getIdentifier();
                    if (ptype == PacketType.DISCONNECT) {
                        break;
                    } else if (ptype == PacketType.PM) {
                        // Send private message
                        this.forwardPrivateMessage((PrivateMessagePacket) packet);
                    } else {
                        // Broadcast message to all other clients
                        this.broadcast((GroupMessagePacket) packet);
                    }
                }

                // Tell every client, that the current client is going offline
                this.broadcastExceptMe(new InfoPacket("*** User \"" + this.clientName + "\" has left ***"));
                this.send(new DisconnectPacket());

                // Remove client from threads array and close connections
                disconnect(false);

                this.broadcastUserList(true);
            } else {
                disconnect(true);
            }

        } catch (IOException e) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + "): " + e.getMessage());
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
        for (ClientThread thread : threads) {
            if (thread.clientName != null) {
                this.send(new GroupMessagePacket(message, this.clientName), thread);
            }
        }
        logControl.log(logGeneral, Level.INFO, "GM #" + Counters.Totals.Messages.gmTotal + " from " + this.clientName);
        Counters.gm();
    }

    /**
     * Sends a message to all clients
     *
     * @param packet Packet to send
     */
    protected synchronized void broadcast(Packet packet) {
        for (ClientThread thread : threads) {
            if (thread.clientName != null) {
                this.send(packet, thread);
            }
        }
        logControl.log(logGeneral, Level.INFO, "GM #" + Counters.Totals.Messages.gmTotal + " from " + this.clientName);
        Counters.gm();
    }

    /**
     * Sends a message to all other clients except the current client (this)
     *
     * @param packet
     */
    protected synchronized void broadcastExceptMe(Packet packet) {
        for (ClientThread thread : threads) {

            if (thread.clientName != null && thread != this) {
                ClientThread.this.send(packet, thread);
            }
        }
        // Counters.gm(); is normally no group but system shoutout
    }

    /**
     * Sends a private privatePacket to one client
     *
     * @param privatePacket privatePacket to send
     * @return message send status
     */
    protected synchronized boolean forwardPrivateMessage(PrivateMessagePacket privatePacket) {
        String receiver = privatePacket.getReceiver();
        // Check if sender wants to send privatePacket to himself
        if (receiver.equals(this.clientName)) {
            this.send(new InfoPacket("You can't send private messages to yourself"));
            logControl.log(logGeneral, Level.INFO, this.clientName + " wanted to send himself a private message");
            return true;
        } else {
            for (ClientThread thread : threads) {
                if (thread != this
                        && thread.clientName != null
                        && thread.clientName.equals(receiver)) {

                    // Send privatePacket to receiver
                    this.send(privatePacket, thread);

                    // Send privatePacket to sender
                    this.send(privatePacket);
                    Counters.pm();
                    logControl.log(logGeneral, Level.INFO, "PM #" + Counters.Totals.Messages.pmTotal + " from " + this.clientName + " to " + receiver);
                    return true;
                }
            }
        }
        // Receiver has not been found / is not online
        // TODO Handle asynchronous messages/connections
        this.send(new InfoPacket("Message could not be delivered, reason: \"" + receiver + "\" is not online"));
        Counters.pm();
        Counters.pmFailed();
        logControl.log(logGeneral, Level.INFO, "PM #" + Counters.Totals.Messages.pmTotal + " from " + this.clientName + " failed: " + receiver + " is not online");
        return false;
    }

    /**
     * Writes a packet to a specific PrintStream
     *
     * @param packet stands for itself
     * @return result of sending
     */
    protected synchronized boolean send(Packet packet) {
        try {
            Counters.connection();
            this.outStream.writeObject(packet);
            return true;
        } catch (IOException e) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + "): " + e.getMessage());
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
    protected synchronized boolean send(Packet packet, ClientThread thread) {
        try {
            Counters.connection();
            thread.outStream.writeObject(packet);
            return true;
        } catch (Exception e) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads a message from a specific inStream
     *
     * @return read packet
     */
    protected synchronized Packet read() {
        try {
            Packet readPacket = (Packet) this.inStream.readObject();
            Counters.connection();
            return readPacket;
        } catch (IOException | ClassNotFoundException e) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * Let the user choose a nickname
     *
     * @return name
     * @throws java.io.IOException
     */
    protected ConnectPacket setName() throws IOException {

        Packet clientAnswer = (Packet) this.read();
        PacketType pType = clientAnswer.getIdentifier();

        if (pType == PacketType.CONNECT) {
            this.clientName = ((ConnectPacket) clientAnswer).getName();
            return (ConnectPacket) clientAnswer;
        } else {
            if (pType != PacketType.DISCONNECT) {
                this.send(new KickPacket("Connection refused"));
            }
            return null;
        }
    }

    /**
     * Adds name to clientThread at index of this client
     *
     * @param name name of the client
     */
    protected void linkNameToThread(String name) {
        for (ClientThread thread : threads) {
            if (thread == this) {
                this.clientName = name;
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
            for (ClientThread thread : threads) {
                if (thread == this) {
                    thread = null;
                }
            }
        }

        // Close streams and socket
        this.inStream.close();
        this.outStream.close();
        this.clientSocket.close();

        if (!closeOnly) {
            logControl.log(logConnection, Level.INFO, this.ip + ": " + this.clientName + " has disconnected");
        }
        Counters.disconnect();
    }

    protected synchronized void broadcastUserList(boolean excludeMe) {
        List<String> users = new ArrayList<>();
        int i = 0;
        for (ClientThread client : ChatServer.threads) {
            if (client != null && client.clientName != null) {
                users.add(client.clientName);
                i++;
            }
        }

        if (excludeMe) {
            this.broadcastExceptMe(new UserListPacket(users));
        } else {
            this.broadcast(new UserListPacket(users));
        }
    }
}
