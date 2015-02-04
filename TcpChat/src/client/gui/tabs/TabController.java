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
package client.gui;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 *
 * @author Manuel Schmid
 */
public final class TabController {

    private JTabbedPane tabPane;
    private boolean isInitialized = false;

    /**
     * Constructor
     *
     * @param tabPane
     */
    public TabController(JTabbedPane tabPane) {
        this.tabPane = tabPane;
    }

    /**
     * Initialize components
     */
    public void init() {
        tabPane.setEnabled(true);
        tabPane.removeAll();
        addTab("Group Chat", false);
        tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // tabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        isInitialized = true;
    }

    /**
     * Terminates components
     */
    public void terminate() {
        // Set isInitialized false
        isInitialized = false;
        // Disable TabbedPane
        tabPane.setEnabled(false);
        // Disable ChatArea at curreltny viewed tab
        getChat(tabPane.getSelectedIndex()).setEnabled(false);
    }

    /**
     * Getter for isInitialized
     *
     * @return isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Adds a new tab with ScrollPane and ChatArea to TabbedPane
     *
     * @param title title of the tab
     * @param addCloseElement add cross to close tab
     * @return
     */
    public int addTab(String title, boolean addCloseElement) {
        ChatArea chatText = new ChatArea();
        chatText.setEditable(false);
        int counter = tabPane.getTabCount();
        tabPane.add(title, new JScrollPane(chatText));
        if (addCloseElement) {
            int counter2 = tabPane.getTabCount();
            initTabComponent(counter);
        }
        return tabPane.getTabCount() - 1;
    }

    /**
     * Removes the tab with the given title
     *
     * @param title title of the tab
     * @return boolean if tab was removed
     */
    public boolean removeTab(String title) {
        for (int i = 1; i < tabPane.getTabCount(); i++) {
            String tabTitle = tabPane.getTitleAt(i);
            if (title.equals(tabTitle)) {
                tabPane.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for index of tab by title
     *
     * @param title title of the tab
     * @return index of tab or -1 if not found
     */
    public int getTabIndexByTitle(String title) {
        for (int i = 1; i < tabPane.getTabCount(); i++) {
            String tabTitle = tabPane.getTitleAt(i);
            if (title.equals(tabTitle)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets focus on a specific tab by title
     *
     * @param title title of the tab
     * @return
     */
    public boolean setFocusAt(String title) {
        int index = getTabIndexByTitle(title);
        if (index == -1) {
            return false;
        } else {
            tabPane.setSelectedIndex(index);
            return true;
        }
    }

    /**
     * Sets focus on a specific tab by index
     *
     * @param title title of the tab
     * @return
     */
    public boolean setFocusAt(int index) {
        // Check if tab index is valid
        if (index >= tabPane.getTabCount()) {
            // Tab index can not be bigger than countof tabs, because index is max count -1
            return false;
        } else {
            // Select tab at index
            tabPane.setSelectedIndex(index);
            return true;
        }
    }

    /**
     * Initializes a new closing element for the tab (the cross)
     *
     * @param index index of the tab where cross should be displayed
     */
    private void initTabComponent(int index) {
        tabPane.setTabComponentAt(index, new ButtonTabComponent(tabPane));
    }

    /**
     * Output a message on the currently selected tab
     *
     * @param message message to show on gui
     */
    public synchronized void outputLineOnGui(String message) {
        if (!message.trim().equals("")) {
            appendTextToChat(message, tabPane.getSelectedIndex());
        }
    }

    /**
     * Output a message on the chat text area
     *
     * @param message message to show on gui
     * @param person person of the message
     * @return tab was created
     */
    public synchronized boolean outputLineOnGui(String message, String person) {
        boolean tabCreated = false;
        if (!message.trim().equals("")) {
            int tabIndex = getTabIndexByTitle(person);
            if (tabIndex == -1) {
                tabIndex = addTab(person, true);
                tabCreated = true;
            }
            appendTextToChat(message, tabIndex);
        }
        return tabCreated;
    }

    /**
     * Output a message on a tab with given index
     *
     * @param message message to show on gui
     * @param tabIndex index of tab to show message in
     */
    public synchronized void outputLineOnGui(String message, int tabIndex) {
        if (!message.trim().equals("")) {
            appendTextToChat(message, tabIndex);
        }
    }

    /**
     * Appends a String to the ChatArea at the given index
     *
     * @param message message to append
     */
    public void appendTextToChat(String message, int tabIndex) {
        ChatArea chatArea = getChat(tabIndex);
        chatArea.append("\n" + message);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    /**
     * Returns the ChatArea at tabIndex
     *
     * @param index index of tab
     * @return
     */
    private ChatArea getChat(int tabIndex) {
        return (ChatArea) ((JScrollPane) tabPane.getComponentAt(tabIndex)).getViewport().getView();
    }
}
