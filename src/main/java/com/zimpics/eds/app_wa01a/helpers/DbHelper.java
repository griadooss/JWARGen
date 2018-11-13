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
package com.zimpics.eds.app_wa01a.helpers;


import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.date.DateFormatUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

//This is a singleton class
public class DbHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbHelper.class);
    //the datasource is the only field of this class!!
    private BasicDataSource ds;

    //instantiation of the singleton through a field of the same type ...usually called INSTANCE
    //#########################
    public static DbHelper getInstance() {
        return DbHelper.INSTANCE;
    }

    private static final DbHelper INSTANCE = new DbHelper();//when this class is loaded this field is initialized, and this will be the only instance of this class

    //private constructor .. only this class can instantiate it ..nobody else can create an instance of this .. it is a singleton
    private DbHelper() {
    }
    //#########################

    public void close() {
        if (ds != null) {
            DbHelper.LOGGER.debug("Closing the data source: DbHelper");
            try {
                ds.close();
            } catch (SQLException e) {
                DbHelper.LOGGER.error("Failed to close the datasource", e);
            }
        }
    }

    private BasicDataSource getDataSource() {
        return ds;
    }

    public void init() {
        DbHelper.LOGGER.debug("Loading database and connection properties");
        final Properties properties = new Properties();
        properties.put("db.dataPath", "data");
        properties.put("db.reportsPath", "reports");
        properties.put("db.name", "wa.db");
        properties.put("db.username", "");
        properties.put("db.password", "");
        try {
            properties.load(getClass().getResourceAsStream("/app.properties"));
        } catch (final IOException e) {
            DbHelper.LOGGER.error("Failed to load properties");
        }

        DbHelper.LOGGER.debug("Creating the data source");
        ds = new BasicDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");

        //first make sure the directory exists to hold the database and data,
        // and while you're at it ... the final report files!
        Path dataPath = Paths.get(properties.getProperty("db.dataPath"));
        Path reportsPath = Paths.get(properties.getProperty("db.reportsPath"));
        File dirData = new File(String.valueOf(dataPath));
        File dirReports = new File(String.valueOf(reportsPath));
        try {
            if (!dirData.exists()) {
                Files.createDirectory(dataPath);
            }
            if (!dirReports.exists()) {
                Files.createDirectory(reportsPath);
            }
        } catch (IOException e) {
            DbHelper.LOGGER.error("Cannot create the directories \'data\' or \'reports\'. Do it manually and run the software once more.");
            //e.printStackTrace();
        }

        //Set datasource properties ... hardcoded SQLite
        ds.setUrl("jdbc:sqlite:" + properties.getProperty("db.dataPath") + "/" + properties.getProperty("db.name"));
        ds.setUsername("'" + properties.get("db.username") + "'");
        ds.setPassword("'" + properties.getProperty("db.password" + "'"));

        //Flyway put here as it needs to be the very first thing that runs after
        //the datasource has been created .. to make sure the database is latest version.
        DbHelper.LOGGER.debug("Executing Flyway (database migration)");
        Flyway flyway = new Flyway();
        try {
            flyway.setDataSource(ds);
            flyway.migrate();
        } catch (FlywayException e) {
            //e.printStackTrace();
            DbHelper.LOGGER.error("Flyway unable to obtain database connection: DbHelper");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerShutdownHook() {
        //This takes away the responsibility of closing threads properly
        //so whenever the JVM shuts down it will always close the datasource.
        Runtime.getRuntime().addShutdownHook(new Thread(this::close)); //, "DB-Helper-Thread"));

        //Give the thread a name for ease of spotting in the log.
        //However, the gotcha is that thread names have to be unique ., so in order not to complicate
        //matters best to let the system handle the naming .. so i have removed it!

    }

    public static Connection getConnection() throws SQLException {
        return DbHelper.getInstance().getDataSource().getConnection();
    }


    //Utitity Database METHODS
    public static void deleteCalls(String fileLocation) {
        String sql = "DELETE FROM tblCall ";
        sql += "WHERE EXISTS ";
        sql += "(SELECT call_id ";
        sql += "FROM tblMeta ";
        sql += "WHERE tblMeta.call_id = tblCall.call_id ";
        sql += "AND tblMeta.xls_file = ?)";

        try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
            psSel.setString(1, fileLocation);
            psSel.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void deleteMeta(String fileLocation) {
        String sql = "DELETE FROM tblMeta ";
        sql += "WHERE xls_file = ?";
        try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
            psSel.setString(1, fileLocation);
            psSel.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void resetData(String file)  {
        //sql for clearing this file's previous anomlies from the database
        //each run through Validation for a particular file starts afresh.

        String sqlDel = "DELETE FROM tblAnomalies ";
        sqlDel += "WHERE file_name = ?";

        //sql for resetting the file statistics .. except for 'times_run'
        String sqlUpd = "UPDATE tblFile ";
        sqlUpd += "SET date_processed = ?, ";
        sqlUpd += "file_status = ?, ";
        sqlUpd += "enquiries = ?, ";
        sqlUpd += "calls = ? ";
        sqlUpd += "WHERE file_name = ?";

        //Perform the transaction with try with resources
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement psDel = conn.prepareStatement(sqlDel);
             PreparedStatement psUpd = conn.prepareStatement(sqlUpd)) {

            //set auto-commit to false
            conn.setAutoCommit(false);

            //1. remove previous anomalies for this file
            psDel.setString(1, file);
            int rowsDeleted = psDel.executeUpdate();
            if (rowsDeleted == 0) {
                conn.rollback();
                DbHelper.LOGGER.error("Failed to clear the anomalies for " + file + ". Rolling back the transaction.");
            }

            //2. reset file statistics
            psUpd.setString(1, "");
            psUpd.setString(2, "INVALID");
            psUpd.setInt(3, 0);
            psUpd.setInt(4, 0);
            psUpd.setString(5, file);
            int rowsAffected = psUpd.executeUpdate();
            if (rowsAffected == 0) {
                conn.rollback();
                DbHelper.LOGGER.error("Failed to reset the counters for " + file + ". Rolling back the transaction.");
            }
            //commit both operations
            conn.commit();

        } catch (SQLException e1) {
            System.out.println(e1.getMessage());
            DbHelper.LOGGER.error("Failed to perform the transaction on tblAnomalies for " + file);
        }

    }

    public static String makeDateReport(String fromDb) {
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outFormat = new SimpleDateFormat("dd-MMM-yy");
        try {
            return outFormat.format(inFormat.parse(fromDb));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String makeDateSQLite(String fromXls) {
        //  fromXls = ("0000000000" + fromXls).substring(fromXls.length());

        //SimpleDateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy");
        //SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat inFormat = new SimpleDateFormat("dd-MMM-yy");
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return outFormat.format(inFormat.parse(fromXls));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String myDateStamp() {
        Calendar now = Calendar.getInstance();
        System.out.println();
        String strDate = (now.get(Calendar.MONTH) + 1) + "-"
                + now.get(Calendar.DATE) + "-" + now.get(Calendar.YEAR) + " "
                + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":"
                + now.get(Calendar.SECOND) + "." + now.get(Calendar.MILLISECOND);
        System.out.println(strDate);
        strDate = DateFormatUtils.format(now, "yyyy-MM-dd HH:mm");

        return strDate;

    }

}

