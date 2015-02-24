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
package de.mash1t.chat.client.gui;

import de.mash1t.networklib.packets.*;
import de.mash1t.chat.client.gui.tabs.TabController;
import de.mash1t.chat.logging.Counters;
import java.io.IOException;

/**
 * This class serves as an outsourced thread, as the gui can only handle one thread (itself)
 *
 * @author Manuel Schmid
 */
public class ClientGuiThread implements Runnable {

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
    }

    /*
     * Create a thread to read messages asynchronous from the server
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {
            while (!TabController.isInitialized()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException ex) {
            // TODO handle Exception
            Counters.exception();
        }

        /*
         * Keep on reading from the socket untill "Bye" is received from the
         * server
         */
        Packet responsePacket;
        PacketType ptype;

        String message, sender, receiver;
        do {
            responsePacket = null;
            responsePacket = gui.networkObj.read();
            ptype = responsePacket.getType();

            switch (ptype) {
                case Disconnect:
                    exitListening = true;
                    break;
                case GM:
                    GroupMessagePacket gm = ((GroupMessagePacket) responsePacket);
                    message = gm.getMessage();
                    sender = gm.getSender();

                    // Always output groupmessage on first tab ("Group Chat")
                    gui.tabController.outputLineOnGui("<" + sender + "> " + message, 0);
                    break;
                case Kick:
                    // TODO dialog?
                    gui.tabController.outputLineOnGui(((KickPacket) responsePacket).getMessage());
                    exitListening = true;
                    break;
                case PM:
                    PrivateMessagePacket pm = ((PrivateMessagePacket) responsePacket);
                    message = pm.getMessage();
                    sender = pm.getSender();
                    receiver = pm.getReceiver();

                    // Get name of other person
                    String person = (gui.clientName.equals(receiver)) ? sender : receiver;

                    // TODO fix bug where sender is this client
                    if (gui.tabController.outputLineOnGui("<" + sender + "> " + message, person)) {
                        if (gui.clientName.equals(sender)) {
                            gui.tabController.setFocusAt(receiver);
                        }
                    }
                    break;

                case Userlist:
                    UserListPacket ulPacket = (UserListPacket) responsePacket;
                    gui.userListController.updateUserList(ulPacket);
                    break;
                case Info:
                    gui.tabController.outputLineOnGui(((MessagePacket) responsePacket).getMessage());
            }
        } while (!exitListening);
        // Close the connection as it is no longer needed
        gui.closeConnection();
    }
}
