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
package client.gui.tabs;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * Class for a new chat tab
 *
 * @author Manuel Schmid
 */
public final class ChatTab {

    // Components
    private final ChatType chatType;
    private final ChatArea chatArea;

    // Parent
    private final JScrollPane scrollPane;
    private final JTabbedPane tabbedPane;
    private final TabController tabController;

    // Params
    private final int index;
    private final String title;
    private final List<String> persons;

    /**
     * Constructor
     *
     * @param chatType Type declared in enum ChatType
     * @param tabbedPane Pane which the ChatTab is associated with
     * @param tabController Controller of the tabs for ButtonTabComponent
     * @param persons persons of the tab
     */
    public ChatTab(ChatType chatType, JTabbedPane tabbedPane, TabController tabController, List<String> persons) {
        this.tabController = tabController;
        this.chatType = chatType;
        this.chatArea = new ChatArea();
        this.chatArea.setEditable(false);
        this.scrollPane = new JScrollPane(chatArea);
        this.index = tabbedPane.getTabCount();
        this.tabbedPane = tabbedPane;
        this.persons = persons;
        this.title = persons.get(0);
    }

    /**
     * Appends a message on the chat area
     *
     * @param message
     * @return
     */
    public ChatTab appendOnChatArea(String message) {
        chatArea.append(message);
        return this;
    }

    /**
     * Getter of scroll pane
     *
     * @return
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Getter of the index
     *
     * @return index of the element in the TabbedPane
     */
    public int getIndex() {
        return index;
    }

    /**
     * Getter of associated tabbed pane
     *
     * @return
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * Getter of ChatType of this ChatTab
     *
     * @return
     */
    public ChatType getChatType() {
        return chatType;
    }

    /**
     * Getter of the ChatArea
     *
     * @return
     */
    public ChatArea getChatArea() {
        return chatArea;
    }
    
    /**
     * Getter of the persons associated to the chat
     *
     * @return
     */
    public List<String> getPersons() {
        return persons;
    }

    /**
     * Scrolls to the bottom of the chatArea
     */
    public void scrollToBottom() {
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    /**
     * Removes this ChatTab from the tabbedPane
     */
    public void remove() {
        tabController.removeTab(index);
    }

    /**
     * Disables this tab
     */
    public void disable() {
        chatArea.setEnabled(false);
        scrollPane.setEnabled(false);
    }

    /**
     * Disables this tab and the TabbedPane
     */
    public void disableAll() {
        disable();
        this.tabbedPane.setEnabled(false);
    }
}
