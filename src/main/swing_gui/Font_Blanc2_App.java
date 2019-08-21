package main.swing_gui;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class Font_Blanc2_App {
    private JButton openFileButton;
    private JPanel panelMain;
    private JTextArea DnD_area;
    private JButton advancedReset;
    private JList fileList;
    private DefaultListModel lm;
    private JButton removeSelectedButton;
    private JButton preferencesButton;
    private JCheckBox advancedCheckBox;
    private JTextArea statusArea;
    private JButton copyPrefsButton;
    private JTextField outputField;
    private JCheckBox outputCheckbox;
    private JPasswordField keyField;
    private JButton simpleGO;
    private JButton advancedGO;
    private JButton setOutputButton;
    private JLabel keyLabel;
    private JLabel outputLabel;
    private JButton simpleReset;
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

        //advanced reset button
        advancedReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                resetAdvanced();
            }
        });
        //simple reset button
        simpleReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                resetSimple();
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
        preferencesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int ind = fileList.getSelectedIndex();
                if(ind > 0) { // none selected

                }
            }
        });
        advancedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean selected = advancedCheckBox.isSelected();
                if(selected) {
                    showAdvanced();
                } else {
                    showSimple();
                }
            }
        });
        outputCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean selected = outputCheckbox.isSelected();
                if(selected) {
                    setOutputButton.setEnabled(false);
                    //set output field to file output
                } else {
                    setOutputButton.setEnabled(true);
                }
            }
        });
    }

    //reset the app home frame
    public void resetAdvanced() {
        if(!lm.isEmpty()) {
            lm.clear();
        }
        if(!fileMap.isEmpty()) {
            fileMap.clear();
        }
    }

    public void resetSimple() {
        keyField.setText("");
        outputField.setText("");
        outputCheckbox.setSelected(true);
        setOutputButton.setEnabled(false);
    }

    public void showSimple() {
        hideAdvanced();
        keyLabel.setVisible(true);
        keyField.setVisible(true);
        keyField.setText("");
        outputLabel.setVisible(true);
        outputCheckbox.setVisible(true);
        outputCheckbox.setSelected(true);
        outputField.setVisible(true);
        outputField.setText("");
        setOutputButton.setVisible(true);
        setOutputButton.setEnabled(false);
        statusArea.setVisible(true);
        simpleGO.setVisible(true);
        simpleReset.setVisible(true);
    }

    public void showAdvanced() {
        hideSimple();
        fileList.setVisible(true);
        if(!lm.isEmpty()) {
            lm.clear();
        }
        if(!fileMap.isEmpty()) {
            fileMap.clear();
        }
        openFileButton.setVisible(true);
        removeSelectedButton.setVisible(true);
        preferencesButton.setVisible(true);
        copyPrefsButton.setVisible(true);
        advancedGO.setVisible(true);
        advancedReset.setVisible(true);
    }

    public void hideSimple() {
        keyLabel.setVisible(false);
        keyField.setVisible(false);
        outputLabel.setVisible(false);
        outputCheckbox.setVisible(false);
        outputField.setVisible(false);
        setOutputButton.setVisible(false);
        statusArea.setVisible(false);
        simpleGO.setVisible(false);
        simpleReset.setVisible(false);
    }

    public void hideAdvanced() {
        fileList.setVisible(false);
        openFileButton.setVisible(false);
        removeSelectedButton.setVisible(false);
        preferencesButton.setVisible(false);
        copyPrefsButton.setVisible(false);
        advancedGO.setVisible(false);
        advancedReset.setVisible(false);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Font Blanc 2");
        Font_Blanc2_App app = new Font_Blanc2_App();
        f.setContentPane(app.panelMain);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        //f.setSize(600,400);
        f.setVisible(true);
        app.showSimple();
    }
}
