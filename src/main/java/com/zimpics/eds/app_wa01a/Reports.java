package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;


/*
http://appcrawler.com/wordpress/2014/01/21/using-apache-poi-to-generate-excel-spreadsheets-from-database-queries/
*/

public class Reports {

    //FIELDS
    private static final Logger LOGGER = LoggerFactory.getLogger(Reports.class);
    private String lastQuarter;
    private String lastQuarterYear;
    private String startDate;
    private String endDate;


    //GETTERS and SETTERS
  /*  public String getLastQuarter() {
        return lastQuarter;
    }
*/
    public void setLastQuarter(String newValue) {
        this.lastQuarter = newValue;
    }

  /*  public String getLastQuarterYear() {
        return lastQuarterYear;
    }
*/
    public void setLastQuarterYear(String newValue) {
        this.lastQuarterYear = newValue;
    }

    public void setStartDate(String newValue) {
        this.startDate = newValue;
    }


    public void setEndDate(String newValue) {
        this.endDate = newValue;
    }


    //METHODS
    public void rptDetClientDate(String waClient) {
        final Properties properties = new Properties();
        properties.put("db.reportsPath", "reports");
        try {
            properties.load(getClass().getResourceAsStream("/app.properties"));
        } catch (final IOException e) {
            Reports.LOGGER.error("Failed to load properties: Reports:rptDetClientDate");
        }
        String outpath = properties.getProperty("db.reportsPath");

        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -1);

            Connection conn = DbHelper.getConnection();
            String sql = "SELECT ca.mem_no, me.mem_surname, me.mem_first_name, " +
                    "lcc.call_cat, ca.call_location, ca.caller_name, lsc.call_sub_cat, ca.call_id, " +
                    "call_date_start, ca.call_note_start, ca.call_date_end, ca.call_duration, lcs.call_status, " +
                    "lca.call_action, lcr.call_res, ca.call_note_res\n" +
                    "FROM tblCall ca\n" +
                    "INNER JOIN tlkpCallStatus lcs on lcs.callstatus_id = ca.callstatus_id\n" +
                    "INNER JOIN tlkpCallAction lca on lca.callaction_id = ca.callaction_id\n" +
                    "INNER JOIN tlkpCallResolution lcr on lcr.callres_id = ca.callres_id\n" +
                    "LEFT OUTER JOIN tlkpCallSubCategory lsc on lsc.callsubcat_id = ca.callsubcat_id\n" +
                    "INNER JOIN tlkpCallCategory lcc on lcc.call_cat_id = ca.call_cat_id\n" +
                    "LEFT OUTER JOIN tblMember me on me.mem_no = ca.mem_no\n" +
                    "INNER JOIN tblClient cl on cl.client_ident = me.client_ident\n" +
                    "WHERE me.client_ident = " + "'" + waClient + "'" +
                    "AND ca.call_date_start BETWEEN " + "'" + startDate + "'" + "AND " + "'" + endDate + "'" + "\n" +
                    "ORDER BY  date(ca.call_date_start);\n";
            ResultSet rst = conn.createStatement().executeQuery(sql);

            ResultSet rstClName = conn.createStatement().executeQuery("SELECT cl.client_name " +
                    "FROM tblClient cl\n" +
                    "WHERE cl.client_ident = " + "'" + waClient + "'");

            ResultSet rstCount = conn.createStatement().executeQuery("SELECT COUNT(call_id) " +
                    "FROM tblCall ca\n" +
                    "INNER JOIN tlkpCallStatus lcs on lcs.callstatus_id = ca.callstatus_id\n" +
                    "INNER JOIN tlkpCallAction lca on lca.callaction_id = ca.callaction_id\n" +
                    "INNER JOIN tlkpCallResolution lcr on lcr.callres_id = ca.callres_id\n" +
                    "LEFT OUTER JOIN tlkpCallSubCategory lsc on lsc.callsubcat_id = ca.callsubcat_id\n" +
                    "INNER JOIN tlkpCallCategory lcc on lcc.call_cat_id = ca.call_cat_id\n" +
                    "LEFT OUTER JOIN tblMember me on me.mem_no = ca.mem_no\n" +
                    "INNER JOIN tblClient cl on cl.client_ident = me.client_ident\n" +
                    "WHERE me.client_ident = " + "'" + waClient + "'" +
                    "AND ca.call_date_start BETWEEN " + "'" + startDate + "'" + "AND " + "'" + endDate + "'" + "\n" +
                    "ORDER BY  date(ca.call_date_start);\n");
            int numEnq = rstCount.getInt(1);
            if (numEnq == 0) {
                return;
            }

     /*       ResultSet rstFileName = conn.createStatement().executeQuery("SELECT DISTINCT me.file_name\n" +
                    "FROM tblMeta me\n" +
                    "INNER JOIN tblCall ca on ca.call_id = me.call_id\n" +
                    "LEFT OUTER JOIN tblMember mb on mb.mem_no = ca.mem_no\n" +
                    "WHERE mb.client_ident = " + "'" + waClient + "'" +
                    "AND ca.call_date_start BETWEEN " + "'" + startDate + "'" + "AND " + "'" + endDate + "'");
*/
            String yR = lastQuarterYear;
            String qT = lastQuarter;
            //If the quarter variables are null the user must have ordered a specific date range.
            if (yR.length() == 0) {
                qT = startDate;
                yR = endDate;
            }


            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet(outpath);
            Row row;

            // ST01 - STYLE FOR MemberId
            CellStyle st01 = wb.createCellStyle();

            // font
            Font ft01 = wb.createFont();
            ft01.setFontName("Aerial");
            ft01.setFontHeightInPoints((short) 16);
            ft01.setBold(true);

            st01.setFont(ft01);
            st01.setVerticalAlignment(VerticalAlignment.CENTER);
            st01.setAlignment(HorizontalAlignment.LEFT);

            // foreground color
            st01.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            st01.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            //##########################################


            // ST02 - STYLE FOR Call Header cols 2,3 and 4
            CellStyle st02 = wb.createCellStyle();
            // font
            Font ft02 = wb.createFont();
            ft02.setFontName("Aerial");
            ft02.setFontHeightInPoints((short) 12);
            ft02.setBold(true);
            // foreground color
            st02.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            st02.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            st02.setFont(ft02);
            st02.setVerticalAlignment(VerticalAlignment.CENTER);
            st02.setAlignment(HorizontalAlignment.LEFT);
            //##########################################


            // ST03 - Not Used
            // CellStyle sty03 = wb.createCellStyle();
            //##########################################


            // ST04 - STYLE FOR Call Attributes
            CellStyle st04 = wb.createCellStyle();
            // font
            Font ft04 = wb.createFont();
            ft04.setFontName("Aerial");
            ft04.setFontHeightInPoints((short) 10);
            ft04.setBold(true);

            st04.setFont(ft04);
            st04.setVerticalAlignment(VerticalAlignment.TOP);
            st04.setAlignment(HorizontalAlignment.LEFT);
            //##########################################


            // ST05 - STYLE FOR General Text
            CellStyle st05 = wb.createCellStyle();
            // font
            Font ft05 = wb.createFont();
            ft05.setFontName("Aerial");
            ft05.setFontHeightInPoints((short) 10);
            ft05.setBold(false);

            st05.setFont(ft05);
            st05.setVerticalAlignment(VerticalAlignment.TOP);
            st05.setAlignment(HorizontalAlignment.LEFT);
            st05.setWrapText(true);
            //##########################################

            // ST06 - STYLE FOR Report Header (Client full name)
            CellStyle st06 = wb.createCellStyle();
            // font
            Font ft06 = wb.createFont();
            ft06.setFontName("Aerial");
            ft06.setFontHeightInPoints((short) 30);
            ft06.setBold(true);

            st06.setFont(ft06);
            //##########################################

            // ST07 - STYLE FOR Report Header (Report Name)
            CellStyle st07 = wb.createCellStyle();
            // font
            Font ft07 = wb.createFont();
            ft07.setFontName("Aerial");
            ft07.setFontHeightInPoints((short) 15);
            ft07.setBold(true);

            st07.setFont(ft07);
            //##########################################

            // ST08 - STYLE FOR Report Header (Final line)
            CellStyle st08 = wb.createCellStyle();
            // font
            Font ft08 = wb.createFont();
            ft08.setFontName("Aerial");
            ft08.setFontHeightInPoints((short) 8);
            ft08.setBold(false);

            st08.setFont(ft08);
            //##########################################

            sheet.setDefaultColumnWidth(20);
            sheet.setColumnWidth(3, 30000);
            sheet.setColumnWidth(2, 6000);
            sheet.setHorizontallyCenter(true);
            sheet.setVerticallyCenter(true);


            int rownum = 0;

            // Merging cells and writing Report Header
            Cell cell;
            //Add header rows
            for (int r = 0; r <= 8; r++) {
                row = sheet.createRow(rownum++);
                for (int rc = 0; rc <= 4; rc++) {
                    cell = row.createCell((short) rc);
                    if (rc == 0 && r == 3) {
                        if (rstClName.next()) {
                            row.setHeight((short) 750);
                            cell.setCellValue(rstClName.getString(1));
                            cell.setCellStyle(st06);
                            CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
                        }
                    }
                    if (rc == 0 && r == 4) {
                        row.setHeight((short) 375);
                        //cell.setCellValue(rstClName.getString(1));
                        cell.setCellValue("Detailed Calls Report");
                        cell.setCellStyle(st07);
                        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
                    }
                    if (rc == 0 && r == 6) {
                        row.setHeight((short) 375);
                        if (lastQuarter.length() == 0) {
                            cell.setCellValue(DbHelper.makeDateReport(qT) + " <--> "
                                    + DbHelper.makeDateReport(yR));
                        } else {
                            cell.setCellValue(qT + " - " + yR);
                        }
                        cell.setCellStyle(st07);
                        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
                    }
                    if (rc == 0 && r == 7) {
                        row.setHeight((short) 250);
                        cell.setCellValue("Enquiries Processed - " + numEnq);
                        cell.setCellStyle(st08);
                        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
                    }
                }
            }
            //Add merged cells for Header
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 3));
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 3));
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 3));
            sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 3));
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 3));

            while (rst.next()) {
                sheet.createRow(rownum++);
                sheet.createRow(rownum++);
                row = sheet.createRow(rownum++);
                for (int col = 1; col <= 16; col++) {
                    switch (col) {
                        case 1:
                            //write Mem No ... col 1
                            row.setHeight((short) 500);
                            cell = row.createCell(0);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st01);
                            break;
                        case 2:
                            //write Member surname col 2
                            cell = row.createCell(1);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st02);
                            break;
                        case 3:
                            //write Member first name col 3
                            cell = row.createCell(2);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st02);
                            break;
                        case 4:
                            //write Call Category - col 4
                            cell = row.createCell(3);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st02);
                            break;
                        case 5:
                            //write a new line .. write Location - col 2
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Location");
                            cell.setCellStyle(st04);
                            cell = row.createCell(1);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            //writea new line .. write Pilot Participant label
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Pilot Particpant");
                            cell.setCellStyle(st04);
                            break;
                        case 6:
                            //write 2 lines ... write Subject and CallerName
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Subject of Enq");
                            cell.setCellStyle(st04);
                            cell = row.createCell(1);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            break;
                        case 7:
                            //Dont write line -- write call sub category
                            cell = row.createCell(2);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            break;
                        case 8:
                            //write line -- write Call Id
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Enquiry No.");
                            cell.setCellStyle(st04);
                            cell = row.createCell(1);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            break;
                        case 9:
                            //write line -- write Open Date
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Date Opened");
                            cell.setCellStyle(st04);
                            cell = row.createCell(1);
                            cell.setCellValue(DbHelper.makeDateReport(rst.getString(col)));
                            cell.setCellStyle(st05);
                            break;
                        case 10:
                            //DONT write line -- write Open Date Note
                            cell = row.createCell(3);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            break;
                        case 11:
                            //write line -- write Closed Date
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Date Closed");
                            cell.setCellStyle(st04);
                            cell = row.createCell(1);
                            cell.setCellValue(DbHelper.makeDateReport(rst.getString(col)));
                            cell.setCellStyle(st05);
                            break;
                        case 12:
                            //write 2 lines -- write Call duration
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Call Time (mins)");
                            cell.setCellStyle(st04);
                            cell = row.createCell(1);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            break;
                        case 13:
                            //write line -- write STatus
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Status");
                            cell.setCellStyle(st04);
                            cell = row.createCell(1);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            break;
                        case 14:
                            //write line -- write Action
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Call Action");
                            cell.setCellStyle(st04);
                            cell = row.createCell(1);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            break;
                        case 15:
                            //write line -- write Resolution
                            row = sheet.createRow(rownum++);
                            cell = row.createCell(0);
                            cell.setCellValue("Call Resolved");
                            cell.setCellStyle(st04);
                            cell = row.createCell(1);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            break;
                        case 16:
                            //Dont write line -- write resolution notes
                            cell = row.createCell(3);
                            cell.setCellValue(rst.getString(col));
                            cell.setCellStyle(st05);
                            cell.getRow().setHeight((short) 0);
                            break;
                    }
                }
            }
            try {
                FileOutputStream out = new FileOutputStream(new File(outpath + "/" + waClient + "_" + qT + "_" + yR + "_WentAdv.xlsx"));
                wb.write(out);
                out.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }
}

