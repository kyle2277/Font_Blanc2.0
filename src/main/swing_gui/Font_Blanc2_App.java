package main.swing_gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import main.encryption_engine.*;

public class Font_Blanc2_App {
    private JPanel panelMain;
    private JTextArea DnD_area;
    private JList fileList;
    private DefaultListModel lm;
    private JButton removeInstructionButton;
    private JCheckBox advancedCheckBox;
    private JTextArea statusArea;
    private JTextField outputField;
    private JCheckBox outputCheckbox;
    private JPasswordField keyField;
    private JButton simpleRun;
    private JButton advancedRun;
    private JButton setOutputButton;
    private JLabel keyLabel;
    private JLabel outputLabel;
    private JButton Reset;
    private JRadioButton encryptRadioButton;
    private JRadioButton decryptRadioButton;
    private JButton openFileSimple;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JButton extButton;
    private JLabel errorPathLabel;
    private JCheckBox extCheckBox;
    private JRadioButton fixedRadioButton;
    private JRadioButton flexibleRadioButton;
    private JTextField fixedDimField;
    private JPanel radioDimPanel;
    private JPasswordField advKeyInput;
    private JButton addInstructionButton;
    private JPanel advPanel;
    private JPanel encryptRadioPanel;
    private JFileChooser fc;
    private JFileChooser dc; //directory chooser
    private FilePreferences curFile;
    private Globals g;
    private boolean running;
    private static final String DEFAULT_EXTENSION = ".fb2";

    public Font_Blanc2_App() {

        /*
         ******************************************ADVANCED FUNCIONALITY*************************************************
         */

        //set advanced borders
        TitledBorder dimInputBorder = BorderFactory.createTitledBorder("Dimension (max 1024)");
        fixedDimField.setBorder(dimInputBorder);
        TitledBorder advRadioBorder = BorderFactory.createTitledBorder("Dimension Type");
        radioDimPanel.setBorder(advRadioBorder);
        TitledBorder advKeyBorder = BorderFactory.createTitledBorder("Encrypt Key");
        advKeyInput.setBorder(advKeyBorder);
        TitledBorder encryptBorder = BorderFactory.createTitledBorder("Mode");
        encryptRadioPanel.setBorder(encryptBorder);

        lm = new DefaultListModel();
        fileList.setModel(lm);

        //show or hide advanced functionality
        advancedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(!isRunning()) {
                    boolean selected = advancedCheckBox.isSelected();
                    if(selected) {
                        showAdvanced();
                    } else {
                        hideAdvanced();
                    }
                } else {
                    runningError();
                }
            }
        });

        //remove a file from the queue
        removeInstructionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int[] ind = fileList.getSelectedIndices();
                if(ind.length > 0) {
                    int count = 0;
                    for(int i: ind) {
                        lm.removeElementAt(i-count);
                        count++;
                    }
                }
            }
        });

        addInstructionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int dimension = 0;
                char[] key;
                if(fixedRadioButton.isSelected()) {
                    String dim = fixedDimField.getText();
                    try {
                        dimension = Math.abs(Integer.parseInt(dim));
                    } catch(NumberFormatException e) {
                        fixedDimField.setBackground(Color.red);
                        return;
                    }
                }
                key = advKeyInput.getPassword();
                if(key.length == 0) {
                    advKeyInput.setBackground(Color.red);
                    return;
                }
                Instruction i = new Instruction(dimension, key);
                lm.addElement(i);
            }
        });

        fixedDimField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                super.focusGained(focusEvent);
                fixedDimField.setBackground(Color.white);
            }
        });

        advKeyInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                super.focusGained(focusEvent);
                advKeyInput.setBackground(Color.white);
            }
        });

        flexibleRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(flexibleRadioButton.isSelected()) {
                    fixedDimField.setEnabled(false);
                }
            }
        });

        fixedRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(fixedRadioButton.isSelected()) {
                    fixedDimField.setEnabled(true);
                }
            }
        });

        advancedRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(!isRunning()) {
                    if(curFile != null) {
                        curFile.setOutPath(outputField.getText());
                        if(curFile != null && curFile.getOutPath() != null) {
                            curFile.setInstructions(generateInstructions());
                        } else {
                            outputField.setBackground(Color.red);
                            errorPathLabel.setVisible(true);
                            return;
                        }
                        if(curFile != null && curFile.getFileName() != null && curFile.getInPath() != null
                                && curFile.getOutPath() != null && curFile.getInstructions() != null) {
                            //create cipher
                            progressCipher c = new progressCipher(g, curFile.getFileName(), curFile.getInPath(), curFile.getOutPath(),
                                    curFile.isEncrypt(), curFile.getInstructions(), Font_Blanc2_App.this);
                            //run thread
                            Thread t = new Thread(c);
                            t.start();
                        }
                    } else {
                        noFileError();
                    }
                } else {
                    runningError();
                }
            }
        });

        /*
         ***********************************END ADVANCED FUNCTIONALITY**************************************************
         */

        fc = new JFileChooser();
        curFile = null;
        String logPath = new File(".").getAbsolutePath();
        logPath = logPath.substring(0, logPath.length()-1);
//        g = new Globals("e_", "d_", DEFAULT_EXTENSION, logPath + "log.txt");
        g = new Globals(DEFAULT_EXTENSION, logPath + "log.txt");
        running = false;

        //files drag and dropped
        new FileDrop(DnD_area, new FileDrop.Listener() {
            public void filesDropped(File[] files) {
                if(curFile != null) {
                    cleanCurFile();
                }
                File f = files[0];
                curFile = new FilePreferences(f.getAbsolutePath(), encryptRadioButton.isSelected(), f.length());
                outputField.setText(curFile.getOutPath());
                setStatus();
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
                    curFile = new FilePreferences(f.getAbsolutePath(), encryptRadioButton.isSelected(), f.length());
                    outputField.setText(curFile.getOutPath());
                    setStatus();
                }
            }
        });

        //switch between using default extension and user-specified extension
        extCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean selected = extCheckBox.isSelected();
                if(selected) {
                    g.setEncryptExt(DEFAULT_EXTENSION);
                    extButton.setEnabled(false);
                    setStatus();
                } else {
                    extButton.setEnabled(true);
                }
            }
        });

        //set the output extension
        extButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String s = (String)JOptionPane.showInputDialog(panelMain, "Extension:", "Set Extension", JOptionPane.PLAIN_MESSAGE, null, null, g.encryptExt);
                if((s != null) && (s.length() > 0)) {
                    g.setEncryptExt(s);
                    setStatus();
                }
                //if options are changed to empty strings do not update fields in g
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
                    String path = f.getAbsolutePath() + "/";
                    outputField.setText(path);
                    curFile.setOutPath(path);
                    setStatus();
                }
            }
        });

        //simple reset button
        Reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(!isRunning()) {
                    if(advancedCheckBox.isSelected()) { //reset advanced
                        //clear preferences list
                        resetAdvanced();
                    }
                    resetSimple();
                } else {
                    runningError();
                }

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

        //restore background to white when focus regained
        outputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                super.focusGained(focusEvent);
                outputField.setBackground(Color.white);
                errorPathLabel.setVisible(false);
            }
        });

        //update file object encrypt key when focus leaves field
        keyField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                super.focusLost(focusEvent);
                checkKeyField();
            }
        });

        //restore background to white when focus gained
        keyField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                super.focusGained(focusEvent);
                keyField.setBackground(Color.white);
            }
        });

        //initiate encryption/decryption process
        simpleRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //check curFile has no null fields
                if(!isRunning()) {
                    if(curFile != null) {
                        //check encrypt key length
                        if((curFile.getEncryptKey() == null) || (!(checkKeyField()))) {
                            keyField.setBackground(Color.red);
                            return;
                        }
                        if(checkOutputField()) {
                            panelMain.setEnabled(false);
                            if (curFile.getFileName() != null && curFile.getInPath() != null
                                    && curFile.getOutPath() != null && curFile.getEncryptKey() != null) {
                                //**********DEFAULT INSTRUCTIONS*****************************************
                                //one layer fixed dimension at n = 1024
                                //one layer flexible dimension
                                Deque<Instruction> instructions = new LinkedList<>();
                                instructions.add(new Instruction(1024, curFile.getEncryptKey()));
                                instructions.add(new Instruction(0, curFile.getEncryptKey()));
                                //***********************************************************************
                                progressCipher c = new progressCipher(g, curFile.getFileName(), curFile.getInPath(), curFile.getOutPath(),
                                        curFile.isEncrypt(), instructions, Font_Blanc2_App.this);
                                //run thread
                                Thread t = new Thread(c);
                                t.start();
                            }
                        }
                    } else {
                        noFileError();
                    }
                } else {
                    runningError();
                }
            }
        });

    }

    /*
     ******************************************ADVANCED FUNCTIONS******************************************************
     */

    //set progress bar level
    public void setProgressBar(int val) {
        progressBar.setValue(val);
    }

    //set progress bar label text
    public void setProgressLabel(String s) {
        progressLabel.setText(s);
    }

    //check if the encryption engine is currently running
    public boolean isRunning() {
        return running;
    }

    //set status of encryption engine as running or not running
    public void setRunning(boolean running) {
        this.running = running;
    }

    //reset the app home frame
    public void resetAdvanced() {
        if(!lm.isEmpty()) {
            lm.clear();
        }
        fixedDimField.setText("");
        advKeyInput.setText("");
        fixedDimField.setBackground(Color.white);
        advKeyInput.setBackground(Color.white);
        flexibleRadioButton.setSelected(true);
        fixedDimField.setEnabled(false);
        progressBar.setValue(0);
        progressLabel.setText("");
        //clear fields
    }

    public void showAdvanced() {
        resetAdvanced();
        advPanel.setVisible(true);
        keyField.setText("");
        keyField.setEnabled(false);
        keyLabel.setEnabled(false);
        simpleRun.setVisible(false);
    }

    public void hideAdvanced() {
        resetSimple();
        advPanel.setVisible(false);
        keyLabel.setEnabled(true);
        keyField.setEnabled(true);
        simpleRun.setVisible(true);
    }

    /*
     ***********************************END ADVANCED FUNCTIONS**********************************************************
     */

    public void resetSimple() {
        //reset curFile
        cleanCurFile();
        setStatus();
        keyField.setBackground(Color.white);
        outputField.setBackground(Color.white);
        errorPathLabel.setVisible(false);
        g.setEncryptExt(DEFAULT_EXTENSION);
        extCheckBox.setSelected(true);
        extButton.setEnabled(false);
        encryptRadioButton.setSelected(true);
        progressBar.setValue(0);
        progressLabel.setText("");
        keyField.setText("");
        outputField.setText("");
        outputCheckbox.setSelected(true);
        outputField.setEditable(false);
        setOutputButton.setEnabled(false);
    }

    public void setStatus() {
        if(curFile != null) {
            String s = "";
            s += curFile.getFileName();
            s += "\nIn: ";
            s += curFile.getInPath();
            s += "\nOut: ";
            s += curFile.getOutPath();
            s += "\nFile size: ";
            s += curFile.getFileLength();
            s += " bytes";
            s += "\nEncrypt: ";
            s += curFile.isEncrypt();
            s += "\nKey: ";
            s += Arrays.toString(curFile.getEncryptKey());
            s += "\nEncrypted extension: ";
            s += g.encryptExt;
            statusArea.setText(s);
        } else {
            statusArea.setText("No File Added.");
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
            FilePreferences.clean(curFile);
        }
        curFile = null;
    }

    public Deque<Instruction> generateInstructions() {
        Deque<Instruction> instructions = new LinkedList<>();
        for(int i = 0; i < lm.getSize(); i++) {
            Instruction cur = (Instruction) lm.getElementAt(i);
            if((cur.getDimension() > 0) && cur.getDimension() > Cipher.MAX_DIMENSION) {
                cur.setDimension(Cipher.MAX_DIMENSION);
            }
            instructions.add(cur);
        }
        return instructions;
    }

    public boolean checkOutputField() {
        curFile.setOutPath(outputField.getText());
        if (curFile.getOutPath() == null) {
            outputField.setBackground(Color.red);
            errorPathLabel.setVisible(true);
            return false;
        } else {
            return true;
        }
    }

    public void runningError() {
        JOptionPane.showMessageDialog(panelMain, "Busy.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void noFileError() {
        JOptionPane.showMessageDialog(panelMain, "No file added.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean checkKeyField() {
        char[] encryptKey = keyField.getPassword();
        int len = encryptKey.length;
        if (len >= 5 && len <= 20) {
            curFile.setEncryptKey(encryptKey);
            setStatus();
            return true;
        } else {
            return keyLengthError();
        }
    }

    public boolean keyLengthError() {
        curFile.setEncryptKey(null);
        keyField.setBackground(Color.red);
        JOptionPane.showMessageDialog(panelMain, "Key must be between 5 and 20 characters long.",
                "Error", JOptionPane.ERROR_MESSAGE);
        return false;

    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Font Blanc 2");
        Font_Blanc2_App app = new Font_Blanc2_App();
        f.setContentPane(app.panelMain);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        //f.setSize(600,400);
        f.setVisible(true);
        app.hideAdvanced();
    }
}