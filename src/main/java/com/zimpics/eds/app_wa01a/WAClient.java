package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class WAClient {

    //FIELDS
    private String cId;
    private String cName;
    private String cRptFreq;
    private boolean cPersists;

    private static final Logger LOGGER = LoggerFactory.getLogger(WAClient.class);

    private static final Properties props = new Properties();

    //CONSTRUCTOR
    public WAClient() {
    }

    public static void init() {
        loadFromPropertiesFile();
        // setClients();
        loadToDB();
    }

    //GETTERS
    public String getcId() {
        return cId;
    }

    public String getcName() {
        return cName;
    }

    public String getcRptFreq() {
        return cRptFreq;
    }

    private static void loadFromPropertiesFile() {
        //Default values for tblClient .. say client.properties is missing!!
        props.setProperty("ACA", "Australian Chiropractors Association");
        props.setProperty("ADAINC", "Australian Dental Association Inc");
        props.setProperty("AIA", "Australian Institute of Architects");
        props.setProperty("AOA", "Osteopathic Australian");
        props.setProperty("APA", "Australian Physiotherapy Association");
        props.setProperty("APODA", "Australian Podiatry Association");
        props.setProperty("APODAV", "Australian Podiatry Association (VIC)");
        props.setProperty("AVA", "The Australian Veterinary Assoc Ltd");
        props.setProperty("CAA", "Australian Chiropractors Association");
        props.setProperty("EDS", "Emu Data Services");
        props.setProperty("OA", "Osteopathic Australian");
        props.setProperty("PIAA", "Printing Industries Association of Australia");
        props.setProperty("VNCA", "Veterinary Nurses Council of Australia");
        try {
            props.load(WAClient.class.getResourceAsStream("/client.properties"));
        } catch (final IOException e) {
            WAClient.LOGGER.error("Failed to load clients from properties file");
        }
    }

    public void setcId(String newValue) {
        cId = newValue;
        try {
            cPersists = chkClientPersists(cId);
        } catch (SQLException e) {
            WAClient.LOGGER.error("Cannot access tblClient: WAClient | setcId");
        }
    }

    public void setcName(String newValue) {
        cName = newValue;
    }

    public void setcRptFreq(String newValue) {
        cRptFreq = newValue;
    }

    //METHODS
    public static String isValidClient(String firstCol) {
        String sql = "SELECT client_ident FROM tblClient ";
        String cId;
        try (Connection conn = DbHelper.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                cId = rs.getString(1);
                if (firstCol.startsWith(cId)) {
                    return cId;
                }
            }
        } catch (SQLException e) {
            WAClient.LOGGER.error("Could not access tblClient. WAClient | isValidClient");
            e.printStackTrace();
        }
        return "";
    }

    private boolean chkClientPersists(String id) throws SQLException {
        final String sql = "SELECT * FROM tblClient WHERE client_ident = " + "'" + id + "'";
        try (Connection connection = DbHelper.getConnection();
             PreparedStatement psmt = connection.prepareStatement(sql);
             ResultSet rs = psmt.executeQuery()) {
            return rs.next();
        }
    }

    private static void loadToDB() {
        //Populate the table tblClient with the known valid client details
        //load the client data from the properties file

        //read the property keys and values into a Set
        Set<String> keys = props.stringPropertyNames();
        //write each set to the database tblClient
        try (Connection conn = DbHelper.getConnection(); Statement stmt = conn.createStatement()) {
            for (String key : keys) {
                String sql = "SELECT * FROM tblCLient ";
                sql += "WHERE ";
                sql += "client_ident = " + "'" + key + "'";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (!rs.next()) {
                        final String sqlIns = "INSERT INTO  tblClient(client_ident, client_name) VALUES (?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlIns)) {
                            pstmt.setString(1, key);
                            pstmt.setString(2, props.getProperty(key));
                            pstmt.execute();
                        } catch (SQLException e) {
                            //e.printStackTrace();
                            WAClient.LOGGER.error("Problem inserting into tblClient: WAClient", e);
                        }
                    } else {
                        final String sqlUpd = "UPDATE tblClient SET client_name = ? WHERE client_ident =?";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpd)) {
                            pstmt.setString(1, props.getProperty(key));
                            pstmt.setString(2, key);
                            pstmt.execute();
                        } catch (SQLException e) {
                            //e.printStackTrace();
                            WAClient.LOGGER.error("Problem updating tblClient: WAClient", e);
                        }
                    }
                } catch (SQLException e) {
                    //e.printStackTrace();
                    WAClient.LOGGER.error("Problem getting resultset: WAClient");
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            WAClient.LOGGER.error("Problem getting Connection: WAClient");
        }
    }

    public void save() throws SQLException {
        try (Connection connection = DbHelper.getConnection()) {
            if (!cPersists) {
                WAClient.LOGGER.debug("Adding new client: {}", this);
                final String sql = "INSERT INTO tblClient(" +
                        "client_ident, " +
                        "client_name, " +
                        "client_rpt_freq) " +
                        "VALUES( ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, cId);
                    pstmt.setString(2, cName);
                    pstmt.setString(3, cRptFreq);
                    pstmt.execute();
                    cPersists = true;
                }
            } else {
                WAClient.LOGGER.debug("Updating existing client details: {}", this);
                final String sql = "UPDATE tblClient " +
                        "SET client_name = ?, " +
                        "client_rpt_freq = ? " +
                        "WHERE client_ident = ? ";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, cName);
                    pstmt.setString(2, cRptFreq);
                    pstmt.setString(3, cId);
                    pstmt.executeUpdate();
                }
            }
        }
    }

    public void delete() throws SQLException {
        // Can throw an exception
        if (cId.length() == 0) {
            WAClient.LOGGER.error("Cannot delete client " + "'" + this.getcName() + "'");
        } else {
            WAClient.LOGGER.debug("Deleting client: {}", this);
            final String sql = "DELETE FROM tblClient WHERE client_ident = ?";
            try (Connection connection = DbHelper.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, cId);
                pstmt.execute();
                cId = "";
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder formatted = new StringBuilder();
        if (cId.length() == 0) {
            formatted.append("[NoClietID] ");
        } else {
            formatted.append("  ").append(cId).append(" ");
        }

        if (cName.length() == 0) {
            formatted.append("noName");
        } else {
            formatted.append(" | ").append(cName);
        }

        if (cRptFreq.length() == 0) {
            formatted.append("noRptFeq");
        } else {
            formatted.append(" | ").append(cRptFreq).append("  ");
        }

        return formatted.toString();
    }

}




