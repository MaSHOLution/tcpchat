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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import security.basics.SessionIdGenerator;

/**
 *
 * @author Manuel Schmid
 */
public class Udp implements NetworkProtocolClass {

    public static final int packetSize = 1024;

    private final byte[] inData = new byte[packetSize];
    private byte[] outData = new byte[packetSize];
    private final InetAddress serverIP;
    private final DatagramSocket socket;
    private final int port;

    public Udp(String server, int port) throws UnknownHostException, SocketException {
        serverIP = InetAddress.getByName(server);
        socket = new DatagramSocket();
        this.port = port;
    }

    @Override
    public boolean send(String message) {
        try {
            outData = message.getBytes();
            DatagramPacket out = new DatagramPacket(outData, outData.length, serverIP, port);
            socket.send(out);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Udp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public String read() {
        try {
            DatagramPacket in = new DatagramPacket(inData, inData.length);
            socket.receive(in);
            String message = new String(in.getData(), 0, in.getLength());
            return message;
        } catch (IOException ex) {
            Logger.getLogger(Udp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean sendSessionId() {
        return send(sessionId);
    }

    @Override
    public void close() {
        socket.close();
    }
}
