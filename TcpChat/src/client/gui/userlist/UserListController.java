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
package client.gui.userlist;

import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * Controller for the userlist
 * 
 * @author Manuel Schmid
 */
public final class UserListController {

    private final JList lbUsers;
    private final DefaultListModel listModel;

    /**
     * Constructor
     *
     * @param lbUsers userlist on gui
     */
    public UserListController(JList lbUsers) {
        this.lbUsers = lbUsers;
        this.listModel = ((DefaultListModel) lbUsers.getModel());
    }

    /**
     * Removes all entries from the list
     */
    public void clearList() {
        listModel.removeAllElements();
    }

    /**
     * Updates the user list
     *
     * @param users
     */
    public void updateUserList(List<String> users) {
        clearList();
        // TODO sort users
        for (String user : users) {
            listModel.addElement(user);
        }
    }
}
