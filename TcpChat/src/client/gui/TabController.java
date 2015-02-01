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
import javax.swing.JTextArea;

/**
 *
 * @author Manuel Schmid
 */
public final class TabController {

    private JTabbedPane tabPane;

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
        tabPane.removeAll();
        addTab("Group Chat", false);
        tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // tabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
    }

    public int addTab(String name, boolean addCloseElement) {
        JTextArea taChat = new JTextArea();
        taChat.setRows(5);
        taChat.setColumns(20);
        int counter = getTabCountForIndex();
        tabPane.add(name, new JScrollPane(taChat));
        if (addCloseElement) {
            int counter2 = getTabCountForIndex();
            initTabComponent(counter);
        }
        return getTabCountForIndex() -1;
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

    public int getTabByTitle(String title) {
        for (int i = 1; i < getTabCountForIndex(); i++) {
            String tabTitle = tabPane.getTitleAt(i);
            if (title.equals(tabTitle)) {
                return i;
            }
        }
        return -1;
    }

    private void initTabComponent(int i) {
        tabPane.setTabComponentAt(i, new ButtonTabComponent(tabPane));
    }

    public JTextArea getTextAreaOnTab(int tabIndex) {
        return (JTextArea) ((JScrollPane) tabPane.getComponentAt(tabIndex)).getViewport().getView();
    }

    private int getTabCountForIndex() {
        return tabPane.getTabCount();
    }
}
