package com.zimpics.eds.app_wa01a;
//TODO: This method crashes (runs out of HEAP memeory) under Win10 with 32bit JVM installed
//Installing 64bit JVM solves the issue
//At this link there are suggestions how to solve the issue without having to
//install 64bit java
//https://stackoverflow.com/questions/6069847/java-lang-outofmemoryerror-java-heap-space-while-reading-excel-with-apache-poi

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import com.zimpics.eds.app_wa01a.helpers.TPAnomalyHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class VDate {

    //FIELDS
    private static boolean inCall = false;
    private static boolean missingData = false;
    private static boolean locationNext = false;
    private static boolean memberIdNext;
    private static boolean skipToNextMemId;

    private static final DataFormatter formatter = new DataFormatter();

    private static final Logger LOGGER = LoggerFactory.getLogger(VDate.class);

    //CONSTRUCTOR
    private VDate() {
    }


    //################ START MEMBER CLASS (FieldAttributes) ###################################
    static class FieldAttribute {
        private final String[] callProps = new String[11];

        private FieldAttribute() {
            setCallProps();
        }

        private void setCallProps() {
            callProps[0] = "Location";
            callProps[1] = "Pilot Participant";
            callProps[2] = "Call ID";
            callProps[3] = "Contact";
            callProps[4] = "Date Opened";
            callProps[5] = "Date Closed";
            callProps[6] = "Total Call Time";
            callProps[7] = "Status";
            callProps[8] = "Action";
            callProps[9] = "Call Resolved";
            callProps[10] = "Total Calls All";
        }

        private boolean isValidAttrib(String firstCol, boolean inCall, int rowNum) {
            if (inCall) {
                for (String s : callProps) {
                    if (s.equals(firstCol)) {
                        //processing is inside a call domain and the attribute is valid
                        return true;
                    }
                }
                //processing in inside a call domain but has found an INVALID call attribute
                if (locationNext) {
                    locationNext = false;
                    return true;
                } else {
                    System.out.println("Invalid call attribute: " + firstCol + " See row num: " + rowNum);
                    VDate.LOGGER.error("Invalid call attribute: " + firstCol + " See row num: " + rowNum);
                    return false;
                }
            } else {
                for (String s : callProps) {
                    if (s.equals(firstCol)) {
                        //processing in NOT inside a call domain but has found an attribute that belongs there???
                        System.out.println("Missing or invalid MemberId! See before row num: " + rowNum);
                        VDate.LOGGER.error("Missing or invalid Member Id! " + firstCol + " See row num: " + rowNum);
                        return false;
                    }
                }
            }

            return true;
        }

    }
    //################ END MEMBER CLASS (Field Attributes) ###################################

    //METHODS

    private static XSSFSheet readFile(String fileLocation) {
        try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(new File(fileLocation)))) {
            XSSFWorkbook workbook = new XSSFWorkbook(br);
            return workbook.getSheetAt(0);
        } catch (IOException e) {
            VDate.LOGGER.error("An error has occurred reading the file: ProcessSheet:readFile");
            // e.printStackTrace();
        }
        return null;
    }

    // START of main VALIDATE method
    public static void validate(TPFile inputXLS) throws SQLException {
        Properties properties = new Properties();
        properties.put("db.path", "data");

        String inFName = inputXLS.getfName();
        inputXLS.setfStatus("InValid");

        boolean initialLoad = inputXLS.chkIsInitalLoad(inFName);
        if (!initialLoad) {
            if (!DbHelper.resetData(inFName, 1)) {
                return;
            }
            inputXLS.resetFields();
        }

        XSSFSheet sheetToValidate = readFile("./" + properties.getProperty("db.path") + "/" + inFName);
        VDate.LOGGER.info("Validating: " + inFName);

        FieldAttribute fa = new FieldAttribute();
        if (sheetToValidate != null) {
            for (Row row : sheetToValidate) {
                int rowNum = row.getRowNum();
                if (sheetToValidate.isDisplayRowColHeadings()) {
                    rowNum += 1;
                }

                String s0 = formatter.formatCellValue(row.getCell(0)).trim();
                String s1 = formatter.formatCellValue(row.getCell(1)).trim();
                String s7 = formatter.formatCellValue(row.getCell(7)).trim();
                String s13 = formatter.formatCellValue(row.getCell(13)).trim();
                String s19 = formatter.formatCellValue(row.getCell(19)).trim();

                //the only way to determine the start of a CALL BLOCK
                if (s1.equals("Member")) {
                    memberIdNext = true;
                    skipToNextMemId = false;
                }
                //the skipToNextMemId flag is set when memberIdNext is TRUE but the data found next is
                //NOT a valid MemberId .. at that point the skip flag is set and stays set
                //until the "Member" value in column B is again found .. signalling the start
                //of a new call block (the memberid should be the next data encounterd
                if (skipToNextMemId) continue;

                if (s0.length() > 0) {
                    //Check against CLIENT ID in tblClient in database
                    String rtn = WAClient.isValidClient(s0);
                    if (rtn.length() > 0) {

                        //set flags accordingly
                        inputXLS.incEnquiries(1);
                        inCall = true;
                        locationNext = true;
                        memberIdNext = false;

                        System.out.println(s0 + ": Row Number : " + rowNum);

                    } else if (memberIdNext) {
                        TPAnomalyHelper.createAnomaly(inFName, rowNum, 13, s0, "FATAL", "Invalid Member Identifier");
                        //TPAnomalyHelper.save();
                        missingData = true;
                        System.out.println("invalid Member Id: " + s0 + " Row Num: " + rowNum);
                        VDate.LOGGER.error("Invalid Member ID: " + s0 + " See row num: " + rowNum);
                        memberIdNext = false;
                        skipToNextMemId = true;
                        //return false;
                    } else if (!fa.isValidAttrib(s0, inCall, rowNum)) {
                        TPAnomalyHelper.createAnomaly(inFName, rowNum, 13, s0, "FATAL", "Invalid Call attribute!");
                        missingData = true;
                        //return false;
                    }
                    switch (s0) {
                        case "Pilot Participant":
                            locationNext = false;
                            System.out.println(s0 + ": Row Number: " + rowNum);
                            break;
                        case "Call ID":
                            inputXLS.incCalls(1);
                            System.out.println(s0 + " : " + s7 + " : " + s13 + ": start of Call block: Row Num: " + rowNum);
                            if (s13.length() == 0) {
                                missingData = true;
                                try {
                                    throw (new Exception("SUB CATEGORY data missing: Row number " + rowNum + " Col: 13"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.warn(e.getMessage());
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 13, s0, "WARN", "Missing SubCategory");
                                }
                            }
                            break;
                        case "Contact":
                            System.out.println(s0 + ": Row Number: " + rowNum);
                            if (s7.length() == 0) {
                                missingData = true;
                                try {
                                    throw (new Exception("CALL ID missing: Row number " + rowNum + " Col: 7"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.error(e.getMessage());
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "FATAL", "Missing Call Id");
                                }
                            }
                            break;
                        case "Date Opened":
                            System.out.println(s0 + ": Row Number: " + rowNum);
                            if (s7.length() == 0) {
                                missingData = true;
                                try {
                                    throw (new Exception("NO OPENING DATE: Row number " + rowNum + " Col: 7"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.error((e.getMessage()));
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "FATAL", "Missing Open Date");
                                }
                            }
                            if (DbHelper.getDatePattern(s7).equals("Invalid")) {
                                missingData = true;
                                try {
                                    throw (new Exception("Invalid date format: Row number " + rowNum + " Col No: 7"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.error(e.getMessage());
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "FATAL", "Invalid opening date format");
                                }
                            }
                            if (s19.length() == 0) {
                                missingData = true;
                                try {
                                    throw (new Exception("DATE OPENED has NO NOTE: Row number " + rowNum + " column 19"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.warn(e.getMessage());
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 19, s0, "WARN", "Missing Call Opening Notes");
                                }
                            }
                            break;
                        case "Date Closed":
                            System.out.println(s0 + ": Row Number: " + rowNum);
                            if (s7.length() == 0) {
                                missingData = true;
                                try {
                                    throw (new Exception("Call NOT CLOSED: Row number " + rowNum + " Col No: 7"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.error(e.getMessage());
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "FATAL", "Call not closed");
                                }
                            }
                            if (DbHelper.getDatePattern(s7).equals("Invalid")) {
                                missingData = true;
                                try {
                                    throw (new Exception("Invalid date format: Row number " + rowNum + " Col No: 7"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.error(e.getMessage());
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "FATAL", "Invalid closing data format");
                                }
                            }
                            break;
                        case "Total Call Time":
                            System.out.println(s0 + ": Row Number: " + rowNum);
                            if (s7.length() == 0) {
                                missingData = true;
                                {
                                    try {
                                        throw (new Exception("NO TIME RECORDED: Row number " + rowNum + " Col No: 7"));
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                        VDate.LOGGER.warn(e.getMessage());
                                        TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "WARN", "Missing Call duration");
                                    }
                                }
                            }
                            if (!s7.matches("\\S+")) {
                                missingData = true;
                                {
                                    try {
                                        throw (new Exception("Invalid Call Time format: Row number " + rowNum + " Col No: 7"));
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                        VDate.LOGGER.warn(e.getMessage());
                                        TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "FATAL", "Invalid Call Time format");
                                    }
                                }
                            }
                            break;
                        case "Status":
                            System.out.println(s0 + ": Row Number: " + rowNum);
                            if (s7.length() == 0) {
                                missingData = true;
                                try {
                                    throw (new Exception("STATUS MISSING: Row number " + rowNum + " Col. No: 7"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.error(e.getMessage());
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "ERROR", "Missing Call Status");
                                }
                            }
                            break;
                        case "Action":
                            System.out.println(s0 + ": Row Number: " + rowNum);
                            if (s7.length() == 0) {
                                missingData = true;
                                try {
                                    throw (new Exception("ACTION missing: Row number " + rowNum + " Col. No: 7"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.error(e.getMessage());
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "ERROR", "Missing Call Action");
                                }
                            }
                            break;
                        case "Call Resolved":
                            System.out.println(s0 + ": end of call block: Row Num: " + rowNum);
                            System.out.println("##############################");
                            System.out.println();
                            if (s7.length() == 0) {
                                missingData = true;
                                try {
                                    throw (new Exception("NO CALL RESOLUTION: Row number " + rowNum + " Col. No: 7"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.error((e.getMessage()));
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 7, s0, "ERROR", "No Call Resolution");
                                }
                            }
                            if (s19.length() == 0) {
                                missingData = true;
                                try {
                                    throw (new Exception("CALL RESOLUTION has NO NOTE: Row number " + rowNum + " column 19"));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    VDate.LOGGER.warn(e.getMessage());
                                    TPAnomalyHelper.createAnomaly(inFName, rowNum, 19, s0, "WARN", "Missing Call Resolution Notes");
                                }
                            }
                            break;
                        case "Total Calls All":
                            inCall = false;
                            break;
                    }
                }
            }
        }
        if (missingData) {
            inputXLS.setfStatus("INVALID");
        } else {
            inputXLS.setfStatus("VALID");
        }
        inputXLS.setDtProcessed(DbHelper.myDateStamp());
        inputXLS.setfTimesRun(inputXLS.getfTimesRun() + 1);

        try {
            inputXLS.save();
        } catch (
                SQLException e) {
            e.printStackTrace();
        }
    }
}
