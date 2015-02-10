/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp;

import java.net.*;

/**
 *
 * @author manuelschmid
 */
public class UDPClient {

    public static void main(String args[]) throws Exception {
        byte[] inData = new byte[1024]; // Platz für Pakete vorbereiten
        byte[] outData = new byte[1024];
        String message;
        DatagramSocket socket = new DatagramSocket(8000); // Socket binden
        while (true) {
            // Ein Paket empfangen
            DatagramPacket in = new DatagramPacket(inData, inData.length);
            socket.receive(in);
            // Infos ermitteln und ausgeben
            InetAddress senderIP = in.getAddress();
            int senderPort = in.getPort();
            message = new String(in.getData(), 0, in.getLength());
            System.out.println("Got " + message + " from " + senderIP + "," + senderPort);
            // Antwort erzeugen
            outData = "Pong".getBytes();
            DatagramPacket out = new DatagramPacket(outData, outData.length, senderIP, senderPort);
            // Antwort senden
            socket.send(out);
        }
    }
}
