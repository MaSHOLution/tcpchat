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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Manuel Schmid
 */
public class UDP implements NetworkProtocolClass {

    byte[] inData = new byte[1024]; // Platz für Pakete vorbereiten
    byte[] outData = new byte[1024];
    String message;
    DatagramSocket socket;
    int senderPort;
    InetAddress senderIP;
    Type type;
    String check = "OK";

    public UDP(Type type) {
        try {
            this.type = type;
            if (type == Type.Client) {
                senderIP = InetAddress.getByName("localhost");
                socket = new DatagramSocket(8001);
            } else {
                socket = new DatagramSocket(8000);
            }
        } catch (SocketException | UnknownHostException ex) {
            Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String read() {
        try {
            resetSocketTimeout();
            if (type == Type.Client) {
                // Antwort empfangen und ausgeben.
                DatagramPacket in = new DatagramPacket(inData, inData.length);
                socket.receive(in);
                message = new String(in.getData(), 0, in.getLength());
                sendWithoutTimeout(check);
                //close();
            } else {
                // Ein Paket empfangen
                DatagramPacket in = new DatagramPacket(inData, inData.length);
                socket.receive(in);
                // Infos ermitteln und ausgeben
                senderIP = in.getAddress();
                senderPort = in.getPort();
                message = new String(in.getData(), 0, in.getLength());
                sendWithoutTimeout(check);
            }
            return message;
        } catch (IOException ex) {
            Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean send(String message) {
        try {
            if (sendWithoutTimeout(message)) {
                socket.setSoTimeout(5000);
                while (true) {
                    try {
                        DatagramPacket checkPacket = new DatagramPacket(inData, inData.length);
                        socket.receive(checkPacket);
                        String received = new String(checkPacket.getData(), 0, checkPacket.getLength());
                        if(received.equals(check)){
                            return true;
                        }                        
                        // TODO multiclient
                        return false;
                    } catch (SocketTimeoutException e) {
                        System.out.println("Timeout reached!!! " + e);
                        return false;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean sendWithoutTimeout(String message) {
        try {
            
            if (type == Type.Client) {
                //socket = new DatagramSocket();
                outData = message.getBytes();
                DatagramPacket out = new DatagramPacket(outData, outData.length, senderIP, 8000);
                socket.send(out);
            } else {
                resetSocketTimeout();
                outData = message.getBytes();
                DatagramPacket out = new DatagramPacket(outData, outData.length, senderIP, senderPort);
                socket.send(out);
            }
            return true;
        } catch (IOException ex) {
            Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean sendSessionId() {
        return send(sessionId);
    }

    @Override
    public void close() {
        socket.close();
    }

    private void resetSocketTimeout() {
        try {
            if (socket != null) {
                socket.setSoTimeout(0);
            }
        } catch (SocketException ex) {
            Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
