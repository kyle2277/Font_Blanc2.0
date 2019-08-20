package main.swing_gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

public class Font_Blanc2_App {
    private JButton openFileButton;
    private JPanel panelMain;
    private JTextArea DnD_area;
    private JTextArea statusArea;
    private JButton resetButton;
    private JList fileList;
    private DefaultListModel lm;
    //todo remove selected button
    private JButton removeSelectedButton;
    private JFileChooser fc;
    private HashMap<String, String> fileMap;

    public Font_Blanc2_App() {

        fileMap = new LinkedHashMap<>();
        lm = new DefaultListModel();
        fileList.setModel(lm);

        //files drag and dropped
        new FileDrop(DnD_area, new FileDrop.Listener() {
           public void filesDropped(File[] files) {
               for(File f: files) {
                   String name = f.getName();
                   lm.addElement(name);
                   fileMap.put(name, f.getAbsolutePath());
               }
           }
        });

        //open file button
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = fc.showOpenDialog(panelMain);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    String name = f.getName();
                    lm.addElement(name);
                    fileMap.put(name, f.getAbsolutePath());
                }
            }
        });

        //reset button
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                resetMain();
            }
        });
        removeSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int[] ind = fileList.getSelectedIndices();
                if(ind.length > 0) {
                    int count = 0;
                    for(int i: ind) {
                        String fileName = (String) lm.getElementAt(i-count);
                        lm.removeElementAt(i-count);
                        fileMap.remove(fileName);
                        count++;
                    }
                }
            }
        });
    }

    //reset the app home frame
    public void resetMain() {
        if(!lm.isEmpty()) {
            lm.clear();
        }
        if(!fileMap.isEmpty()) {
            fileMap.clear();
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Font Blanc 2");
        Font_Blanc2_App app = new Font_Blanc2_App();
        f.setContentPane(app.panelMain);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        app.resetMain();
    }
}
