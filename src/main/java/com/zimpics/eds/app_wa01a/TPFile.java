package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

@SuppressWarnings("SpellCheckingInspection")
public class TPFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPFile.class);

    //Private fields
    private String fName;
    private String dtProcessed;
    private String fStatus;
    private int fEnquiries;
    private int fCalls;
    private int fTimesRun;
    private boolean fPersists;


    //public getters and setters
    public String getfName() {
        return fName;
    }

    public void setfName(String newValue) throws SQLException {
        this.fName = newValue;
        fPersists = chkIfFilePersists(fName);


    }

    public String getDtProcessed() {
        return dtProcessed;
    }

    public void setDtProcessed(String newValue) {
        this.dtProcessed = newValue;
    }

    public String getfStatus() {
        return fStatus;
    }

    public void setfStatus(String newValue) {
        this.fStatus = newValue;
    }

    public int getEnquiries() {
        return fEnquiries;
    }

    public void setEnquiries(int newValue) {
        this.fEnquiries = newValue;
    }

    public int getCalls() {
        return fCalls;
    }

    public void setCalls(int newValue) {
        this.fCalls = newValue;
    }

    public int getfTimesRun() {
        return fTimesRun;
    }

    public void setfTimesRun(int newValue) {
        this.fTimesRun = newValue;
    }


    //METHODS
    public void delete() throws SQLException {
        if (fName.length() == 0) {
            // Can throw an exception
        } else {
            TPFile.LOGGER.debug("Deleting file: {}", this);
            final String sql = "DELETE FROM tblFile WHERE file_name = ?";
            try (Connection connection = DbHelper.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, fName);
                pstmt.execute();
                fName = "";
            }
        }
    }

    private boolean chkIfFilePersists(String file) throws SQLException {
        final String sql = "SELECT * FROM tblFile WHERE file_name = " + "'" + file + "'";
        try (Connection connection = DbHelper.getConnection();
             PreparedStatement psmt = connection.prepareStatement(sql);
             ResultSet rs = psmt.executeQuery()) {
            return rs.next();
        }
    }


    public void save() throws SQLException {
        try (Connection connection = DbHelper.getConnection()) {
            if (!fPersists) {
                TPFile.LOGGER.debug("Adding new file: {}", this);
                final String sql = "INSERT INTO tblFile(" +
                        "file_name, " +
                        "date_processed, " +
                        "file_status, " +
                        "enquiries, " +
                        "calls, " +
                        "times_run) VALUES(?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, fName);
                    pstmt.setString(2, dtProcessed);
                    pstmt.setString(3, fStatus);
                    pstmt.setInt(4, fEnquiries);
                    pstmt.setInt(5, fCalls);
                    pstmt.setInt(6, fTimesRun);
                    pstmt.execute();
                    fPersists = true;
                }
            } else {
                TPFile.LOGGER.debug("Updating existing contact: {}", this);
                final String sql = "UPDATE tblFile " +
                        "SET date_processed = ?, " +
                        "file_status = ?, " +
                        "enquiries = ?, " +
                        "calls = ?, " +
                        "times_run = ? " +
                        "WHERE file_name = ? ";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, dtProcessed);
                    pstmt.setString(2, fStatus);
                    pstmt.setString(3, String.valueOf(fEnquiries));
                    pstmt.setString(4, String.valueOf(fCalls));
                    pstmt.setString(5, String.valueOf(fTimesRun));
                    pstmt.setString(6, fName);
                    pstmt.executeUpdate();
                }
            }
        }
    }
       @Override
    public String toString() {
        final StringBuilder formatted = new StringBuilder();
        if (!fPersists) {
            formatted.append("[No PK_FileName] ");
        } else {
            formatted.append("  [").append(fName).append("] ");
        }

        if (dtProcessed.length() == 0) {
            formatted.append("no date");
        } else {
            formatted.append(" | ").append(dtProcessed);
        }

      if (fStatus.length() == 0) {
            formatted.append("no status");
        } else {
            formatted.append(" | ").append(fStatus).append("  ")  ;
        }

        return formatted.toString();
    }

}
