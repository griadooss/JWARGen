package com.zimpics.eds.app_wa01a;

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

public class ULoad {

    private static boolean locationNext;
    private static boolean inCallHeader;
    private static boolean memberIdNext;
    private static boolean skipToNextMemId;
    private static String inFName;
    private static String fPath; //shared with inner class CSVNotes

    private static final DataFormatter formatter = new DataFormatter();

    private static final Logger LOGGER = LoggerFactory.getLogger(ULoad.class);
    private static TPFile inputXLS;

    //CONSTRUCTOR
    private ULoad() {
    }

    //INNER CLASS ######################## START MEMBER CLASS (CSVNotes) #######################
    static class CSVNotes {

        private static int countNotes;
        private static int countErr;

        public static final Logger LOGGER = LoggerFactory.getLogger(CSVNotes.class);

        private CSVNotes() {
        }

        public static boolean loadNotes() throws SQLException {
            ULoad.LOGGER.info("Merging CSV Notes: " + inFName);
            XSSFSheet sheet2 = readFile(fPath, 1);

            ULoad.CSVNotes.LOGGER.info("Notes File: " + inFName + ": index: " + "1");

            if (sheet2 != null) {
                for (Row row : sheet2) {
                    int rowNum = row.getRowNum();
                    if (sheet2.isDisplayRowColHeadings()) {
                        rowNum += 1;
                    }
                    String s0 = formatter.formatCellValue(row.getCell(0)).trim(); //Col 1
                    String s1 = formatter.formatCellValue(row.getCell(1)).trim(); //key
                    String s37 = formatter.formatCellValue(row.getCell(37)).trim(); //Call id (int)
                    String s40 = formatter.formatCellValue(row.getCell(40)).trim(); //Opening NOTES
                    String s51 = formatter.formatCellValue(row.getCell(51)).trim(); //Resolution Notes

                    if (!s1.equals("Report Between")) {
                        //line is invalid  ... write line num to database tblAnomalies
                        TPAnomalyHelper.createAnomaly(inFName, rowNum, 1, "Call Notes", "Orphaned Notes", s0);
                        countErr += 1;
                    } else {
                        DBCall.saveNotes(Integer.parseInt(s37), s40, s51);
                        countNotes += 1;
                    }
                }
            } else return false;

            inputXLS.setfStatus("Notes Processed");

            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("##############################");

            System.out.println("SUCCESS ... NOTES Uploaded!");
            System.out.println("##############################");
            return true;
        }
    }
    //######################## END MEMBER CLASS (CSVNotes) #######################

    //METHODS
    private static XSSFSheet readFile(String fileLocation, int indx) {
        try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(new File(fileLocation)))) {
            XSSFWorkbook workbook = new XSSFWorkbook(br);
            return workbook.getSheetAt(indx);
        } catch (IOException e) {
            ULoad.LOGGER.error("An error has occurred reading the file: ProcessSheet:readFile");
            // e.printStackTrace();
        }
        return null;
    }

    //MAIN PROCESSING STARTS HERE ... Notes are merged after this routine finished.
    public static boolean upload(TPFile file) throws SQLException {
        inputXLS = file;
        Properties properties = new Properties();
        properties.put("db.path", "data");

        inFName = inputXLS.getfName();

        boolean initialLoad = inputXLS.chkIsInitalLoad(inFName);
        if (!initialLoad) {
            if (!DbHelper.resetData(inFName, 2)) {
                return false;
            }
            inputXLS.resetFields();
        }

        fPath = "./" + properties.getProperty("db.path") + "/" + inFName;
        XSSFSheet sheet1 = readFile(fPath, 0);
        ULoad.LOGGER.info("Uploading file: " + inFName);

        if (sheet1 != null) {
            for (Row row : sheet1) {
                int rowNum = row.getRowNum();
                if (sheet1.isDisplayRowColHeadings()) {
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

                //FIELDS
                //boolean inCall;
                if (s0.length() > 0) {
                    //Check against CLIENT ID in tblClient in database
                    String rtn = WAClient.isValidClient(s0);
                    if (rtn.length() > 0) {
                        //store call values
                        DBCall.setClIdent(rtn);
                        DBCall.setMemNum(s0);
                        DBCall.setMemFirstName(s13);
                        DBCall.setMemSurname(s7);
                        DBCall.setCallCat(s19);
                        DBCall.setMemPosition("");
                        inputXLS.incEnquiries(1);
                        //set flags accordingly
                        //inCall = true;
                        locationNext = true;
                        memberIdNext = false;
                        inCallHeader = true;
                        System.out.println(s0 + ": Row Number : " + rowNum);
                    } else if (inCallHeader && locationNext) {
                        if (!s0.equals("Pilot Participant") && (!s0.equals("Call ID"))) {
                            DBCall.setCallLocation(s0);
                            locationNext = false;
                        }
                    } else if (memberIdNext) {
                        ULoad.LOGGER.error(inFName, rowNum, 13, s0, "FATAL", "Invalid Member Identifier");
                        memberIdNext = false;
                        skipToNextMemId = true;
                        return false;
                    }
                }

                switch (s0) {
                    case "Pilot Participant":
                        locationNext = false;
                        DBCall.setCallPilotPart(s7);
                        System.out.println(s0 + ": Row Number: " + rowNum);
                        break;
                    case "Call ID":
                        inputXLS.incCalls(1);
                        DBCall.setCallerName(s7);
                        DBCall.setCallSubCat(s13);
                        System.out.println(s0 + " : " + s7 + " : " + s13 + ": start of Call block: Row Num: " + rowNum);
                        break;
                    case "Contact":
                        //setCallId HERE because in the spreadsheet it appears in against 'Contact' and not 'Call ID'
                        DBCall.setCallId(Integer.parseInt(s7.replaceAll("[^\\d]", "")));
                        DBCall.setCallRowNumber(rowNum);
                        System.out.println(s0 + ": Row Number: " + rowNum);
                        break;
                    case "Date Opened":
                        DBCall.setCallStartDate(DbHelper.makeDateSQLite(s7));
                        DBCall.setCallStartNote(s19);
                        System.out.println(s0 + ": Row Number: " + rowNum);
                        break;
                    case "Date Closed":
                        DBCall.setCallEndDate(DbHelper.makeDateSQLite(s7));
                        System.out.println(s0 + ": Row Number: " + rowNum);
                        break;
                    case "Total Call Time":
                        DBCall.setCallDuration(s7);
                        System.out.println(s0 + ": Row Number: " + rowNum);
                        break;
                    case "Status":
                        DBCall.setCallStatus(s7);
                        System.out.println(s0 + ": Row Number: " + rowNum);
                        break;
                    case "Action":
                        DBCall.setCallAction(s7);
                        System.out.println(s0 + ": Row Number: " + rowNum);
                        break;
                    case "Call Resolved":
                        DBCall.setCallResolution(s7);
                        DBCall.setCallResNote(s19);
                        DBCall.saveCall();
                        DBCall.saveMeta(inFName);
                        DBCall.resetCallFields();
                        //CallLocation is reset here
                        //as a work around as it really is NOT
                        //a field in the dataBase structure.
                        //It appears as an ad hoc inclusion that
                        //occurs on the first column of the
                        //line following MemberIdent. So kept
                        //separate here for clarity.
                        DBCall.setCallLocation("");
                        System.out.println(s0 + ": end of call block: Row Num: " + rowNum);
                        System.out.println("##############################");
                        System.out.println();
                        break;
                    case "Total Calls All":
                        inCallHeader = false;
                        //  inCall = false;
                        break;
                }
            }
        }
        inputXLS.setfStatus("UpLoaded");
        inputXLS.setDtProcessed(DbHelper.myDateStamp());
        inputXLS.setfTimesRun(inputXLS.getfTimesRun() + 1);

        try {
            inputXLS.save();
        } catch (
                SQLException e) {
            e.printStackTrace();
        }

        return CSVNotes.loadNotes();
    }
}
