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
package common.networking;

import common.networking.packets.GroupMessagePacket;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Manuel Schmid
 */
public class Transfer {

    private final String testMessage = "Test1";

    /**
     * Test for writing an object to a stream
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void write() throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream("msgPackage.tmp");
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(new GroupMessagePacket(this.testMessage));
        oos.close();
    }

    /**
     * Test for reading an object from a stream
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws java.lang.ClassNotFoundException
     */
    @Test
    public void read() throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("msgPackage.tmp");
        ObjectInputStream ois = new ObjectInputStream(fis);

        GroupMessagePacket msgPacket = (GroupMessagePacket) ois.readObject();
        ois.close();

        assertEquals(msgPacket.getMessage(), this.testMessage);
    }
}
