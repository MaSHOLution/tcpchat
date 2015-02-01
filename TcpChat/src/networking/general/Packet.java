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
package networking.general;

import java.io.Serializable;

/**
 * Abstract class for packets
 *
 * @author Manuel Schmid
 */
public abstract class Packet implements Serializable {

    protected PacketType packetIdentifier = PacketType.PACKET;
    protected String senderAlias = "Server";
    protected boolean isPrepared = false;
    /**
     * Getter for identifier
     *
     * @return
     */
    public PacketType getIdentifier() {
        return this.packetIdentifier;
    }

    /**
     * Getter for senderAlias
     *
     * @return
     */
    public String getSenderAlias() {
        return this.senderAlias;
    }

    /**
     * Setter for senderAlias
     * protected because of one time usage of packets
     *
     * @param senderAlias
     */
    protected void setSenderAlias(String senderAlias) {
        this.senderAlias = senderAlias;
    }
    /**
     * Prepares the packet for sending
     *
     */
    public void prepare() {
        if (this.isPrepared) {
            // TODO Decrypt
            this.isPrepared = false;
        } else {
            // TODO Encrypt
            this.isPrepared = true;
        }
    }
}