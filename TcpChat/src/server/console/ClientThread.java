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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import logging.general.Counters;
import networking.packets.*;
import networking.general.*;
import security.basics.CryptoBasics;
import security.cryptography.EncryptionMethod;
import static server.console.ChatServer.*;

/**
 * Class for a seperate thread for a thread
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
    protected ConnectionState state;

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

        state = ConnectionState.InLogin;

        try {
            // Setting up streams
            this.initStreams();

            // Setting up name
            ConnectPacket cPacket = this.setName();
            if (cPacket != null) {
                this.linkNameToThread(cPacket.getName());

                state = ConnectionState.Online;

                // Broadcasts welcome message to all clients
                this.broadcastUserList(UserListPacketType.Connected);
                //this.broadcastExceptMe(new InfoPacket("*** User \"" + this.clientName + "\" joined ***"));
                this.send(new InfoPacket("Welcome \"" + this.clientName + "\" to our chat room."));
                logControl.log(logGeneral, Level.INFO, this.clientName + " joined");

                // Start conversation
                while (state != ConnectionState.Kicked && state != ConnectionState.RequestedDisconnect) {
                    Packet packet = this.read();
                    PacketType ptype = packet.getIdentifier();

                    switch (ptype) {
                        case Disconnect:
                            // Client disconnected
                            state = ConnectionState.RequestedDisconnect;
                            break;
                        case PM:
                            // Private message
                            this.forwardPrivateMessage((PrivateMessagePacket) packet);
                            break;
                        case Invalid:
                            // Invalid obj or obj received
                            this.send(new KickPacket("Security breach: Please do not use a modified client"));
                            state = ConnectionState.Kicked;
                            break;
                        case GM:
                            // Broadcast group message to all other clients
                            this.broadcast((GroupMessagePacket) packet);
                    }
                }

                if (state == ConnectionState.Kicked) {
                    // Tell every thread, that the current thread has been kicked
                    //this.broadcastExceptMe(new InfoPacket("*** User \"" + this.clientName + "\" has been kicked ***"));

                    // Remove thread from threads array and close connections
                    disconnect();
                } else {
                    // Tell every thread, that the current thread is going offline
                    //this.broadcastExceptMe(new InfoPacket("*** User \"" + this.clientName + "\" has left ***"));
                    this.send(new DisconnectPacket());

                    // Remove thread from threads array and close connections
                    disconnect();
                }

                this.broadcastUserList(UserListPacketType.Disconnected);
            } else {
                disconnect();
            }

        } catch (Exception ex) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + "): " + ex.getMessage());
            Counters.exception();
        }
    }

    /**
     * Creates input and output streams for this thread
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
            if (state == ConnectionState.Online) {
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
            if (thread.state == ConnectionState.Online) {
                this.send(packet, thread);
            }
        }
        logControl.log(logGeneral, Level.INFO, "GM #" + Counters.Totals.Messages.gmTotal + " from " + this.clientName);
        Counters.gm();
    }

    /**
     * Sends a message to all other clients except the current thread (this)
     *
     * @param packet
     */
    protected synchronized void broadcastExceptMe(Packet packet) {
        for (ClientThread thread : threads) {
            if (thread.state == ConnectionState.Online && thread != this) {
                this.send(packet, thread);
            }
        }
        // Counters.gm(); is normally no group but system shoutout
    }

    /**
     * Sends a private privatePacket to one thread
     *
     * @param privatePacket privatePacket to send
     * @return message send status
     */
    protected synchronized boolean forwardPrivateMessage(PrivateMessagePacket privatePacket) {
        try {
            String receiver = privatePacket.getReceiver();
            // Check if sender wants to send privatePacket to himself
            if (receiver.equals(this.clientName)) {
                this.send(new InfoPacket("You can't send private messages to yourself"));
                logControl.log(logGeneral, Level.INFO, this.clientName + " wanted to send himself a private message");
                return true;
            } else {
                for (ClientThread thread : threads) {
                    if (thread != this
                            && thread.state == ConnectionState.Online
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
        } catch (Exception ex) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + ") while sending PM: " + ex.getMessage());
            logging.general.Counters.exception();
            this.send(new InfoPacket("Message could not be delivered, reason: Internal Server Error"));
            Counters.pm();
            Counters.pmFailed();
            return false;
        }

    }

    /**
     * Writes a obj to a specific PrintStream
     *
     * @param packet stands for itself
     * @return result of sending
     */
    protected synchronized boolean send(Packet packet) {
        try {
            Counters.connection();
            this.outStream.writeObject(packet);
            return true;
        } catch (IOException ex) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + "): " + ex.getMessage());
            logging.general.Counters.exception();
        }
        return false;
    }

    /**
     * Writes a obj to a specific PrintStream
     *
     * @param packet stands for itself
     * @param thread ClientThread to send obj to
     * @return result of sending
     */
    protected synchronized boolean send(Packet packet, ClientThread thread) {
        try {
            Counters.connection();
            thread.outStream.writeObject(packet);
            return true;
        } catch (Exception ex) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + "): " + ex.getMessage());
            logging.general.Counters.exception();
        }
        return false;
    }

    /**
     * Reads a message from a specific inStream
     *
     * @return read obj
     */
    protected synchronized Packet read() {
        try {
            Object obj = this.inStream.readObject();
            Counters.connection();
            if (obj instanceof Packet) {
                Packet readPacket = (Packet) obj;
                return readPacket;
            }
        } catch (IOException | ClassNotFoundException ex) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + ") while reading packet: " + ex.getMessage());
            logging.general.Counters.exception();
        }
        return new InvalidPacket();
    }

    /**
     * Let the user choose a nickname
     *
     * @return name
     */
    protected ConnectPacket setName() {

        Packet clientAnswer = (Packet) this.read();
        PacketType pType = clientAnswer.getIdentifier();

        if (pType == PacketType.Connect) {
            String name = ((ConnectPacket) clientAnswer).getName();
            if (name == null || name.length() < 4 || name.length() > 15) {
                this.send(new KickPacket("Please make sure that your nickname has between 4 and 15 letters"));
                return null;
            }

            // Check if name is already in use, if yes return null
            for (ClientThread client : threads) {
                if (client != null && client.state == ConnectionState.Online && client.clientName.equals(name)) {
                    this.send(new KickPacket("The nickname \"" + name + "\" is already in use"));
                    return null;
                }
            }

            this.clientName = name;
            return (ConnectPacket) clientAnswer;
        } else {
            if (pType != PacketType.Disconnect) {
                this.send(new KickPacket("Security breach: Please do not use a modified client"));
            }
            return null;
        }
    }

    /**
     * Adds name to clientThread at index of this thread
     *
     * @param name name of the thread
     */
    protected void linkNameToThread(String name) {
        for (ClientThread thread : threads) {
            if (thread == this) {
                this.clientName = name;
                logControl.log(logConnection, Level.INFO, this.ip + ": is now " + name);
                userList.add(name);
                break;
            }
        }
    }

    /**
     * Disconnects the thread
     */
    protected synchronized void disconnect() {
        try {
            threads.remove(this);

            // Close streams and socket
            this.inStream.close();
            this.outStream.close();
            this.clientSocket.close();
            userList.remove(clientName);

            if (state == ConnectionState.Kicked) {
                logControl.log(logConnection, Level.INFO, this.ip + ": " + this.clientName + " has disconnected");
            } else {
                logControl.log(logConnection, Level.INFO, this.ip + ": " + this.clientName + " has been kicked");
            }
            Counters.disconnect();
        } catch (IOException ex) {
            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + ") while disconnecting: " + ex.getMessage());
            logging.general.Counters.exception();
        }
    }

    /**
     * Broadcasts the userlist
     *
     * @param ulPacketType
     */
    protected synchronized void broadcastUserList(UserListPacketType ulPacketType) {
        if (ulPacketType == UserListPacketType.Connected) {
            // Broadcast changes to all and a full list to hte new client
            this.broadcastExceptMe(new UserListPacket(this.clientName, ulPacketType));
            this.send(new UserListPacket());
        } else if (ulPacketType == UserListPacketType.Disconnected) {
            // Broadcast changes only
            this.broadcastExceptMe(new UserListPacket(this.clientName, ulPacketType));
        }
    }
}
