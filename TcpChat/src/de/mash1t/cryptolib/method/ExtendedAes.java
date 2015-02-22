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
package de.mash1t.cryptolib.method;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encryption method AES
 *
 * @see http://blog.axxg.de/java-aes-verschluesselung-mit-beispiel/
 * @author Manuel Schmid
 */
public final class ExtendedAes extends Aes {

    private SecretKeySpec secretKeySpec;

    /**
     * Constructor, makes a new secretKeySpec from the SessionId
     */
    public ExtendedAes() {
        this.makeKey();
    }

    @Override
    public String encrypt(String message) {
        try {
            return super.encrypt(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            // TODO handle exceptions
        } finally {
            de.mash1t.chat.logging.Counters.exception();
        }
        return null;
    }

    @Override
    public String decrypt(String message) {
        try {
            return super.decrypt(message);
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            // TODO handle exceptions
        } finally {
            de.mash1t.chat.logging.Counters.exception();
        }
        return null;
    }

    @Override
    public boolean makeKey() {
        boolean result = super.makeKey();
        if (result) {
            de.mash1t.chat.logging.Counters.exception();
        }
        return result;
    }
}
