package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;


class WAClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(WAClient.class);

    private static final String[][] clients = new String[11][2];

    private WAClient() {
        setClients();
        loadClients();
    }

    public static String[][] getClients() {
        return clients;
    }

    private static void setClients() {
        clients[0][0] = "ADAINC";
        clients[0][1] = "Australian Dental Association Inc";
        clients[1][0] = "AVA";
        clients[1][1] = "The Australian Veterinary Assoc Ltd";
        clients[2][0] = "AIA";
        clients[2][1] = "Australian Institute of Architects";
        clients[3][0] = "APA";
        clients[3][1] = "Australian Physiotherapy Association";
        clients[4][0] = "AOA";
        clients[4][1] = "Australian Osteopathic Association";
        clients[5][0] = "APODA";
        clients[5][1] = "Australian Podiatry Association (NSW)";
        clients[6][0] = "APODAV";
        clients[6][1] = "Australian Podiatry Association (VIC)";
        clients[7][0] = "CAA";
        clients[7][1] = "Chiropractors Association of Australia";
        clients[8][0] = "PIAA";
        clients[8][1] = "Pet Industry Association of Australia";
        clients[9][0] = "VNCA";
        clients[9][1] = "Veterinary Nurses Council of Australia";
        clients[10][0] = "OA";
        clients[10][1] = "Optometry Australia";
    }

    public static String isValidClient(String firstCol, int rowNum) {
        setClients();
        loadClients();
        for (String[] client : clients) {
            if (firstCol.startsWith(client[0])) {
                return client[0];
            }
        }
        return "";
    }

    private static void loadClients() {
        //Populate the table tblClient with the known valid client details
        try (Connection conn = DbHelper.getConnection(); Statement stmt = conn.createStatement()) {
            for (String[] arg : WAClient.clients) {
                String sql = "SELECT * FROM tblCLient ";
                sql += "WHERE ";
                sql += "client_ident = " + "'" + String.valueOf(arg[0]) + "'";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (!rs.next()) {
                        final String sqlIns = "INSERT INTO  tblClient(client_ident, client_name) VALUES (?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlIns)) {
                            pstmt.setString(1, String.valueOf(arg[0]));
                            pstmt.setString(2, String.valueOf(arg[1]));
                            pstmt.execute();
                        } catch (SQLException e) {
                            //e.printStackTrace();

                            WAClient.LOGGER.error("Problem inserting into tblClient: WAClient", e);
                        }
                    } else {
                        final String sqlUpd = "UPDATE tblClient SET client_name = ? WHERE client_ident =?";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpd)) {
                            pstmt.setString(1, String.valueOf(arg[1]));
                            pstmt.setString(2, String.valueOf(arg[0]));
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

}




