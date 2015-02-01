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
package client.gui;

import networking.packets.KickPacket;
import networking.packets.PrivateMessagePacket;
import networking.packets.UserListPacket;
import networking.packets.GroupMessagePacket;
import networking.general.PacketType;
import networking.general.MessagePacket;
import networking.general.Packet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import networking.packets.InvalidPacket;

/**
 * This class serves as an outsourced thread, as the gui can only handle one
 * thread (itself)
 *
 * @author Manuel Schmid
 */
public class ClientGuiThread implements Runnable {

    // Stream
    protected ObjectInputStream inStream = null;

    // Current gui thread
    protected ClientGui gui = null;

    protected boolean exitListening = false;

    /**
     * Constructor
     *
     * @param gui
     */
    public ClientGuiThread(ClientGui gui) {
        this.gui = gui;
        this.inStream = gui.inStream;
    }

    /*
     * Create a thread to read messages asynchronous from the server
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        /*
         * Keep on reading from the socket untill "Bye" is received from the
         * server
         */
        Packet responsePacket;
        PacketType ptype;

        String message, sender, receiver;
        do {

            responsePacket = read();
            ptype = responsePacket.getIdentifier();

            switch (ptype) {
                case DISCONNECT:
                    gui.outputLineOnGui("*** Disconnected ***");
                    exitListening = true;
                    break;
                case KICK:
                    gui.outputLineOnGui(((KickPacket) responsePacket).getMessage());
                    exitListening = true;
                    break;
                case USERLIST:
                    List<String> list = ((UserListPacket) responsePacket).getUserList();
                    gui.updateUserList(list);
                    break;
                case PM:
                    PrivateMessagePacket pm = ((PrivateMessagePacket) responsePacket);
                    message = pm.getMessage();
                    sender = pm.getSender();
                    receiver = pm.getReceiver();

                    gui.outputLineOnGui("<" + sender + " to " + receiver + "> " + message);
                    break;
                case GM:
                    GroupMessagePacket gm = ((GroupMessagePacket) responsePacket);
                    message = gm.getMessage();
                    sender = gm.getSender();

                    gui.outputLineOnGui("<" + sender + "> " + message);
                    break;
                default:
                    gui.outputLineOnGui(((MessagePacket) responsePacket).getMessage());
            }
        } while (!exitListening);
        // Close the connection as it is no longer needed
        gui.closeConnection();
    }

    /**
     * Reads a message from a specific inStream
     *
     * @return read packet
     */
    protected synchronized Packet read() {
        try {
            Object temp = this.inStream.readObject();
            if (temp instanceof Packet) {
                Packet readPacket = (Packet) temp;
                return readPacket;
            }
        } catch (IOException | ClassNotFoundException ex) {
            gui.outputLineOnGui("*** SERVER IS GOING DOWN ***");
            exitListening = true;
        }
        return new InvalidPacket();
    }
}
