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
import de.mash1t.networklib.AbstractNetworkProtocol;
import de.mash1t.chat.core.RoleType;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import de.mash1t.chat.logging.Counters;
import static de.mash1t.chat.server.console.ChatServer.*;
import de.mash1t.networklib.ExtendedTCP;

/**
 * Class for a seperate thread for a thread
 *
 * @author Manuel Schmid
 */
public final class ClientThread extends Thread {

    protected String clientName = null;
    protected ConnectionState state;
    public AbstractNetworkProtocol conLib;

    /**
     * Constructor
     *
     * @param clientSocket Sochet where the connection was accepted
     * @throws java.io.IOException
     */
    public ClientThread(Socket clientSocket) throws IOException {
        conLib = new ExtendedTCP(clientSocket, RoleType.Server);
    }

    /**
     * Called when thread is started
     */
    @Override
    public void run() {

        state = ConnectionState.InLogin;

        try {
            // Setting up name
            ConnectPacket cPacket = this.setName();
            if (cPacket != null) {
                this.linkNameToThread(cPacket.getName());

                state = ConnectionState.Online;

                // Broadcasts welcome message to all clients
                this.broadcastUserList(UserListPacketType.Connected);
                //this.broadcastExceptMe(new InfoPacket("*** User \"" + this.clientName + "\" joined ***"));
                conLib.send(new InfoPacket("Welcome \"" + this.clientName + "\" to our chat room."));
                logControl.log(logGeneral, Level.INFO, this.clientName + " joined");

                // Start conversation
                while (state != ConnectionState.Kicked && state != ConnectionState.RequestedDisconnect) {
                    Packet packet = conLib.read();
                    PacketType ptype = packet.getType();

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
                            conLib.send(new KickPacket("Security breach: Please do not use a modified client"));
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
                    conLib.send(new DisconnectPacket());

                    // Remove thread from threads array and close connections
                    disconnect();
                }

                this.broadcastUserList(UserListPacketType.Disconnected);
            } else {
                disconnect();
            }

        } catch (Exception ex) {
            logControl.log(logException, Level.INFO, conLib.getIP() + "(" + this.clientName + "): " + ex.getMessage());
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
            if (state == ConnectionState.Online) {
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
            if (thread.state == ConnectionState.Online) {
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
            if (thread.state == ConnectionState.Online && thread != this) {
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
                conLib.send(new InfoPacket("You can't send private messages to yourself"));
                logControl.log(logGeneral, Level.INFO, this.clientName + " wanted to send himself a private message");
                return true;
            } else {
                for (ClientThread thread : threads) {
                    if (thread != this
                            && thread.state == ConnectionState.Online
                            && thread.clientName.equals(receiver)) {

                        // Send privatePacket to receiver
                        AbstractNetworkProtocol.send(privatePacket, thread, ChatServer.nwpType);

                        // Send privatePacket to sender
                        conLib.send(privatePacket);
                        Counters.pm();
                        logControl.log(logGeneral, Level.INFO, "PM #" + Counters.Totals.Messages.pmTotal + " from " + this.clientName + " to " + receiver);
                        return true;
                    }
                }
            }

            // Receiver has not been found / is not online
            // TODO Handle asynchronous messages/connections
            conLib.send(new InfoPacket("Message could not be delivered, reason: \"" + receiver + "\" is not online"));
            Counters.pm();
            Counters.pmFailed();
            logControl.log(logGeneral, Level.INFO, "PM #" + Counters.Totals.Messages.pmTotal + " from " + this.clientName + " failed: " + receiver + " is not online");
            return false;
        } catch (Exception ex) {
            logControl.log(logException, Level.INFO, conLib.getIP() + "(" + this.clientName + ") while sending PM: " + ex.getMessage());
            de.mash1t.chat.logging.Counters.exception();
            conLib.send(new InfoPacket("Message could not be delivered, reason: Internal Server Error"));
            Counters.pm();
            Counters.pmFailed();
            return false;
        }

    }

    /**
     * Let the user choose a nickname
     *
     * @return name
     */
    protected ConnectPacket setName() {

        Packet clientAnswer = (Packet) conLib.read();
        PacketType pType = clientAnswer.getType();

        if (pType == PacketType.Connect) {
            String name = ((ConnectPacket) clientAnswer).getName();
            if (name == null || name.length() < 4 || name.length() > 15) {
                conLib.send(new KickPacket("Please make sure that your nickname has between 4 and 15 letters"));
                return null;
            }

            // Check if name is already in use, if yes return null
            for (ClientThread client : threads) {
                if (client != null && client.state == ConnectionState.Online && client.clientName.equals(name)) {
                    conLib.send(new KickPacket("The nickname \"" + name + "\" is already in use"));
                    return null;
                }
            }

            this.clientName = name;
            return (ConnectPacket) clientAnswer;
        } else {
            if (pType != PacketType.Disconnect) {
                conLib.send(new KickPacket("Security breach: Please do not use a modified client"));
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
        clientName = name;
        logControl.log(logConnection, Level.INFO, conLib.getIP() + ": is now " + name);
        userList.add(name);
    }

    /**
     * Disconnects the thread
     */
    protected synchronized void disconnect() {
//        try {
        threads.remove(this);
        conLib.close();
        userList.remove(clientName);

        if (state == ConnectionState.Kicked) {
            logControl.log(logConnection, Level.INFO, conLib.getIP() + ": " + this.clientName + " has been kicked");
        } else {
            logControl.log(logConnection, Level.INFO, conLib.getIP() + ": " + this.clientName + " has disconnected");
        }
        Counters.disconnect();
//        } catch (IOException ex) {
//            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + ") while disconnecting: " + ex.getMessage());
//            logging.general.Counters.exception();
//        }
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
            conLib.send(new UserListPacket(getUserList()));
        } else if (ulPacketType == UserListPacketType.Disconnected) {
            // Broadcast changes only
            this.broadcastExceptMe(new UserListPacket(this.clientName, ulPacketType));
        }
    }
}
