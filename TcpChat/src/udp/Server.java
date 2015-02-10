/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp;

import networking.methods.UDP;
import networking.methods.Type;

/**
 *
 * @author manuelschmid
 */
public class Server {

    public static void main(String args[]) throws Exception {
        String message;
        UDP test = new UDP(Type.Server);
        System.out.println("SERVER");

        while (true) {
            message = test.read();
            System.out.println("Got " + message);
            test.send("From Server");
        }
    }

}
