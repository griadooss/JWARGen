package com.zimpics.eds.app_wa01a.GUI;

import com.github.lgooddatepicker.components.*;
import com.zimpics.eds.app_wa01a.Reports;
import com.zimpics.eds.app_wa01a.WAClient;
import com.zimpics.eds.app_wa01a.helpers.WAClientHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;


public class ReportForm extends JFrame {
    private DefaultListModel<WAClient> waClientDefaultListModel;
    private JList<WAClient> waClientJList;
    private WAClient selectedClient;

    private Action reportAllAction;
    private Action reportSelectedAction;
    private Action refreshClientDataAction;
    private Action newAction;
    private Action saveAction;
    private Action deleteAction;

    private DatePicker datePicker1;
    private DatePicker datePicker2;
    private JTextField clientName;
    private JTextField clientIdent;
    private JTextField clientRptFreq;

    private String lastQuarter;
    private String lastQuarterYear;
    private AbstractButton button2;
    private AbstractButton button1;


    //CONSTRUCTOR
    public ReportForm() {
        initActions();
        initMenu();
        initializeComponents();
    }

    //SETTERS and GETTERS
    private void setDatePicker1(String newValue) {
        this.datePicker1.setText(newValue);
    }

    public void setDatePicker2(String newValue) {
        this.datePicker2.setText(newValue);
    }

   /* public String getLastQuarter() {
        return lastQuarter;
    }
*/
    public void setLastQuarter(String newValue) {
        this.lastQuarter = newValue;
    }

   /* public String getLastQuarterYear() {
        return lastQuarterYear;
    }
*/
    public void setLastQuarterYear(String newValue) {
        this.lastQuarterYear = newValue;
    }


    //METHODS
    private void initActions() {
        reportAllAction = new AbstractAction("Report All", load("Reports")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                runReport(2);
            }
        };

        reportSelectedAction = new AbstractAction("Report Selected", load("New")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                runReport(1);
            }
        };

        refreshClientDataAction = new AbstractAction("Refresh Client List", load("Refresh")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshClientData();
            }
        };

        newAction = new AbstractAction("New", load("New")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNew();
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
    /*    setPreviousQuarterAction = new AbstractAction("Prev Quarter", load("Save")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPreviousQuarter();
            }
        };
        resetQuarterVarsAction = new AbstractAction("Reset Quarter", load("Custom")) {
            @Override
            public void actionPerformed(ActionEvent e) { resetQuarterVars(); }
        };
*/



       /* resetQuarterVarsAction = new AbstractAction("Clear Quarter", load("Save")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetQuarterVars();
            }
        };*/
    }

    private void createNew() {
        final WAClient client = new WAClient();
        clientIdent.setEditable(true);
        client.setcId("New Client ID");
        client.setcName("New Client Name");
        client.setcRptFreq("New Report Freq");
        setSelectedClient(client);
    }

    private void delete() {
        if (selectedClient != null) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Delete?", "Delete", JOptionPane.YES_NO_OPTION)) {
                try {
                    selectedClient.delete();
                } catch (final SQLException e) {
                    JOptionPane.showMessageDialog(this, "Failed to delete the selected contact", "Delete",
                            JOptionPane.WARNING_MESSAGE);
                } finally {
                    setSelectedClient(null);
                    refreshClientData();
                }
            }
        }
    }

    private ImageIcon load(final String name) {
        return new ImageIcon(getClass().getResource("/icons/" + name + ".png"));
    }


    /**
     * initializeComponents, This creates the user interface
     */
    private void initializeComponents() {
        add(createWAClientListPane(), BorderLayout.WEST);
        add(createDateEditor(), BorderLayout.EAST);
        //   add(createToolBar1(), BorderLayout.PAGE_START);
        add(createToolBar2(), BorderLayout.PAGE_END);
        refreshClientData();
    }

    private void initMenu() {
        final JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        final JMenu editMenu = menuBar.add(new JMenu("Reports Menu"));
        editMenu.add(reportAllAction);
        editMenu.addSeparator();
        editMenu.add(reportSelectedAction);
        editMenu.addSeparator();

    }

    private JComponent createDateEditor() {
        final JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints;
        //JButton button1, button2 = new JButton();
        //#######################################################33
        //CLIENT EDITOR - HEADER
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        JTextField subheaderText = new JTextField();
        subheaderText.setText("CLIENT EDITOR");
        Font font = new Font("Arial", Font.BOLD, 14);
        subheaderText.setFont(font);
        subheaderText.setBackground(Color.GRAY);
        subheaderText.setForeground(Color.WHITE);
        subheaderText.setToolTipText("Enter dates");
        subheaderText.setHorizontalAlignment(JTextField.CENTER);
        subheaderText.setEditable(false);
        panel.add(subheaderText, constraints);


        //#######################################################33
        //CLIENT DETAILS - EDITABLE
        // Client Ident
        constraints = new GridBagConstraints();
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Client Ident"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        clientIdent = new JTextField(10);
        clientIdent.setEditable(false);
        panel.add(clientIdent, constraints);

        // Client Name
        constraints = new GridBagConstraints();
        constraints.gridy = 3;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Client name"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        clientName = new JTextField(20);
        panel.add(clientName, constraints);

        // Client Reporting Frequency
        constraints = new GridBagConstraints();
        constraints.gridy = 4;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Report Freq"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        clientRptFreq = new JTextField(5);
        panel.add(clientRptFreq, constraints);

        //#######################################################33
        // USER INSTRUCTIONS and Area Separatorr
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        JTextArea areaSeparator = new JTextArea(3, 10);
        areaSeparator.setEditable(false);
        areaSeparator.setLineWrap(true);
        areaSeparator.setWrapStyleWord(true);
        areaSeparator.setText("Use the editor above to maintan clients. Use the date picker below to set the date range for reports.");
        panel.add(areaSeparator, constraints);


        //#######################################################33
        //REPORT DATE PICKER - HEADER
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        subheaderText = new JTextField();
        subheaderText.setText("REPORT DATE RANGE");
        font = new Font("Arial", Font.BOLD, 14);
        subheaderText.setFont(font);
        subheaderText.setBackground(Color.GRAY);
        subheaderText.setForeground(Color.WHITE);
        subheaderText.setHorizontalAlignment(JTextField.CENTER);
        subheaderText.setEditable(false);
        panel.add(subheaderText, constraints);


       // Date From
        constraints = new GridBagConstraints();
        constraints.gridy = 7;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Date From"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 7;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        datePicker1 = new DatePicker(dateSettings);
        // datePicker1.setDateToToday();
        panel.add(datePicker1, constraints);

        // Date To
        constraints = new GridBagConstraints();
        constraints.gridy = 8;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Date To"), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 8;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        DatePickerSettings dateSett2 = new DatePickerSettings();
        dateSett2.setFormatForDatesCommonEra("yyyy-MM-dd");
        datePicker2 = new DatePicker(dateSett2);
        datePicker2.setDateToToday();
        panel.add(datePicker2, constraints);

        //BUTTONS
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 9;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        button2 = new JButton("Set Custom Dates");
        button2.setVerticalTextPosition(AbstractButton.CENTER);
        button2.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        button2.setMnemonic(KeyEvent.VK_D);
        button2.addActionListener(e -> resetQuarterVars());
        button2.setToolTipText("Choose a custom date range.");
        panel.add(button2, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 10;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;

        button1 = new JButton("Set Last Quarter");
        button1.setVerticalTextPosition(AbstractButton.CENTER);
        button1.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        button1.setMnemonic(KeyEvent.VK_Q);
        button1.addActionListener(e -> setPreviousQuarter());
        button1.setToolTipText("Get the dates for last quarter.");
        panel.add(button1, constraints);

        return panel;
    }

    private void resetQuarterVars() {
        lastQuarter = "";
        lastQuarterYear = "";
        button2.setEnabled(false);
        button1.setEnabled(true);
        datePicker1.setText("");
        datePicker2.setText("");
        datePicker1.setEnabled(true);
        datePicker2.setEnabled(true);
    }

    //Method to create WAClient list
    private JComponent createWAClientListPane() {
        waClientDefaultListModel = new DefaultListModel<>();
        waClientJList = new JList<>(waClientDefaultListModel);
        //need to add a listener to the list so that we can populate editor fields in the GUI when selecting a file
        waClientJList.getSelectionModel().addListSelectionListener(event -> { //invoked evertime selction changes
            if (!event.getValueIsAdjusting()) {
                //11:10 vid part11
                //we only want to process the event only when it has been adjusted!
                //the value change may be triggered more than once .. but only once is it adjusted
                //and then we will get the selectedClient value
                setSelectedClient(waClientJList.getSelectedValue());
            }
        });
        return new JScrollPane(waClientJList);

    }

    private void setPreviousQuarter() {
        button2.setEnabled(true);
        button1.setEnabled(false);
        datePicker1.setEnabled(false);
        datePicker2.setEnabled(false);
        // current date
        LocalDate now = LocalDate.now();
        // get quarter
        int presentQuarter = now.get(IsoFields.QUARTER_OF_YEAR);
        // year for last quarter
        int presentYear = now.minus(1, IsoFields.QUARTER_YEARS).getYear();
        // last quarter
        int lastQuarter;
        int lastQuarterYear;
        //adjust for when last quarter is last year
        if (presentQuarter == 1) {
            lastQuarter = 4;
            lastQuarterYear = presentYear - 1;
        } else {
            lastQuarter = presentQuarter - 1;
            lastQuarterYear = presentYear;
        }
        setLastQuarterYear(String.valueOf(lastQuarterYear));

        String start = "";
        String end = "";
        switch (lastQuarter) {
            case 1:
                start = lastQuarterYear + "-01-01";
                end = lastQuarterYear + "-03-31";
                setLastQuarter("Q1");
                break;
            case 2:
                start = lastQuarterYear + "-04-01";
                end = lastQuarterYear + "-06-30";
                setLastQuarter("Q2");
                setLastQuarterYear(String.valueOf(lastQuarterYear));
                break;
            case 3:
                start = lastQuarterYear + "-07-01";
                end = lastQuarterYear + "-09-30";
                setLastQuarter("Q3");
                setLastQuarterYear(String.valueOf(lastQuarterYear));
                break;
            case 4:
                start = lastQuarterYear + "-10-01";
                end = lastQuarterYear + "-12-31";
                setLastQuarter("Q4");
                setLastQuarterYear(String.valueOf(lastQuarterYear));
                break;
        }
        setDatePicker1(start);
        setDatePicker2(end);

    }

    private void setSelectedClient(final WAClient selected) {
        this.selectedClient = selected;
        if (selected == null) {
            clientIdent.setText("");
            clientName.setText("");
            clientRptFreq.setText("");
        } else {
            clientIdent.setText(selectedClient.getcId());
            clientName.setText(selectedClient.getcName());
            clientRptFreq.setText(selectedClient.getcRptFreq());

            // refreshClientData();
        }

    }

    private void refreshClientData() {
        //THis is the recommended way to deal with long tasks from within
        //a swing application

        //First clear the panel or you will get duplicated entries
        waClientDefaultListModel.removeAllElements();
        SwingWorker<Void, WAClient> fileWorker = new SwingWorker<Void, WAClient>() {
            @Override
            //this is executed on an different thread and it can take as long as it requires!!
            protected Void doInBackground() throws Exception {
                //we have moved the loading of filenames from within the 'try' block below to here!0p"
                List<WAClient> clients = WAClientHelper.getInstance().getClients();
                for (final WAClient client : clients) {
                    //the publish line is here to move the xlsfile to the event special thread
                    // after they have been fetched from the database
                    publish(client);
                }
                return null;
            }

            @Override //then after moving to this GUI thread they are added to the pane
            protected void process(List<WAClient> chunks) {
                for (WAClient client : chunks) {
                    waClientDefaultListModel.addElement(client);
                }
            }
        };
        fileWorker.execute();
        clientIdent.setEditable(false);
    }

    public void runReport(int ver) {

        Reports reports = new Reports();
        reports.setLastQuarter(lastQuarter);
        reports.setLastQuarterYear(lastQuarterYear);
        reports.setStartDate(datePicker1.getText());
        reports.setEndDate(datePicker2.getText());

        if ((datePicker1.getText().length() == 0 || datePicker2.getText().length() == 0)) {
            JOptionPane.showMessageDialog(this,
                    "Please select a date range to report on.",
                    "Date selection required!", JOptionPane.INFORMATION_MESSAGE);
        } else if (datePicker2.getDate().isBefore(datePicker1.getDate())) {
            JOptionPane.showMessageDialog(this,
                    " Date #2 must be after Date#1",
                    "Problem with data range.", JOptionPane.INFORMATION_MESSAGE);
            //Run for one client
        } else if (ver == 1) {
            if (selectedClient == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a client to report on.",
                        "Selection required!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                reports.rptDetClientDate(selectedClient.getcId());
            }
            //Run for all clients
        } else {
            List<WAClient> clients = null;
            try {
                clients = WAClientHelper.getInstance().getClients();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (clients != null) {
                for (WAClient client : clients) {
                    System.out.println(client.getcName());
                    reports.rptDetClientDate(
                            client.getcId());
                    // Thread.sleep(2 * 1000);
                }
            }
        }

        System.out.println("Reporting has finished");
    }

    /* private JToolBar createToolBar1() {
         final JToolBar toolBar = new JToolBar();
         toolBar.add(reportAllAction);
         toolBar.addSeparator();
         toolBar.add(reportSelectedAction);
         return toolBar;
     }
 */
    private JToolBar createToolBar2() {
        final JToolBar toolBar = new JToolBar();
        toolBar.add(refreshClientDataAction);
        toolBar.addSeparator();
        toolBar.add(newAction);
        toolBar.add(saveAction);
        toolBar.addSeparator();
        toolBar.add(deleteAction);
        return toolBar;
    }


    private void save() {
        if (selectedClient != null) {
            selectedClient.setcId(clientIdent.getText());
            selectedClient.setcName(clientName.getText());
            selectedClient.setcRptFreq(clientRptFreq.getText());
            try {
                selectedClient.save();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to save the selected client details", "Save", JOptionPane.WARNING_MESSAGE);
            } finally {
                refreshClientData();
            }
        }
    }
}

