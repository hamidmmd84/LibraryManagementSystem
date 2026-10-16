package com.example.library;

import com.example.library.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use Swing's default look and feel if the system look and feel is unavailable.
            }

            Font persianFont = new Font("Tahoma", Font.PLAIN, 13);
            String[] fontKeys = {"Label.font", "Button.font", "TextField.font", "PasswordField.font",
                    "TextArea.font", "Table.font", "TableHeader.font", "ComboBox.font", "TabbedPane.font",
                    "OptionPane.messageFont", "OptionPane.buttonFont"};
            for (String key : fontKeys) UIManager.put(key, persianFont);

            new MainFrame().setVisible(true);
        });
    }
}
