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

import networking.general.Packet;
import security.basics.CryptoBasics;
import security.cryptography.EncryptionMethod;

/**
 * Interface for a network protocol
 *
 * @author Manuel Schmid
 */
public interface NetworkProtocol {

    final EncryptionMethod encMethod = CryptoBasics.makeEncryptionObject();

    /**
     * Returns the IP of the currently connected client
     *
     * @return IP of client
     */
    public String getIP();

    /**
     * Sends a message
     *
     * @param packet
     * @return
     */
    public boolean send(Packet packet);

    /**
     * Sends the session id
     *
     * @return
     */
    public boolean sendSessionId();

    /**
     * Reads an object
     *
     * @return
     */
    public Packet read();

    /**
     * Closes all opened streams
     *
     * @return everything closed?
     */
    public boolean close();
}
