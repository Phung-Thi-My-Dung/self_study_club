package com.selfstudyclub.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Small Swing utilities. */
public final class UiUtil {
    private UiUtil() {}

    public static void bindEscToBack(JComponent component, Runnable backAction) {
        InputMap im = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = component.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "escBack");
        am.put("escBack", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { backAction.run(); }
        });
    }

    public static JPanel pad(Component inner) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
