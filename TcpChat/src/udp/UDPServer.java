/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuelschmid
 */
public class UDPServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            byte[] inData = new byte[1024];
            byte[] outData = new byte[1024];
            String message;
            int i = 0;

            while (true) {
                // Socket erzeugen
                DatagramSocket socket = new DatagramSocket();
                // Paket bauen und adressieren
                InetAddress serverIP = InetAddress.getByName("localhost");
                outData = "Ping".getBytes();
                DatagramPacket out = new DatagramPacket(outData, outData.length, serverIP, 8000);
                // Paket senten
                socket.send(out);
                // Antwort empfangen und ausgeben.
                DatagramPacket in = new DatagramPacket(inData, inData.length);
                socket.receive(in);
                message = new String(in.getData(), 0, in.getLength());
                System.out.println("Got " + message + ", " + i);
                // Socket schliessen
                socket.close();
                i++;
            }
        } catch (SocketException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
