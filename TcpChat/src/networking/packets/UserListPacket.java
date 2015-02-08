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
package networking.packets;

import networking.general.Packet;
import networking.general.PacketType;
import java.util.List;
import networking.general.UserListPacketType;

/**
 * Class for a specific packet type
 *
 * @author Manuel Schmid
 */
public class UserListPacket extends Packet {

    protected List<String> users;
    protected String user;
    private UserListPacketType ulPacketType = UserListPacketType.Full;

    /**
     * Constructor
     */
    public UserListPacket() {
        this.users = server.console.ChatServer.getUserList();
        this.ulPacketType = UserListPacketType.Full;
        this.packetIdentifier = PacketType.Userlist;
    }

    /**
     * Constructor for transmitting only changes
     *
     * @param user
     * @param ulPacketType
     */
    public UserListPacket(String user, UserListPacketType ulPacketType) {
        this.user = user;
        this.ulPacketType = ulPacketType;
        this.packetIdentifier = PacketType.Userlist;
    }

    /**
     * Getter for users
     *
     * @return users
     */
    public List<String> getUserList() {
        return this.users;
    }

    /**
     * Getter for user
     *
     * @return user
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Getter for UserListType
     *
     * @return userlist packet type
     */
    public UserListPacketType getUserListType() {
        return this.ulPacketType;
    }
}
