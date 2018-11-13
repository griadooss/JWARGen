/*
 * #%L
 * Excel Report Format Application
 * %%
 * Copyright (C) 2016 - 2018 Emu Data Services
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import com.zimpics.eds.app_wa01a.helpers.TPAnomalyHelper;
import com.zimpics.eds.app_wa01a.helpers.TPFileHelper;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("SpellCheckingInspection")
class Application extends JFrame {

    private JTextField fnameTextField;
    private JTextField dtprocessedTextField;
    private JTextField fstatusTextField;
    private JTextField noenqTextField;
    private JTextField nocallTextField;
    private JTextField notimesrunTextField;

    private DefaultListModel<TPFile> tpFileDefaultListModel;
    private DefaultListModel<TPAnomaly> tpAnomDefaultListModel;
    private JList<TPFile> tpFileJList;
    private JList<TPAnomaly> tpAnomJList;

    //  private JMenuBar menuBar;

    private Action validateAction;
    private Action uploadAction;
    private Action reportsAction;
    private Action refreshAction;
    private Action newAction;
    private Action saveAction;
    private Action deleteAction;


    private TPFile selectedFile;
    private TPAnomaly selectedAnomaly;


    public Application() {
        initActions();
        // initMenu();
        initComponents();

        refreshFileData();
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


    //Method to create TPFile list
    private JComponent createTPFileListPane() {
        tpFileDefaultListModel = new DefaultListModel<>();
        tpFileJList = new JList<>(tpFileDefaultListModel);
        //need to add a listener to the list so that we can populate editor fields in the GUI when selecting a file
        tpFileJList.getSelectionModel().addListSelectionListener(event -> { //invoked evertime selction changes
            if (!event.getValueIsAdjusting()) {
                //11:10 vid part11
                //we only want to process the event only when it has been adjusted!
                //the value change may be triggered more than once .. but only once is it adjusted
                //and then we will get the selectedFile value
                setSelectedTPFile(tpFileJList.getSelectedValue());
            }
        });
        return new JScrollPane(tpFileJList);
    }

    //Method to create TPFile list
    private JComponent createTPAnomalyListPane() {
        tpAnomDefaultListModel = new DefaultListModel<>();
        tpAnomJList = new JList<>(tpAnomDefaultListModel);
        //need to add a listener to the list so that we can populate editor fields in the GUI when selecting a file
        tpAnomJList.getSelectionModel().addListSelectionListener(event -> { //invoked evertime selction changes
            if (!event.getValueIsAdjusting()) {
                //11:10 vid part11
                //we only want to process the event only when it has been adjusted!
                //the value change may be triggered more than once .. but only once is it adjusted
                //and then we will get the selectedFile value
                try {
                    setSelectedTPAnomaly(tpAnomJList.getSelectedValue());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return new JScrollPane(tpAnomJList);
    }


    private void setSelectedTPFile(final TPFile selected) {
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

            refreshAnomalyData(selectedFile.getfName());
        }
    }

    private void setSelectedTPAnomaly(final TPAnomaly selected) throws SQLException {
        this.selectedAnomaly = selected;
        if (selected != null) {
            if (selectedAnomaly.getAnErrCode().equals("FATAL")) {
                JOptionPane.showMessageDialog(this,
                        "Cannot accept anomaly with FATAL status. Address the issue in the source data and " +
                                "and re-run validation.", "READ ONLY", JOptionPane.INFORMATION_MESSAGE);
            } else if (selectedAnomaly.getAnAccept().length() == 0) {
                selectedAnomaly.setAnAccept("#");
            } else {
                selectedAnomaly.setAnAccept("");
            }
            selectedAnomaly.save();


            //String sql = "SELECT * from tblAnomalies WHERE file_name = " + "'" + selectedAnomaly.getAnFName() + "' AND accept = \'#\'";
            String sql = "SELECT count(*) from tblAnomalies WHERE file_name = ? AND accept = ?";
            try (Connection connection = DbHelper.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, selectedFile.getfName());
                pstmt.setString(2,"#");
                ResultSet rs = pstmt.executeQuery();
                if (rs.getInt(1)==0) {
                    selectedFile.setfStatus("VALID");
                    fstatusTextField.setText("VALID");
                    JOptionPane.showMessageDialog(this,
                            "File " + selectedFile.getfName() + " now has a VALID status and can be uploaded to the database!",
                            "Validation Complete", JOptionPane.INFORMATION_MESSAGE);
                    refreshFileData();
                } else if (rs.getInt(1) == 1) {
                    selectedFile.setfStatus("INVALID");
                    fstatusTextField.setText("INVALID");
                }
            }
        }
    }

    // CODE BELOW IN WAITING FOR AN EDITOR
     /*   if (selected == null) {
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
*/


    private void createNew() throws SQLException {
        //Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showSaveDialog(Application.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (FilenameUtils.getExtension(file.getName()).equals("xlsx")) {
                CopyOption[] options = new CopyOption[]{
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES};
                final Properties properties = new Properties();
                properties.put("db.path", "data");
                Path FROM = Paths.get(file.getAbsolutePath());
                Path TO = Paths.get(properties.getProperty("db.path") + "/" + file.getName());

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
                setSelectedTPFile(f);

                JOptionPane.showMessageDialog(this, "Queuing file " + "'" + file.getName() + ".", "New File Selected", JOptionPane.INFORMATION_MESSAGE);
                f.save();
                refreshFileData();
            } else {
                JOptionPane.showMessageDialog(this, "This application expects the file to be XLXS format! The file "
                        + "'" + file.getName() + "' is not a valid choice!", "File Not Cached.", JOptionPane.INFORMATION_MESSAGE);

            }
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
                    final Properties properties = new Properties();
                    properties.put("db.dataPath", "data");
                    properties.getProperty("db.dataPath");
                    Path path = Paths.get( properties.getProperty("db.dataPath") + "/"  + selectedFile.getfName());
                    Files.deleteIfExists(path);
                    selectedFile.delete();
                } catch (SQLException | IOException e) {
                    JOptionPane.showMessageDialog(this, "Failed to delete " + "'" + selectedFile.getfName() + "'" + "?", "Delete", JOptionPane.ERROR_MESSAGE);
                    //e.printStackTrace();
                } finally {
                    setSelectedTPFile(null);
                    refreshFileData();
                    refreshAnomalyData(null);
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
                //               try {
                refreshFileData();
                if (selectedFile != null) {
                    refreshAnomalyData(selectedFile.getfName());
                }
                //           } catch (SQLException e1) {
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
                try {
                    validateFile();
                    refreshFileData();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
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
        add(createTPAnomalyListPane(), BorderLayout.EAST);
        add(createEditor(), BorderLayout.CENTER);
        add(createTPFileListPane(), BorderLayout.WEST);
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

    private void validateFile() throws SQLException {
        VDate.validate(selectedFile);
    }

    private void refreshFileData() {
        //THis is the recommended way to deal with long tasks from within
        //a swing application

        //First clear the panel or you will get duplicated entries
        tpFileDefaultListModel.removeAllElements();
        tpAnomDefaultListModel.removeAllElements();
        SwingWorker<Void, TPFile> fileWorker = new SwingWorker<Void, TPFile>() {
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
        fileWorker.execute();
    }

    private void refreshAnomalyData(String s) {
        //THis is the recommended way to deal with long tasks from within
        //a swing application

        //First clear the panel or you will get duplicated entries
        tpAnomDefaultListModel.removeAllElements();
        SwingWorker<Void, TPAnomaly> anomalyWorker = new SwingWorker<Void, TPAnomaly>() {
            @Override
            //this is executed on an different thread and it can take as long as it requires!!
            protected Void doInBackground() throws Exception {
                //we have moved the loading of anomalies from within the 'try' block below to here!0p"
                List<TPAnomaly> xlsanomalies = TPAnomalyHelper.getInstance().getAnomalies(s);
                for (final TPAnomaly xlsanomaly : xlsanomalies) {
                    //the publish line is here to move the xlsanomaly to the event special thread
                    // after they have been fetched from the database
                    publish(xlsanomaly);
                }
                return null;
            }

            @Override //then after moving to this GUI thread they are added to the pane
            protected void process(List<TPAnomaly> chunks) {
                for (TPAnomaly xlsanomaly : chunks) {
                    tpAnomDefaultListModel.addElement(xlsanomaly);
                }

            }
        };
        anomalyWorker.execute();

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
                refreshFileData();
            }

        }


    }

    public TPFile getSelectedFile() {
        return selectedFile;
    }

}



