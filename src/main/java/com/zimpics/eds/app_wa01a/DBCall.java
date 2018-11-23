package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DBCall {
    //FIELDS
    //the Client ID field
    private static final Logger LOGGER = LoggerFactory.getLogger(DBCall.class);
    private static String clIdent;

    //the call header fields
    private static String callLocation;
    private static String callPilotPart;
    private static String callerName;

    //the Call body fields
    private static int callId;
    private static boolean isDuplicate;
    private static String callStartDate;
    private static String callStartNote;
    private static String callEndDate;
    private static String callDuration;
    private static String callResNote;
    private static int callRowNumber;

    //LOOKUP DATA FIELDS
    //WA Member fields
    private static String memNum;
    private static String memFirstName;
    private static String memSurname;
    private static String memPosition;


    //the Lookup table CallCategory
    private static int callCatId;
    private static String callCat;
   private static String callCatDesc; //used in sql statement

    //the Lookup table CallSubCategory
    private static int callSubCatId;
    private static String callSubCat;
    private static String callSubCatCatdesc; //used in sql statement

    //the Lookup table CallStatus;
    private static int callStatusId;
    private static String callStatus;
    private static String callStatusDesc; //used in sql statement

    //the Lookup table CallAction
    private static int callActionId;
    private static String callAction;
    private static String callActionDesc; //used in sql statement

    //the Lookup table CallResolution;
    private static int callResolutionId;
    private static String callResolution;
    private static String callResolutionDesc; //used in sql statement


    //CONSTRUCTORS


    //SETTERS

    public static void setCallId(int newValue) {
        DBCall.callId = newValue;
    }

    public static void setIsDuplicate(boolean newValue) {
        DBCall.isDuplicate = newValue;
    }

    public static void setCallStartDate(String newValue) {
        DBCall.callStartDate = newValue;
    }

    public static void setCallStartNote(String newValue) {
        DBCall.callStartNote = newValue;
    }

    public static void setCallEndDate(String newValue) {
        DBCall.callEndDate = newValue;
    }

    public static void setCallDuration(String newValue) {
        DBCall.callDuration = newValue;
    }

    public static void setCallResNote(String newValue) {
        DBCall.callResNote = newValue;
    }

    public static void setCallRowNumber(int newValue) {
        DBCall.callRowNumber = newValue;
    }

    public static void setCallLocation(String newValue) {
        callLocation = newValue;
    }

    public static void setCallPilotPart(String newValue) {
        callPilotPart = newValue;
    }

    public static void setCallerName(String newValue) {
        callerName = newValue;
    }

    public static void setClIdent(String newValue) {
        clIdent = newValue;
    }

    public static void setMemNum(String newValue) {
        memNum = newValue;
    }

    public static void setMemFirstName(String newValue) {
        memFirstName = newValue;
    }

    public static void setMemSurname(String newValue) {
        memSurname = newValue;
    }

    public static void setMemPosition(String newValue) {
        memPosition = newValue;
    }


    public static void setCallCat(String newValue) {
        callCat = newValue;
    }


    public static void setCallSubCat(String newValue) {
        callSubCat = newValue;
    }

    public static void setCallStatus(String newValue) {
        callStatus = newValue;
    }

    public static void setCallAction(String newValue) {
        callAction = newValue;
    }

    public static void setCallResolution(String newValue) {
        callResolution = newValue;
    }   //PUBLIC METHODS

  /*  public static void deleteCalls(String fileLocation) {
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
*/
    public static void saveCall() throws SQLException {
        //first ensure lookup data persists
        chkCallLookups();

        String sql = "SELECT * FROM tblCall WHERE call_id = ?";
        try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
            psSel.setInt(1, callId);
            final ResultSet rsSel = psSel.executeQuery();

            if (!rsSel.next()) {
                sql = "INSERT INTO tblCall (";
                sql += "call_id,";
                sql += "mem_no,";
                sql += "caller_name,";
                sql += "call_cat_id,";
                if (callSubCat.length() > 0) {
                    sql += "callsubcat_id,";
                }
                sql += "call_date_start,";
                sql += "call_note_start,";
                sql += "call_date_end,";
                sql += "call_duration,";
                sql += "callstatus_id,";
                sql += "callaction_id,";
                sql += "callres_id,";
                sql += "call_note_res,";
                sql += "call_location,";
                sql += "call_pilot_part)";

                if (callSubCat.length() > 0) {
                    sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                } else {
                    sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                }

                PreparedStatement psIns = conn.prepareStatement(sql);
                if (callSubCat.length() > 0) {
                    psIns.setInt(1, callId);
                    psIns.setString(2, memNum);
                    psIns.setString(3, callerName);
                    psIns.setInt(4, callCatId);
                    psIns.setInt(5, callSubCatId);
                    psIns.setString(6, callStartDate);
                    psIns.setString(7, callStartNote);
                    psIns.setString(8, callEndDate);
                    psIns.setInt(9, Integer.parseInt(callDuration));
                    psIns.setInt(10, callStatusId);
                    psIns.setInt(11, callActionId);
                    psIns.setInt(12, callResolutionId);
                    psIns.setString(13, callResNote);
                    psIns.setString(14, callLocation);
                    psIns.setString(15, callPilotPart);
                    psIns.execute();
                } else {
                    psIns.setInt(1, callId);
                    psIns.setString(2, memNum);
                    psIns.setString(3, callerName);
                    psIns.setInt(4, callCatId);
                    psIns.setString(5, callStartDate);
                    psIns.setString(6, callStartNote);
                    psIns.setString(7, callEndDate);
                    psIns.setInt(8, Integer.parseInt(callDuration));
                    psIns.setInt(9, callStatusId);
                    psIns.setInt(10, callActionId);
                    psIns.setInt(11, callResolutionId);
                    psIns.setString(12, callResNote);
                    psIns.setString(13, callLocation);
                    psIns.setString(14, callPilotPart);
                    psIns.executeUpdate();
                }
            } else {
                isDuplicate = true;
                DBCall.LOGGER.error("Duplicate CallID: " + memNum + ":  " + callId + ": Row Number:" + callRowNumber);
            }
        }
    }

    //Update Call Notes from CSV file
    public static void saveNotes(int id, String openNote, String resNote) {
        String sql = "SELECT * FROM tblCall WHERE call_id = ?";
        try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
            psSel.setInt(1, id);
            final ResultSet rsSel = psSel.executeQuery();

            if (rsSel.next()) {
                sql = "UPDATE tblCall ";
                sql += "SET call_note_start = ?, ";
                sql += "call_note_res = ? ";
                sql += "WHERE call_id = ? ";
                PreparedStatement psIns = conn.prepareStatement(sql);
                psIns.setString(1, openNote);
                psIns.setString(2, resNote);
                psIns.setInt(3, id);
                psIns.executeUpdate();

                DBCall.LOGGER.info("Notes for CallID: " + id + " - updated.");
            }
        } catch (SQLException e) {
            DBCall.LOGGER.error("Could not save Call Notes for call id" + callId + " to the database: DBCall.class(saveNotes)");
        } catch (Exception e) {
            DBCall.LOGGER.error("An unknown exception has occurred: DBCall.class");
        }
    }


    public static void saveMeta(String fileName) throws SQLException {
        String sql = "SELECT call_id FROM tblMeta WHERE call_id = ?";
        try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
            psSel.setInt(1, callId);
            final ResultSet rsSel = psSel.executeQuery();

            if (!rsSel.next()) {
                sql = "INSERT INTO tblMeta (";
                sql += "call_id, ";
                sql += "file_name, ";
                sql += "line_no, ";
                sql += "duplicate)";
                sql += "VALUES (?, ?, ?, ?)";

                PreparedStatement psIns = conn.prepareStatement(sql);
                psIns.setInt(1, callId);
                psIns.setString(2, fileName);
                psIns.setInt(3, callRowNumber);
                psIns.setBoolean(4, isDuplicate);
                psIns.executeUpdate();

            } else {
                sql = "UPDATE tblMeta "
                        + "SET duplicate = ? "
                        + "WHERE call_id = ?";

                PreparedStatement psUpd = conn.prepareStatement((sql));
                psUpd.setBoolean(1, isDuplicate);
                psUpd.setInt(2, callId);
                psUpd.executeUpdate();
            }
        }
    }

  /*  public static void deleteMeta(String fileLocation) {
        String sql = "DELETE FROM tblMeta ";
        sql += "WHERE xls_file = ?";
        try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
            psSel.setString(1, fileLocation);
            psSel.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
*/
    //For when this class is used as an 'array' .. simply for storing values before being
    //written to a new Calls record in the database.
    //When the code in ULoad.java finds a new call .. all but the 'header fields' are reset
    //via this method.
    public static void resetCallFields() {
        setCallStartNote("");
        setCallResolution("");
        setCallAction("");
        setCallStatus("");
        setCallEndDate("");
        setCallStartDate("");
        setCallId(0);
        setCallerName("");
        setIsDuplicate(false);
        //DONT RESET Header items or call counters as it needs to carry over if necessary!!
    }


    //PRIVATE METHODS
    private static void saveMember() throws SQLException {
        if (memNum.length() > 0) {
            String sql = "SELECT * FROM tblMember WHERE mem_no = ?";
            try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
                psSel.setString(1, memNum);
                final ResultSet rsSel = psSel.executeQuery();

                if (!rsSel.next()) {
                    sql = "INSERT INTO tblMember (" +
                            "mem_no," +
                            "client_ident," +
                            "mem_first_name," +
                            "mem_surname," +
                            "mem_position)" +
                            "VALUES (?, ?, ?, ?, ?)";

                    PreparedStatement psIns = conn.prepareStatement(sql);
                    psIns.setString(1, memNum);
                    psIns.setString(2, clIdent);
                    psIns.setString(3, memFirstName);
                    psIns.setString(4, memSurname);
                    psIns.setString(5, memPosition);
                    psIns.executeUpdate();
                }
            }
        }
    }

    //INSERT the DataBase Category record - retrieve the generated auto PK (else the one that exists!!)
    private static void saveEnqCat() throws SQLException {
        if (callCat.length() > 0) {
            String sql = "SELECT call_cat_id FROM tlkpCallCategory WHERE call_cat = ?";
            try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
                psSel.setString(1, callCat);
                final ResultSet rsSel = psSel.executeQuery();

                if (!rsSel.next()) {
                    sql = "INSERT INTO tlkpCallCategory (" +
                            "call_cat," +
                            "call_cat_desc)" +
                            "VALUES (?, ?)";

                    PreparedStatement psIns = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    psIns.setString(1, callCat);
                    psIns.setString(2, callCatDesc);
                    psIns.executeUpdate();

                    final ResultSet rsIns = psIns.getGeneratedKeys();
                    rsIns.next();
                    callCatId = rsIns.getInt(1);
                } else {
                    callCatId = rsSel.getInt(1);
                }
            }
        }
    }

    //INSERT the DataBase Sub Category record - retrieve the generated auto PK (else the one that exists!!)
    private static void saveEnqSubCat() throws SQLException {
        if (callSubCat.length() > 0) {
            String sql = "SELECT callsubcat_id " +
                    "FROM tlkpCallSubCategory " +
                    "INNER JOIN tlkpCallCategory " +
                    "ON tlkpCallCategory.call_cat_id = tlkpCallSubCategory.call_cat_id " +
                    "WHERE call_sub_cat = ?";
            try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
                psSel.setString(1, callSubCat);
                final ResultSet rsSel = psSel.executeQuery();

                if (!rsSel.next()) {
                    sql = "INSERT INTO tlkpCallSubCategory (" +
                            "call_cat_id," +
                            "call_sub_cat," +
                            "call_sub_cat_desc)" +
                            "VALUES (?, ?, ?)";

                    PreparedStatement psIns = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    psIns.setString(1, String.valueOf(callCatId));
                    psIns.setString(2, callSubCat);
                    psIns.setString(3, callSubCatCatdesc);
                    psIns.executeUpdate();

                    try (final ResultSet rsIns = psIns.getGeneratedKeys()) {
                        rsIns.next();
                        callSubCatId = rsIns.getInt(1);
                    }
                } else {
                    callSubCatId = rsSel.getInt(1);
                }
            }
        }
    }

    //INSERT the Call Status lookup record - retrieve the generated auto PK (else the one that exists!!)
    private static void saveCallStatus() throws SQLException {
        if (callStatus.length() > 0) {
            String sql = "SELECT callstatus_id FROM tlkpCallStatus WHERE call_status = ?";
            try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
                psSel.setString(1, callStatus);
                final ResultSet rsSel = psSel.executeQuery();

                if (!rsSel.next()) {
                    sql = "INSERT INTO tlkpCallStatus (" +
                            "call_status," +
                            "call_stat_desc)" +
                            "VALUES (?, ?)";

                    PreparedStatement psIns = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    psIns.setString(1, callStatus);
                    psIns.setString(2, callStatusDesc);
                    psIns.executeUpdate();

                    try (final ResultSet rsIns = psIns.getGeneratedKeys()) {
                        rsIns.next();
                        callStatusId = rsIns.getInt(1);
                    }

                } else {
                    callStatusId = rsSel.getInt(1);
                }
            }
        }
    }

    //INSERT the Call Action lookup record - retrieve the generated auto PK (else the one that exists!!)
    private static void saveCallAction() throws SQLException {
        if (callAction.length() > 0) {
            String sql = "SELECT callaction_id FROM tlkpCallAction WHERE call_action = ?";
            try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
                psSel.setString(1, callAction);
                final ResultSet rsSel = psSel.executeQuery();

                if (!rsSel.next()) {
                    sql = "INSERT INTO tlkpCallAction (" +
                            "call_action," +
                            "call_action_desc)" +
                            "VALUES (?, ?)";

                    PreparedStatement psIns = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    psIns.setString(1, callAction);
                    psIns.setString(2, callActionDesc);
                    psIns.executeUpdate();

                    try (final ResultSet rsIns = psIns.getGeneratedKeys()) {
                        rsIns.next();
                        callActionId = rsIns.getInt(1);
                    }
                } else {
                    callActionId = rsSel.getInt(1);
                }
            }
        }
    }

    //INSERT the Call Resolution lookup record - retrieve the generated auto PK(else the one that exists!!)
    private static void saveCallResolution() throws SQLException {
        if (callResolution.length() > 0) {
            String sql = "SELECT callres_id FROM tlkpCallResolution WHERE call_res = ?";
            try (Connection conn = DbHelper.getConnection(); PreparedStatement psSel = conn.prepareStatement(sql)) {
                psSel.setString(1, callResolution);
                final ResultSet rsSel = psSel.executeQuery();

                if (!rsSel.next()) {
                    sql = "INSERT INTO tlkpCallResolution (" +
                            "call_res," +
                            "call_res_desc)" +
                            "VALUES (?, ?)";

                    PreparedStatement psIns = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    psIns.setString(1, callResolution);
                    psIns.setString(2, callResolutionDesc);
                    psIns.executeUpdate();

                    try (final ResultSet rsIns = psIns.getGeneratedKeys()) {
                        rsIns.next();
                        callResolutionId = rsIns.getInt(1);
                    }

                } else {
                    callResolutionId = rsSel.getInt(1);
                }
            }
        }
    }

    //Make sure that the lookup data for this call are already in the lookup tables
    //If not .. put them there before saving the CALL
    private static void chkCallLookups() throws SQLException {
        saveMember();
        saveEnqCat();
        saveEnqSubCat();
        saveCallStatus();
        saveCallAction();
        saveCallResolution();
    }


}

