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

import de.mash1t.networklib.packets.*;
import de.mash1t.networklib.methods.AbstractNetworkProtocol;
import de.mash1t.chat.core.RoleType;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import de.mash1t.chat.logging.Counters;
import static de.mash1t.chat.server.console.ChatServer.*;
import de.mash1t.networklib.methods.ExtendedTCP;
import org.bouncycastle.openpgp.PGPPublicKey;

/**
 * Class for a seperate thread for a thread
 *
 * @author Manuel Schmid
 */
public final class ClientThread extends Thread {

    protected String clientName = null;
    protected ConnectionState connState;
    public AbstractNetworkProtocol networkObject;

    /**
     * Constructor
     *
     * @param clientSocket Sochet where the connection was accepted
     * @throws java.io.IOException
     */
    public ClientThread(Socket clientSocket) throws IOException {
        networkObject = new ExtendedTCP(clientSocket, RoleType.Server);
    }

    /**
     * Called when thread is started
     */
    @Override
    public void run() {

        connState = ConnectionState.InLogin;

        try {
            // Setting up name
            ConnectPacket clientAnswer = (ConnectPacket) networkObject.read();
            if (this.checkName(clientAnswer) && this.checkEncryption(clientAnswer)) {
                this.linkToThread(clientAnswer);

                connState = ConnectionState.Online;

                // Broadcasts welcome message to all clients
                this.broadcastUserList(UserListPacketType.Connected);
                //this.broadcastExceptMe(new InfoPacket("*** User \"" + this.clientName + "\" joined ***"));
                networkObject.send(new InfoPacket("Welcome \"" + this.clientName + "\" to our chat room."));
                logControl.log(logGeneral, Level.INFO, this.clientName + " joined");

                // Start conversation
                while (connState != ConnectionState.Kicked && connState != ConnectionState.RequestedDisconnect) {
                    Packet packet = networkObject.read();
                    PacketType ptype = packet.getType();

                    switch (ptype) {
                        case Disconnect:
                            // Client disconnected
                            connState = ConnectionState.RequestedDisconnect;
                            break;
                        case PM:
                            // Private message
                            this.forwardPrivateMessage((PrivateMessagePacket) packet);
                            break;
                        case Invalid:
                            // Invalid obj or obj received
                            networkObject.send(new KickPacket("Security breach: Please do not use a modified client"));
                            connState = ConnectionState.Kicked;
                            break;
                        case GM:
                            // Broadcast group message to all other clients
                            this.broadcast((GroupMessagePacket) packet);
                    }
                }

                if (connState == ConnectionState.Kicked) {
                    // Tell every thread, that the current thread has been kicked
                    //this.broadcastExceptMe(new InfoPacket("*** User \"" + this.clientName + "\" has been kicked ***"));

                    // Remove thread from threads array and close connections
                    disconnect();
                } else {
                    // Tell every thread, that the current thread is going offline
                    //this.broadcastExceptMe(new InfoPacket("*** User \"" + this.clientName + "\" has left ***"));
                    networkObject.send(new DisconnectPacket());

                    // Remove thread from threads array and close connections
                    disconnect();
                }

                this.broadcastUserList(UserListPacketType.Disconnected);
            } else {
                disconnect();
            }

        } catch (Exception ex) {
            logControl.log(logException, Level.INFO, networkObject.getIP() + "(" + this.clientName + ", general exception): " + ex.getMessage());
            Counters.exception();
        }
    }

    /**
     * Sends a message to all clients
     *
     * @param message message to send
     */
    protected synchronized void broadcast(String message) {
        for (ClientThread thread : threads) {
            if (connState == ConnectionState.Online) {
                AbstractNetworkProtocol.send(new GroupMessagePacket(message, this.clientName), thread, ChatServer.nwpType);
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
            if (thread.connState == ConnectionState.Online) {
                AbstractNetworkProtocol.send(packet, thread, nwpType);
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
            if (thread.connState == ConnectionState.Online && thread != this) {
                AbstractNetworkProtocol.send(packet, thread, nwpType);
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
                networkObject.send(new InfoPacket("You can't send private messages to yourself"));
                logControl.log(logGeneral, Level.INFO, this.clientName + " wanted to send himself a private message");
                return true;
            } else {
                for (ClientThread thread : threads) {
                    if (thread != this
                            && thread.connState == ConnectionState.Online
                            && thread.clientName.equals(receiver)) {

                        // Send privatePacket to receiver
                        AbstractNetworkProtocol.send(privatePacket, thread, ChatServer.nwpType);

                        // Send privatePacket to sender
                        networkObject.send(privatePacket);
                        Counters.pm();
                        logControl.log(logGeneral, Level.INFO, "PM #" + Counters.Totals.Messages.pmTotal + " from " + this.clientName + " to " + receiver);
                        return true;
                    }
                }
            }

            // Receiver has not been found / is not online
            // TODO Handle asynchronous messages/connections
            networkObject.send(new InfoPacket("Message could not be delivered, reason: \"" + receiver + "\" is not online"));
            Counters.pm();
            Counters.pmFailed();
            logControl.log(logGeneral, Level.INFO, "PM #" + Counters.Totals.Messages.pmTotal + " from " + this.clientName + " failed: " + receiver + " is not online");
            return false;
        } catch (Exception ex) {
            logControl.log(logException, Level.INFO, networkObject.getIP() + "(" + this.clientName + ") while sending PM: " + ex.getMessage());
            de.mash1t.chat.logging.Counters.exception();
            networkObject.send(new InfoPacket("Message could not be delivered, reason: Internal Server Error"));
            Counters.pm();
            Counters.pmFailed();
            return false;
        }

    }

    /**
     * Let the user choose a nickname
     *
     * @param connPacket
     * @return name
     */
    protected boolean checkName(ConnectPacket connPacket) {

        PacketType pType = connPacket.getType();

        if (pType == PacketType.Connect) {
            String name = connPacket.getName();
            if (name == null || name.length() < 4 || name.length() > 15) {
                networkObject.send(new KickPacket("Please make sure that your nickname has between 4 and 15 letters"));
                return false;
            }

            // Check if name is already in use, if yes return null
            for (ClientThread client : threads) {
                if (client.connState == ConnectionState.Online && client.clientName.equals(name)) {
                    networkObject.send(new KickPacket("The nickname \"" + name + "\" is already in use"));
                    return false;
                }
            }

            this.clientName = name;
            return true;
        } else {
            if (pType != PacketType.Disconnect) {
                networkObject.send(new KickPacket("Security breach: Please do not use a modified client"));
            }
            return false;
        }
    }

    /**
     * Checks the encryption key set in the ConnectPacket
     *
     * @param connPacket packet to check key at
     * @return key is valid
     */
    protected boolean checkEncryption(ConnectPacket connPacket) {
        try {
            PGPPublicKey pubKey = connPacket.getKey();
            if (pubKey != null) {
                //pubKey.getBitStrength()
                return true;
            }
        } catch (Exception ex) {
            logControl.log(logException, Level.INFO, this.networkObject.getIP() + " (error while checking encryption):" + ex.getMessage());
        }
        networkObject.send(new KickPacket("Server only accepts clients with encryption turned on"));
        return false;
    }

    /**
     * Adds information to this thread
     *
     * @param connPacket
     */
    protected void linkToThread(ConnectPacket connPacket) {
        this.clientName = connPacket.getName();
        logControl.log(logConnection, Level.INFO, networkObject.getIP() + ": is now " + this.clientName);
        userList.put(this.clientName, connPacket.getKey());
    }

    /**
     * Disconnects the thread
     */
    protected synchronized void disconnect() {
        threads.remove(this);
        networkObject.close();
        userList.remove(clientName);

        if (connState == ConnectionState.Kicked) {
            logControl.log(logConnection, Level.INFO, networkObject.getIP() + ": " + this.clientName + " has been kicked");
        } else {
            logControl.log(logConnection, Level.INFO, networkObject.getIP() + ": " + this.clientName + " has disconnected");
        }
        Counters.disconnect();
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
            networkObject.send(new UserListPacket(getUserList()));
        } else if (ulPacketType == UserListPacketType.Disconnected) {
            // Broadcast changes only
            this.broadcastExceptMe(new UserListPacket(this.clientName, ulPacketType));
        }
    }
}
