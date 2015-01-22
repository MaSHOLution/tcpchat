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
package security.basics;

import security.cryptography.method.*;
import security.cryptography.EncryptionMethod;
import security.cryptography.Method;

/**
 * Contains all basic information for session and cryptography
 *
 * @author Manuel Schmid
 */
public final class CryptoBasics {

    /**
     * Encryption in bytes,
     */
    public static final int encryption = 256;

    /**
     * Bits to make SessionId from
     */
    public static final int encryptionBits = (encryption * 8);

    /**
     * Currently used encryption method
     */
    public static final Method encMethod = Method.OFF;

    // TODO write javadoc
    /**
     *
     * @param <T>
     * @param encMethod
     * @return
     */
    public static final <T> T makeEncryptionObject() {
        switch (encMethod) {
            case AES:
                return (T) new Aes();
            default:
                return (T) new EncryptionMethod();
        }
    }

}
