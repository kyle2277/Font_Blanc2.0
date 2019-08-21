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
    private JRadioButton encryptRadioButton;
    private JRadioButton decryptRadioButton;
    private JButton openFileSimple;
    private JFileChooser fc;
    private JFileChooser dc; //directory chooser
    private HashMap<String, FilePreferences> fileMap;
    private FilePreferences curFile;

    public Font_Blanc2_App() {


        /*
         ******************************************ADVANCED FUNCIONALITY*************************************************
         */

        fileMap = new LinkedHashMap<>();
        lm = new DefaultListModel();
        fileList.setModel(lm);

        //show or hide advanced functionality
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
                    fileMap.put(name, new FilePreferences(f.getAbsolutePath(), true));
                }
            }
        });

        //remove a file from the queue
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

        //set preferences for one file
        preferencesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int ind = fileList.getSelectedIndex();
                if(ind > 0) { // none selected

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

        /*
         ***********************************END ADVANCED FUNCTIONALITY**************************************************
         */

        curFile = null;

        //files drag and dropped
        new FileDrop(DnD_area, new FileDrop.Listener() {
            public void filesDropped(File[] files) {
                if(advancedCheckBox.isSelected()) {
                    //advanced layout
                    for(File f: files) {
                        String name = f.getName();
                        lm.addElement(name);
                        fileMap.put(name, new FilePreferences(f.getAbsolutePath(), true));
                    }
                } else {
                    //simple layout
                    File f = files[0];
                    curFile = new FilePreferences(f.getAbsolutePath(), encryptRadioButton.isSelected());
                    outputField.setText(curFile.getOutPath());
                    setStatus();
                }
            }
        });

        //radio button menu to select encryption or decryption process
        encryptRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                checkEncrypt(encryptRadioButton.isSelected());
                setStatus();
            }
        });
        decryptRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                checkEncrypt(encryptRadioButton.isSelected());
                setStatus();
            }
        });

        //select file from file browser
        openFileSimple.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = fc.showOpenDialog(panelMain);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    curFile = new FilePreferences(f.getAbsolutePath(), encryptRadioButton.isSelected());
                    outputField.setText(curFile.getOutPath());
                    setStatus();
                }
            }
        });

        //select whether to use input directory as output directory
        outputCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean selected = outputCheckbox.isSelected();
                if(selected) {
                    setOutputButton.setEnabled(false);
                    if(curFile != null) {
                        String out = curFile.getInPath();
                        curFile.setOutPath(out);
                        outputField.setText(out);
                        setStatus();
                    }
                    outputField.setEditable(false);
                    //set output field to file output
                } else {
                    setOutputButton.setEnabled(true);
                    outputField.setEditable(true);
                }
            }
        });

        //manually choose directory for output
        dc = new JFileChooser();
        dc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setOutputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = dc.showOpenDialog(panelMain);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = dc.getSelectedFile();
                    String path = f.getAbsolutePath();
                    outputField.setText(path);
                    setStatus();
                }
            }
        });

        //simple reset button
        simpleReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                resetSimple();
            }
        });

        //update file object output path when focus leaves field
        outputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                super.focusLost(focusEvent);
                String output = outputField.getText();
                if(curFile != null) {
                    curFile.setOutPath(output);
                    setStatus();
                }
            }
        });

        //update file object encrypt key when focus leaves field
        keyField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                super.focusLost(focusEvent);
                if(curFile != null) {
                    char[] encryptKey = keyField.getPassword();
                    curFile.setEncryptKey(encryptKey);
                }
            }
        });
    }

    /*
     ******************************************ADVANCED FUNCTIONS******************************************************
     */

    //reset the app home frame
    public void resetAdvanced() {
        if(!lm.isEmpty()) {
            lm.clear();
        }
        if(!fileMap.isEmpty()) {
            fileMap.clear();
        }
    }

    public void showAdvanced() {
        hideSimple();
        fileList.setVisible(true);
        openFileButton.setVisible(true);
        removeSelectedButton.setVisible(true);
        preferencesButton.setVisible(true);
        copyPrefsButton.setVisible(true);
        advancedGO.setVisible(true);
        advancedReset.setVisible(true);
    }

    public void hideAdvanced() {
        if(!lm.isEmpty()) {
            lm.clear();
        }
        if(!fileMap.isEmpty()) {
            fileMap.clear();
        }
        fileList.setVisible(false);
        openFileButton.setVisible(false);
        removeSelectedButton.setVisible(false);
        preferencesButton.setVisible(false);
        copyPrefsButton.setVisible(false);
        advancedGO.setVisible(false);
        advancedReset.setVisible(false);
    }

    /*
     ***********************************END ADVANCED FUNCTIONS**********************************************************
     */

    public void resetSimple() {
        //reset curFile
        cleanCurFile();
        setStatus();
        encryptRadioButton.setSelected(true);
        keyField.setText("");
        outputField.setText("");
        outputCheckbox.setSelected(true);
        setOutputButton.setEnabled(false);
    }

    public void showSimple() {
        hideAdvanced();
        openFileSimple.setVisible(true);
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

    public void hideSimple() {
        //reset curFile
        resetSimple();
        openFileSimple.setVisible(false);
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

    public void setStatus() {
        if(curFile != null) {
            StringBuilder s = new StringBuilder();
            s.append(curFile.getFileName());
            s.append("\n");
            s.append(curFile.getOutPath());
            s.append("\n");
            s.append(curFile.isEncrypt());
            String status = s.toString();
            statusArea.setText(status);
        } else {
            statusArea.setText("");
        }

    }

    public void checkEncrypt(boolean encrypt) {
        if(curFile != null) {
            if(curFile.isEncrypt() != encrypt) {
                curFile.setEncrypt(encrypt);
            }
        }
    }

    public void cleanCurFile() {
        if(curFile != null && curFile.getEncryptKey() != null && curFile.getEncryptKey().length > 0) {
            Arrays.fill(curFile.getEncryptKey(), '0');
        }
        curFile = null;
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
