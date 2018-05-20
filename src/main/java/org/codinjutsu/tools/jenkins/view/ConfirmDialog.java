package org.codinjutsu.tools.jenkins.view;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConfirmDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextPane message;
    private Boolean result = false;

    public ConfirmDialog() {
        setContentPane(contentPane);
        message.setName("message");
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    protected void setMessage(String message) {
        this.message.setText(message);
    }

    private void onOK() {
        result = true;
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        result = false;
        dispose();
    }


    public static Boolean confirm(String message) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setMessage(message);
        dialog.pack();
        dialog.setVisible(true);
        return dialog.result;
    }
}
