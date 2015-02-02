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
        isInitialized = false;
        tabPane.setEnabled(false);
    }

    /**
     * Getter for isInitialized
     *
     * @return isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    public int addTab(String name, boolean addCloseElement) {
        ChatArea chatText = new ChatArea();
        chatText.setEditable(false);
        int counter = getTabCountForIndex();
        tabPane.add(name, new JScrollPane(chatText));
        if (addCloseElement) {
            int counter2 = getTabCountForIndex();
            initTabComponent(counter);
        }
        return getTabCountForIndex() - 1;
    }

    public boolean removeTab(String title) {
        for (int i = 1; i < getTabCountForIndex(); i++) {
            String tabTitle = tabPane.getTitleAt(i);
            if (title.equals(tabTitle)) {
                tabPane.remove(i);
                return true;
            }
        }
        return false;
    }

    public int getTabIndexByTitle(String title) {
        for (int i = 1; i < getTabCountForIndex(); i++) {
            String tabTitle = tabPane.getTitleAt(i);
            if (title.equals(tabTitle)) {
                return i;
            }
        }
        return -1;
    }

    public boolean setFocusAt(String title) {
        int index = getTabIndexByTitle(title);
        if(index == -1){
            return false;
        } else {
            tabPane.setSelectedIndex(index);
            return true;
        }
    }

    private void initTabComponent(int i) {
        tabPane.setTabComponentAt(i, new ButtonTabComponent(tabPane));
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
            if(tabIndex == -1){
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
        ChatArea chatArea = (ChatArea) ((JScrollPane) tabPane.getComponentAt(tabIndex)).getViewport().getView();
        chatArea.append("\n" + message);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private int getTabCountForIndex() {
        return tabPane.getTabCount();
    }
}
