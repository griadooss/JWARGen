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
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


//This is a singleton class
public class DbHelper {
    // private static DataFormatter formatter = new DataFormatter();
   // private static boolean isDuplicate;

 /*   public static void setIsDuplicate(boolean newValue) {
        isDuplicate = newValue;
    }
*/
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
        ds.setMaxTotal(50);

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
        ds.setUsername("'" + properties.getProperty("db.username") + "'");
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

    //CONNECTION UTILITIES
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


    //DATABASE UTILITIES
    private static boolean resetFileStats(String file) {
        String sqlUpd = "UPDATE tblFile ";
        sqlUpd += "SET date_processed = ?, ";
        sqlUpd += "file_status = ?, ";
        sqlUpd += "enquiries = ?, ";
        sqlUpd += "calls = ? ";
        sqlUpd += "WHERE file_name = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement psUpd = conn.prepareStatement(sqlUpd)) {
            psUpd.setString(1, "");
            psUpd.setString(2, "INVALID");
            psUpd.setInt(3, 0);
            psUpd.setInt(4, 0);
            psUpd.setString(5, file);
            psUpd.executeUpdate();
        } catch (SQLException e1) {
            System.out.println(e1.getMessage());
            DbHelper.LOGGER.error("Failed to update file statistics for " + file);
            return false;
        }
        return true;
    }

    private static boolean resetAnomalies(String file) {
        String sqlDel = "DELETE FROM tblAnomalies ";
        sqlDel += "WHERE file_name = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement psDel = conn.prepareStatement(sqlDel)) {
            psDel.setString(1, file);
            psDel.executeUpdate();
        } catch (SQLException e1) {
            System.out.println(e1.getMessage());
            DbHelper.LOGGER.error("Failed to perform delete anomalies for " + file);
            return false;
        }
        return true;
    }

    private static boolean resetCalls(String file) {
        String sqlDel = "DELETE FROM tblCall ";
        sqlDel += "WHERE call_id IN ";
        sqlDel += "(SELECT c.call_id FROM ";
        sqlDel += "tblCall c INNER JOIN ";
        sqlDel += "tblMeta m ON c.call_id = m.call_id ";
        sqlDel += "WHERE file_name = ?)";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement psDel = conn.prepareStatement(sqlDel)) {
            psDel.setString(1, file);
            psDel.executeUpdate();
        } catch (SQLException e1) {
            System.out.println(e1.getMessage());
            DbHelper.LOGGER.error("Failed to perform delete calls on tblCall for " + file);
            return false;
        }
        return true;
    }

    public static boolean resetData(String file, int i) {
        if (i == 1) {
            //VDate is calling
            if (!resetAnomalies(file)) {
                return false;
            }
        } else {
            //ULoad is calling
            if (!resetCalls(file)) {
                return false;
            }
        }
        return resetFileStats(file);
    }

    public static String checkFileStatus(String file) throws SQLException {
        String sql = "SELECT file_status ";
        sql += "FROM tblFile ";
        sql += "WHERE file_name = " + "'" + file + "'";
        try (Connection conn = DbHelper.getConnection(); Statement stmnt = conn.createStatement()) {
            try (ResultSet rs = stmnt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "No Status";
    }


    //DATE UTILITIES
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
        SimpleDateFormat inFormat = new SimpleDateFormat(getDatePattern(fromXls));
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return outFormat.format(inFormat.parse(fromXls));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean is31DayMonth(int m) {
        Set<Integer> grp = new HashSet<>();
        grp.add(1);
        grp.add(3);
        grp.add(5);
        grp.add(7);
        grp.add(8);
        grp.add(10);
        grp.add(12);
        return grp.contains(m);
    }

    private static boolean is31DayMonth(String m) {
        Set<String> grp = new HashSet<>();
        grp.add("Jan");
        grp.add("Mar");
        grp.add("May");
        grp.add("Jul");
        grp.add("Aug");
        grp.add("Oct");
        grp.add("Dec");
        return grp.contains(m);
    }

    private static boolean is30DayMonth(int m) {
        Set<Integer> grp = new HashSet<>();
        grp.add(9);
        grp.add(4);
        grp.add(6);
        grp.add(11);
        return grp.contains(m);
    }

    private static boolean is30DayMonth(String m) {
        Set<String> grp = new HashSet<>();
        grp.add("Sep");
        grp.add("Apr");
        grp.add("Jun");
        grp.add("Nov");
        return grp.contains(m);
    }

    private static boolean is29DayMonth(int m) {
        Set<Integer> grp = new HashSet<>();
        grp.add(2);
        return grp.contains(m);
    }

    private static boolean is29DayMonth(String m) {
        Set<String> grp = new HashSet<>();
        grp.add("Feb");
        return grp.contains(m);
    }

    public static String getDatePattern(String dt) {
        //This app will only accept two different date patterns
        //Patterns outside of this will be rejected
        // REGEX PATTERN #1:  [\d\d\/\d\d\/\d\d\d\d]  .. matches '18/12/1948' pattern
        // REGEX PATTERN #2:  [\d\d\-\w\w\w\-\d\d]    .. matches '18-Dec-48' pattern
        // HOWEVER, the REGEX PATTERN #3: [\d\/\d\d\/\d\d\d\d]  .. matches '8/12/1948' pattern is missing
        // the leading zero ... so it is supplied here!

        Pattern p1 = Pattern.compile("\\d\\d/\\d\\d/\\d\\d\\d\\d");
        Pattern p2 = Pattern.compile("\\d/\\d\\d/\\d\\d\\d\\d");
        Pattern p3 = Pattern.compile("\\d\\d-\\w\\w\\w-\\d\\d");

        if (p2.matcher(dt).matches()) {
            dt = "0" + dt;
        }

        //if the dt string presented matche the first patterh
        //validate that the values of the structre
        if (p1.matcher(dt).matches()) {
            int uno = Integer.parseInt(dt.substring(0, 2)); //gets the number of the day
            int due = Integer.parseInt(dt.substring(3, 5)); //gets the number of the month
            int tre = Integer.parseInt(dt.substring(6)); //gets the number of the year

            // if second place (month) not between 1 and 12 => false
            if (due > 0 && due < 13) {
                // if second place (month) in (2)? ..
                // then if first place (day) not between 1 and 29 => false
                if (is29DayMonth(due)) {
                    // if first place (day) > 29 => false
                    if (uno > 0 && uno < 30) {
                        // third place (year) is greater then this year => false
                        int yr = Calendar.getInstance().get(Calendar.YEAR);
                        if (tre <= yr) {
                            //Both date and pattern validated!! Return the pattern
                            return "dd/mm/yyyy";
                        }
                    }
                }
                if (is30DayMonth(due)) {
                    // if first place (day) > 30 => false
                    if (uno > 0 && uno < 31) {
                        // third place (year) is greater then this year => false
                        int yr = Calendar.getInstance().get(Calendar.YEAR);
                        if (tre <= yr) {
                            //Both date and pattern validated!! Return the pattern
                            return "dd/mm/yyyy";
                        }
                    }
                }
                // else, if second place (month) in (1,3,5,7,8,10,12)? ..
                // then if first place (day) not between 1 and 31 => false
                if (is31DayMonth(due)) {
                    // if first place (day) > 31 => false
                    if (uno > 0 && uno < 32) {
                        int yr = Calendar.getInstance().get(Calendar.YEAR);
                        if (tre <= yr) {
                            //Both date and pattern validated!! Return the pattern
                            return "dd/mm/yyyy";
                        }
                    }
                }
            }
        } else if (p3.matcher(dt).matches()) {
            int uno = Integer.parseInt(dt.substring(0, 2)); //gets the number of the day
            String due = dt.substring(3, 6); //gets the three character string of the month
            int tre = Integer.parseInt(dt.substring(7)); //gets last two digits of the number of the year
            // if second place (month) in (Feb)? ..
            // then if first place (day) not between 1 and 29 => false
            if (is29DayMonth(due)) {
                // if first place (day) > 29 => false
                if (uno > 0 && uno < 30) {
                    // third place (year) is greater then this year => false
                    int yr = Calendar.getInstance().get(Calendar.YEAR);
                    //Hardcoded two digit year takes only years after 2000
                    if (tre <= yr - 2000) {
                        //Both date and pattern validated!! Return the pattern
                        return "dd-MMM-yy";
                    }
                }
            }

            //if (isValid3DayMonthString(due)) {
            //if second place month is a 30 day month ..
            // then if first place (day) > 30 => false
            if (is30DayMonth(due)) {
                // if first place (day) > 30 => false
                if (uno > 0 && uno < 31) {
                    // third place (year) is greater then this year => false
                    int yr = Calendar.getInstance().get(Calendar.YEAR);
                    //Hardcoded two digit year takes only years after 2000
                    if (tre <= yr - 2000) {
                        //Both date and pattern validated!! Return the pattern
                        return "dd-MMM-yy";
                    }
                }
            }

            // else, if second place (month) is a 31 day month ..
            // then if first place (day) not between 1 and 31 => false
            if (is31DayMonth(due)) {
                // if first place (day) > 31 => false
                if (uno > 0 && uno < 32) {
                    int yr = Calendar.getInstance().get(Calendar.YEAR);
                    //Hardcoded two digit year takes only years after 2000
                    if (tre <= yr - 2000) {
                        //Both date and pattern validated!! Return the pattern
                        return "dd-MMM-yy";
                    }
                }
            }

        }
        return "Invalid";
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


