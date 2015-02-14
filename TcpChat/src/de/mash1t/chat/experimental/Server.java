/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mash1t.chat.experimental;

import de.mash1t.chat.networking.methods.UDP;
import de.mash1t.chat.networking.methods.NetworkProtocolRole;

/**
 *
 * @author manuelschmid
 */
public class Server {

    public static void main(String args[]) throws Exception {
        String message;
        UDP test = new UDP(NetworkProtocolRole.Server);
        System.out.println("SERVER");

        while (true) {
            message = test.read();
            System.out.println("Got " + message);
            test.send("From Server");
        }
    }

}
