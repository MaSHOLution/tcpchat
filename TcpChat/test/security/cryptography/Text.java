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
package security.cryptography;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;
import security.basics.CryptoBasics;
import static org.junit.Assert.*;
import security.basics.SessionIdGenerator;
import security.cryptography.method.Aes;

/**
 * Tests for encrypting and decrypting text
 *
 * @author Manuel Schmid
 */
public class Text {

    private final String base = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut l";

    /**
     * Test for Aes Cipher class
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws java.io.UnsupportedEncodingException
     */
    @Test
    public void aesCipherTest() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException {

        String sessionId = new SessionIdGenerator().nextSessionId();
        // Make Byte-Array out of session id
        byte[] key = sessionId.getBytes("UTF-8");
        // Create SHA hash from array
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        // Create secret key
        // TODO Change SecretKeySpec creation
        key = sha.digest(key);
        key = Arrays.copyOf(key, CryptoBasics.encryption);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
    }

    /**
     * Test for encryption method aes
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    @Test
    public void aes() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        EncryptionMethod aes = new Aes();

        String encrypted = aes.encrypt(base);
        assertFalse(base.equals(encrypted));

        String decrypted = aes.decrypt(encrypted);
        assertEquals(decrypted, base);
    }

    /**
     * Test for no encryption method
     */
    @Test
    public void off() {
        EncryptionMethod encMethod = new EncryptionMethod();

        String encrypted = encMethod.encrypt(base);
        assertEquals(base, encrypted);

        String decrypted = encMethod.decrypt(encrypted);
        assertEquals(decrypted, base);
    }

    /**
     * Test of current encryption method
     *
     * @see CryptoBasics.encMethod
     */
    @Test
    public void current() {
        EncryptionMethod encMethod = CryptoBasics.makeEncryptionObject();

        String encrypted = encMethod.encrypt(base);
        String decrypted = encMethod.decrypt(encrypted);
        assertEquals(decrypted, base);
    }
}
