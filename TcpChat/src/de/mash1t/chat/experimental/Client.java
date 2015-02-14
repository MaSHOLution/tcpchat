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
public class Client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UDP test = new UDP(NetworkProtocolRole.Client);
        int i = 0;
        System.out.println("CLIENT");
        while (true) {
            test.send("Client " + i);
            System.out.println(test.read());
            i++;
        }
    }
}
