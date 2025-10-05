package edu.ucol.ygo;

import edu.ucol.ygo.ui.MainWindow;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}