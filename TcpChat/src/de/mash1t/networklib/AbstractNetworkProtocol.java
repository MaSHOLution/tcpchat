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
package de.mash1t.networklib;

import de.mash1t.networklib.methods.NetworkProtocolType;
import de.mash1t.networklib.packets.Packet;
import de.mash1t.chat.server.console.ClientThread;
import de.mash1t.cryptolib.EncryptionMethod;
import de.mash1t.cryptolib.ExtendedCryptoBasics;
import de.mash1t.networklib.methods.NetworkProtocol;

/**
 * Abstract class for mainly handling static method call of send(packet, thread, nwpType) Methods have to be implemented by childs
 *
 * @author Manuel Schmid
 */
public abstract class AbstractNetworkProtocol implements NetworkProtocol {
    
    EncryptionMethod encMethod = ExtendedCryptoBasics.makeEncryptionObject();
    
    /**
     * Sends a message to a specific thread
     *
     * @param packet
     * @param clientThread
     * @param nwpType
     * @return
     */
    public static boolean send(Packet packet, ClientThread clientThread, NetworkProtocolType nwpType) {
        boolean returnValue;
        switch (nwpType) {
            case TCP:
                returnValue = ExtendedTCP.send(packet, clientThread);
                break;
            default:
                returnValue = false;
        }
        return returnValue;
    }

    @Override
    public String getIP() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean send(Packet packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Packet read() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
