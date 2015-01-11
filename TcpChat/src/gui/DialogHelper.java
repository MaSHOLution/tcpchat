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
package gui;

import javax.swing.Icon;
import javax.swing.JOptionPane;

/**
 *
 * @author Manuel Schmid
 */
public class DialogHelper {

    private javax.swing.JFrame frame = null;

    public DialogHelper(javax.swing.JFrame frame) {
        this.frame = frame;
    }

    public void showInfoDialog(String title, String content) {
        JOptionPane.showMessageDialog(frame,
                content,
                title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showWarningDialog(String title, String content) {
        JOptionPane.showMessageDialog(frame,
                content,
                title,
                JOptionPane.WARNING_MESSAGE);
    }

    public void showPlainDialog(String title, String content) {
        JOptionPane.showMessageDialog(frame,
                content,
                title,
                JOptionPane.PLAIN_MESSAGE);
    }

    public void showCustomDialog(String title, String content, Icon icon) {
        JOptionPane.showMessageDialog(frame,
                content,
                title,
                JOptionPane.INFORMATION_MESSAGE,
                icon);
    }
}
