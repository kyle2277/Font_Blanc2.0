package main.swing_gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Font_Blanc2_App {
    private JButton openFileButton;
    private JPanel panelMain;
    private JTextArea DnD_area;
    private JTextArea statusArea;
    private JFileChooser fc;

    public Font_Blanc2_App() {



        //open file button
        fc = new JFileChooser();
        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = fc.showOpenDialog(panelMain);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = fc.getSelectedFiles();
                    StringBuilder s = new StringBuilder("Files in queue:\n");
                    for(File f: files) {
                        s.append(f.getAbsolutePath());
                        s.append("\n");
                    }
                    String out = s.toString();
                    statusArea.setText(out);
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Font Blanc 2");
        Font_Blanc2_App app = new Font_Blanc2_App();
        f.setContentPane(app.panelMain);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        resetMain(app);

    }

    public static void resetMain(Font_Blanc2_App app) {
        app.statusArea.setText("");
    }
}
