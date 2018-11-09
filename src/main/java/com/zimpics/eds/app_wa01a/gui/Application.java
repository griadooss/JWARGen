package com.zimpics.eds.app_wa01a.gui;

import com.zimpics.eds.app_wa01a.TPFile;
import com.zimpics.eds.app_wa01a.helpers.TPFileHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class Application extends JFrame {

    private JTextField fnameTextField;
    private JTextField dtprocessedTextField;
    private JTextField fstatusTextField;
    private JTextField noenqTextField;
    private JTextField nocallTextField;
    private JTextField notimesrunTextField;

    private DefaultListModel<TPFile> tpFileDefaultListModel;
    private JList<TPFile> tpFileJList;

    // private JMenuBar menuBar;

    private Action validateAction;
    private Action uploadAction;
    private Action reportsAction;
    private Action refreshAction;
    private Action newAction;
    private Action saveAction;
    private Action deleteAction;

    private TPFile selectedFile;


    public Application() {
        initMenu();
        initActions();
        initComponents();

        refreshData();
    }

    private JComponent createEditor() {
        final JPanel panel = new JPanel(new GridBagLayout());

        // Id - TPFile Name
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("TPFile Name"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        fnameTextField = new JTextField();
        fnameTextField.setEditable(false);
        panel.add(fnameTextField, constraints);

        // Date Processed
        constraints = new GridBagConstraints();
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Date Processed"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        dtprocessedTextField = new JTextField();
        panel.add(dtprocessedTextField, constraints);

        // Status
        constraints = new GridBagConstraints();
        constraints.gridy = 3;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Status"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        fstatusTextField = new JTextField();
        panel.add(fstatusTextField, constraints);

        //Count Enquiries
        constraints = new GridBagConstraints();
        constraints.gridy = 4;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Enquiries"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        noenqTextField = new JTextField();
        panel.add(noenqTextField, constraints);

        //Count Calls
        constraints = new GridBagConstraints();
        constraints.gridy = 5;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Calls"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        nocallTextField = new JTextField();
        panel.add(nocallTextField, constraints);

        //Times run
        constraints = new GridBagConstraints();
        constraints.gridy = 6;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Times Processed"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        notimesrunTextField = new JTextField();
        panel.add(notimesrunTextField, constraints);
        return panel;
    }


    //Method to create a list
    private JComponent createListPane() {
        tpFileDefaultListModel = new DefaultListModel<>();
        tpFileJList = new JList<>(tpFileDefaultListModel);
        //need to add a listener to the list so that we can populate editor fields in the GUI when selecting a file
        tpFileJList.getSelectionModel().addListSelectionListener(event -> { //invoked evertime selction changes
            if (!event.getValueIsAdjusting()) {
                //11:10 vid part11
                //we only want to process the event only when it has been adjusted!
                //the value change may be triggered more than once .. but only once is it adjusted
                //and then we will get the selectedFile value
                //TPFile selectedFile = xlSheetJList.getSelectedValue();
                setSelectedFile(tpFileJList.getSelectedValue());
            }
        });
        return new JScrollPane(tpFileJList);
    }

    private void setSelectedFile(final TPFile selected) {
        this.selectedFile = selected;
        if (selected == null) {

            fnameTextField.setText("");
            dtprocessedTextField.setText("");
            fstatusTextField.setText("");
            noenqTextField.setText("");
            nocallTextField.setText("");
            notimesrunTextField.setText("");
        } else {
            fnameTextField.setText(selectedFile.getfName());
            dtprocessedTextField.setText(selectedFile.getDtProcessed());
            fstatusTextField.setText(selectedFile.getfStatus());
            noenqTextField.setText(String.valueOf(selectedFile.getEnquiries()));
            nocallTextField.setText(String.valueOf(selectedFile.getCalls()));
            notimesrunTextField.setText(String.valueOf(selectedFile.getfTimesRun()));
        }

    }

    private void createNew() throws SQLException {
        //Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showSaveDialog(Application.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            CopyOption[] options = new CopyOption[]{
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            };
            Path FROM = Paths.get(file.getAbsolutePath());
            Path TO = Paths.get("target/" + file.getName());

            try {
                Files.copy(FROM, TO, options);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "An error occurred saving " + "'" + file.getName() + "!", "File Not Saved", JOptionPane.ERROR_MESSAGE);
                //     e.printStackTrace();
            }

            TPFile f = new TPFile();
            f.setfName(file.getName());
            f.setDtProcessed(String.valueOf(new Date()));
            f.setfStatus("Cached");
            f.setEnquiries(0);
            f.setCalls(0);
            f.setfTimesRun(0);
            setSelectedFile(f);


            JOptionPane.showMessageDialog(this, "Selected file " + "'" + file.getName() + ". Press \'save\' to queue it for processing.", "New File Selected", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Saving command cancelled by user.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
        }


    }

    private JToolBar createToolBar() {
        final JToolBar toolBar = new JToolBar();
        toolBar.add(refreshAction);
        toolBar.addSeparator();
        toolBar.add(newAction);
        toolBar.add(saveAction);
        toolBar.addSeparator();
        toolBar.add(deleteAction);
        toolBar.addSeparator();
        toolBar.addSeparator();
        toolBar.addSeparator();
        toolBar.add(validateAction);
        toolBar.add(uploadAction);
        toolBar.add(reportsAction);
        return toolBar;

    }

    private void delete() {
        if (selectedFile != null) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Delete " + "'" + selectedFile.getfName() + "'" + "?", "Delete", JOptionPane.YES_NO_OPTION)) {
                try {
                    Path path = Paths.get("target/" + selectedFile.getfName());
                    Files.deleteIfExists(path);
                    selectedFile.delete();
                } catch (SQLException | IOException e) {
                    JOptionPane.showMessageDialog(this, "Failed to delete " + "'" + selectedFile.getfName() + "'" + "?", "Delete", JOptionPane.ERROR_MESSAGE);
                    //e.printStackTrace();
                } finally {
                    setSelectedFile(null);
                    refreshData();
                }
            }
        }
    }


    private ImageIcon load(final String name) {
        return new ImageIcon(getClass().getResource("/icons/" + name + ".png"));
    }


    private void initActions() {
        refreshAction = new AbstractAction("Refresh", load("Refresh")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                //            try {
                refreshData();
                //          } catch (SQLException e1) {
                //             e1.printStackTrace();
                //       }
            }
        };
        newAction = new AbstractAction("New", load("New")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    createNew();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        };
        saveAction = new AbstractAction("Save", load("Save")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        };
        deleteAction = new AbstractAction("Delete", load("Delete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        };
        validateAction = new AbstractAction("Validate", load("Validate")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                validateFile();
            }
        };
        uploadAction = new AbstractAction("Upload", load("Upload")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                uploadFile();
            }
        };

        reportsAction = new AbstractAction("Run Reports", load("Reports")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                runReports();
            }
        };
    }


    private void initComponents() {
        add(createToolBar(), BorderLayout.PAGE_END);
        add(createListPane(), BorderLayout.WEST);
        add(createEditor(), BorderLayout.CENTER);
    }

    private void initMenu() {
        final JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        final JMenu editMenu = menuBar.add(new JMenu("Edit"));
        editMenu.add(refreshAction);
        editMenu.addSeparator();
        editMenu.add(newAction);
        editMenu.add(saveAction);
        editMenu.addSeparator();
        editMenu.add(deleteAction);
        editMenu.addSeparator();
        editMenu.addSeparator();
        editMenu.addSeparator();
        editMenu.add(validateAction);
        editMenu.add(uploadAction);
        editMenu.add(reportsAction);

    }


    private void runReports() {
    }

    private void uploadFile() {
    }

    private void validateFile() {
    }

    private void refreshData() {
        //THis is the recommended way to deal with long tasks from within
        //a swing application

        //First clear the panel or you will get duplicated entries
        tpFileDefaultListModel.removeAllElements();
        SwingWorker<Void, TPFile> worker = new SwingWorker<Void, TPFile>() {
            @Override
            //this is executed on an different thread and it can take as long as it requires!!
            protected Void doInBackground() throws Exception {
                //we have moved the loading of filenames from within the 'try' block below to here!0p"
                List<TPFile> xlsfiles = TPFileHelper.getInstance().getFiles();
                for (final TPFile xlsfile : xlsfiles) {
                    //the publish line is here to move the xlsfile to the event special thread
                    // after they have been fetched from the database
                    publish(xlsfile);
                }
                return null;
            }

            @Override //then after moving to this GUI thread they are added to the pane
            protected void process(List<TPFile> chunks) {
                for (TPFile xlsfile : chunks) {
                    tpFileDefaultListModel.addElement(xlsfile);
                }
            }
        };
        worker.execute();

//while this method works .. it can be a drawback as it is using the Event Special thread
        //that the gui is using .. and if, for example the list is long and takes some time to populate
        //then the app will freeze while it waits .. so instead of this we are going to introduce a 'Swing Worker'
        //which is a way to stop the app from freezing. It will be used to load our list in the background without
        //freezing the app

        //so this can be replaced
   /*     try {
            tpFileDefaultListModel.removeAllElements();
            java.util.List<TPFile> xlsfiles = TPFileHelper.getInstance().getFiles();
            for (TPFile xlsfile : xlsfiles) {
                tpFileDefaultListModel.addElement((xlsfile));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to refresh the file list", "Refresh", JOptionPane.WARNING_MESSAGE);
        }*/
    }

    private void save() {

        if (selectedFile != null) {
            selectedFile.setDtProcessed(dtprocessedTextField.getText());
            selectedFile.setfStatus(fstatusTextField.getText());
            selectedFile.setEnquiries(Integer.parseInt(noenqTextField.getText()));
            selectedFile.setCalls(Integer.parseInt(nocallTextField.getText()));
            selectedFile.setfTimesRun(Integer.parseInt(notimesrunTextField.getText()));
            try {
                selectedFile.save();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to save the selected file details", "Save", JOptionPane.WARNING_MESSAGE);
            } finally {
                refreshData();
            }

        }


    }


}

