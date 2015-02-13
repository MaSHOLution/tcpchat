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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import networking.general.Packet;
import static networking.methods.NetworkProtocol.encMethod;
import networking.packets.InfoPacket;

/**
 * Class for the network protocol UDP
 * 
 * @author Manuel Schmid
 */
public class UDP extends AbstractNetworkProtocol {

    private int byteSize = 1024;
    private byte[] inData = new byte[byteSize]; // Platz für Pakete vorbereiten
    private byte[] outData = new byte[byteSize];
    private String check = "OK";
    private int socketTimeout = 5000;
    private DatagramSocket socket;
    private int senderPort;
    private InetAddress senderIP;
    private NetworkProtocolRole type;
    private Packet returnPacket;

    /**
     * Constructor
     *
     * @param type
     */
    public UDP(NetworkProtocolRole type) {
        try {
            this.type = type;
            if (type == NetworkProtocolRole.Client) {
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
    public Packet read() {
        try {
            resetSocketTimeout();
            if (type == NetworkProtocolRole.Client) {
                // Antwort empfangen und ausgeben.
                DatagramPacket in = new DatagramPacket(inData, inData.length);
                socket.receive(in);
                returnPacket = deserializePacket(inData);
                sendWithoutTimeout(check);
                //close();
            } else {
                // Ein Paket empfangen
                DatagramPacket in = new DatagramPacket(inData, inData.length);
                socket.receive(in);
                // Infos ermitteln und ausgeben
                senderIP = in.getAddress();
                senderPort = in.getPort();
                returnPacket = deserializePacket(inData);
                sendWithoutTimeout(check);
            }
            return returnPacket;
        } catch (IOException ex) {
            Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean send(Packet packet) {
        try {
            if (sendWithoutTimeout(packet)) {
                socket.setSoTimeout(socketTimeout);
                while (true) {
                    try {
                        DatagramPacket checkPacket = new DatagramPacket(inData, inData.length);
                        socket.receive(checkPacket);
                        String received = new String(checkPacket.getData(), 0, checkPacket.getLength());
                        return received.equals(check);
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

    /**
     * Sends a packet withoutapplying a timeout on the socket
     *
     * @param packet Packet to send
     * @return if packet has been sent successfully
     */
    public boolean sendWithoutTimeout(Packet packet) {
        try {
            if (type == NetworkProtocolRole.Client) {
                //socket = new DatagramSocket();
                outData = serializePacket(packet);
                DatagramPacket out = new DatagramPacket(outData, outData.length, senderIP, 8000);
                socket.send(out);
            } else {
                resetSocketTimeout();
                outData = serializePacket(packet);
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
        // TODO implement SessionIdPacket
        return send(new InfoPacket(encMethod.sessionId));
    }

    @Override
    public boolean close() {
        socket.close();
        return true;
    }

    /**
     * Resets the socket timeout
     */
    private void resetSocketTimeout() {
        try {
            if (socket != null) {
                socket.setSoTimeout(0);
            }
        } catch (SocketException ex) {
            Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Serializes a packet
     *
     * @param packet Packet to serialize
     * @return byteArray from given packet
     */
    private byte[] serializePacket(Packet packet) {
        try {
            ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream(2048);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baOutputStream);
            objectOutputStream.writeObject(packet);
            objectOutputStream.close();
            // get the byte array of the object
            byte[] byteArray = baOutputStream.toByteArray();
            baOutputStream.close();
            return byteArray;
        } catch (Exception ex) {
            Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Deserializes a packet
     *
     * @param data received data to built packet from
     * @return Packet
     */
    public Packet deserializePacket(byte[] data) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
            Packet packet = (Packet) objectInputStream.readObject();
            objectInputStream.close();
            return packet;
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
