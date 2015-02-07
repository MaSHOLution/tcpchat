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
import networking.general.Packet;
import networking.general.PacketType;
import networking.packets.ConnectPacket;
import networking.packets.InvalidPacket;
import networking.packets.KickPacket;
import security.basics.CryptoBasics;
import security.cryptography.EncryptionMethod;
import static server.console.ChatServer.logConnection;
import static server.console.ChatServer.logControl;
import static server.console.ChatServer.logException;

/**
 * Class for rejecting clients politely if server has already maxClients
 *
 * @author Manuel Schmid
 */
public final class RejectionThread extends Thread {

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
    public RejectionThread(Socket clientSocket) {
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
            initStreams();

            Packet clientAnswer = read();
            PacketType pType = clientAnswer.getIdentifier();

            if (pType == PacketType.CONNECT) {
                String name = ((ConnectPacket) clientAnswer).getName();
                send(new KickPacket("Sorry \"" + name + "\", too many clients. Please try later."));
            } else {
                if (pType != PacketType.DISCONNECT) {
                    send(new KickPacket("Security breach: Please do not use a modified client"));
                }
            }

            logControl.log(logConnection, Level.INFO, clientSocket.getRemoteSocketAddress() + ": rejected, server is full");
            clientSocket.close();

        } catch (IOException ex) {
            logControl.log(logException, Level.INFO, ip + "(Rejected client): " + ex.getMessage());
            logging.general.Counters.exception();
        }
        Counters.rejected();
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
        } catch (IOException ex) {
            logControl.log(logException, Level.INFO, this.ip + "(Rejected client): " + ex.getMessage());
            logging.general.Counters.exception();
        }
        return false;
    }

    /**
     * Reads a message from a specific inStream
     *
     * @return read packet
     */
    protected synchronized Packet read() {
        try {
            Object temp = this.inStream.readObject();
            Counters.connection();
            if (temp instanceof Packet) {
                Packet readPacket = (Packet) temp;
                return readPacket;
            }
        } catch (IOException | ClassNotFoundException ex) {
            logControl.log(logException, Level.INFO, this.ip + "(Rejected client) while reading packet: " + ex.getMessage());
            logging.general.Counters.exception();
        }
        return new InvalidPacket();
    }
}
