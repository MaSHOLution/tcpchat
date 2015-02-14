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
package de.mash1t.chat.client.gui.userlist;

import de.mash1t.chat.client.gui.tabs.TabController;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import de.mash1t.chat.networking.packets.UserListPacket;

/**
 * Controller for the userlist
 *
 * @author Manuel Schmid
 */
public final class UserListController {

    private final JList lbUsers;
    private final DefaultListModel listModel;
    private final TabController tabController;

    /**
     * Constructor
     *
     * @param lbUsers userlist on gui
     * @param tabController
     */
    public UserListController(JList lbUsers, TabController tabController) {
        this.lbUsers = lbUsers;
        this.tabController = tabController;
        this.listModel = ((DefaultListModel) lbUsers.getModel());
    }

    /**
     * Removes all entries from the list
     */
    public void clearList() {
        listModel.removeAllElements();
    }

    /**
     * Updates userlist and existing private message tabs
     *
     * @param ulPacket
     */
    public void updateUserList(UserListPacket ulPacket) {
        String name = ulPacket.getUser();
        switch (ulPacket.getUserListType()) {
            case Connected:
                listModel.addElement(name);
                appendInfoMessage("*** User \"" + name + "\" joined ***", name);
                sortUserList();
                break;
            case Disconnected:
                listModel.removeElement(name);
                appendInfoMessage("*** User \"" + name + "\" left ***", name);
                break;

            case Full:
                clearList();
                // TODO sort users
                List<String> userlist = ulPacket.getUserList();
                Collections.sort(userlist);
                for (String user : userlist) {
                    listModel.addElement(user);
                }
        }
    }

    /**
     * Writes a line to an existing private message tab
     *
     * @param message
     * @param name
     */
    private void appendInfoMessage(String message, String name) {
        int index = tabController.getTabIndexByTitle(name);
        if (index != -1) {
            tabController.appendTextToChat(message, index);
        }
    }

    /**
     * Sort the userlist alphabetically
     */
    private void sortUserList() {
        String[] names = new String[listModel.getSize()];
        for (int i = 0; i < names.length; i++) {
            names[i] = listModel.getElementAt(i).toString();
        }
        Arrays.sort(names);
        clearList();
        for (String name : names) {
            listModel.addElement(name);
        }
    }
}
