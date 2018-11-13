package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TPAnomaly {
    private static final Logger LOGGER = LoggerFactory.getLogger(TPFile.class);

    //PRIVATE FIELDS
    private String anFName;
    private int anLineNo;
    private int anColNo;
    private String anAttrib;
    private String anErrCode;
    private String anDescr;
    private String anAccept;


    //CONSTRUCTOR
    public TPAnomaly() {
    }

    //GETTERS AND SETTERS
    public String getAnFName() {
        return anFName;
    }

   public void setAnFName(String newValue) {
        this.anFName = newValue;
    }

    public int getAnLineNo() {
        return anLineNo;
    }

    public void setAnLineNo(int newValue) {
        this.anLineNo = newValue;
    }

    public int getAnColNo() {
        return anColNo;
    }

    public void setAnColNo(int newValue) {
        this.anColNo = newValue;
    }

    public String getAnAttrib() {
        return anAttrib;
    }

    public void setAnAttrib(String newValue) {
        this.anAttrib = newValue;
    }

    public String getAnErrCode() {
        return anErrCode;
    }

    public void setAnErrCode(String newValue) {
        this.anErrCode = newValue;
    }

    public String getAnDescr() {
        return anDescr;
    }

    public void setAnDescr(String newValue) {
        this.anDescr = newValue;
    }

    public String getAnAccept() {
        return anAccept;
    }

    public void setAnAccept(String newValue) {
        this.anAccept = newValue;
    }

    public void save() throws SQLException {
        try (Connection connection = DbHelper.getConnection()) {
            boolean fPersists = chkAnomalyPersists();
            if (!fPersists) {
                TPAnomaly.LOGGER.debug("Adding new anomaly: {}", this);
                final String sql = "INSERT INTO tblAnomalies(" +
                        "file_name, " +
                        "line_no, " +
                        "col_no, " +
                        "attrib, " +
                        "err_code, " +
                        "descr, " +
                        "accept ) VALUES(?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, anFName);
                    pstmt.setInt(2, anLineNo);
                    pstmt.setInt(3, anColNo);
                    pstmt.setString(4, anAttrib);
                    pstmt.setString(5, anErrCode);
                    pstmt.setString(6, anDescr);
                    pstmt.setString(7, anAccept);
                    pstmt.execute();
                }
            } else {

                TPAnomaly.LOGGER.debug("Updating existing anomaly details: {}", this);
                final String sql = "UPDATE tblAnomalies " +
                        "SET attrib = ?, " +
                        "err_code = ?, " +
                        "descr = ? ," +
                        "accept = ? " +
                        "WHERE file_name = ? " +
                        "AND line_no = ? " +
                        "AND col_no = ? ";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, anAttrib);
                    pstmt.setString(2, anErrCode);
                    pstmt.setString(3, anDescr);
                    pstmt.setString(4, anAccept);
                    pstmt.setString(5, anFName);
                    pstmt.setInt(6, anLineNo);
                    pstmt.setInt(7, anColNo);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String toString() {
        String str = " ";
      //  str +=  "  " + anFName;
        str += anAccept;
        str += anLineNo;
      //  str += " | " + anColNo;
      //  str += " | " + anAttrib;
        str += " | " + anErrCode;
        str += " | " + anDescr;
        str += " ";
        return str;
    }
/*
        final StringBuilder formatted = new StringBuilder();
        //  formatted.append("  ").append(anFName).append(" ");

        formatted.append("  ").append(anAccept);
        formatted.append("  ").append(anLineNo);
        // formatted.append(" | ").append(anColNo);
        // formatted.append(" | ").append(anAttrib);
        formatted.append(" | ").append(anErrCode);
        formatted.append(" | ").append(anDescr).append("  ");
        return formatted.toString();
    }
*/


    private boolean chkAnomalyPersists() throws SQLException {
        final String sql = "SELECT * FROM tblAnomalies " +
                "WHERE file_name = ? " +
                "AND line_no = ? " +
                "AND col_no = ? ";
        try (Connection connection = DbHelper.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, anFName);
            pstmt.setInt(2, anLineNo);
            pstmt.setInt(3, anColNo);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }
}

