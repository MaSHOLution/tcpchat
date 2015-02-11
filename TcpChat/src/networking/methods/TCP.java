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
package networking.methods;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import logging.general.Counters;
import networking.general.Packet;
import networking.packets.InvalidPacket;
import server.console.ClientThread;

/**
 *
 * @author Manuel Schmid
 */
public class TCP implements NetworkProtocolClass {

    protected ObjectInputStream inStream = null;
    protected ObjectOutputStream outStream = null;
    protected Socket clientSocket = null;

    /**
     * Constructor, creates input and output streams
     *
     * @param clientSocket Socket for client
     * @throws IOException
     */
    public TCP(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        inStream = new ObjectInputStream(clientSocket.getInputStream());
        outStream = new ObjectOutputStream(clientSocket.getOutputStream());
    }

    /**
     * Writes a Packet to the ObjectOutputStream
     *
     * @param packet stands for itself
     * @return result of sending
     */
    public synchronized boolean send(Packet packet) {
        try {
            Counters.connection();
            this.outStream.writeObject(packet);
            return true;
        } catch (IOException ex) {
//            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + "): " + ex.getMessage());
//            logging.general.Counters.exception();
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
    public synchronized boolean send(Packet packet, ClientThread thread) {
        try {
            Counters.connection();
            thread.conLib.outStream.writeObject(packet);
            return true;
        } catch (Exception ex) {
//            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + "): " + ex.getMessage());
//            logging.general.Counters.exception();
        }
        return false;
    }

    /**
     * Reads a Packet from the ObjectInputStream
     *
     * @return read obj
     */
    @Override
    public synchronized Packet read() {
        try {
            Object obj = this.inStream.readObject();
            Counters.connection();
            if (obj instanceof Packet) {
                Packet readPacket = (Packet) obj;
                return readPacket;
            }
        } catch (IOException | ClassNotFoundException ex) {
//            logControl.log(logException, Level.INFO, this.ip + "(" + this.clientName + ") while reading packet: " + ex.getMessage());
//            logging.general.Counters.exception();
        }
        return new InvalidPacket();
    }

    @Override
    public boolean send(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean sendSessionId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean close() {
        try {
            // Close streams and socket
            inStream.close();
            outStream.close();
            clientSocket.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(TCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
