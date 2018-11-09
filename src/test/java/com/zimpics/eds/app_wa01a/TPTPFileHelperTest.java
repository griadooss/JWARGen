package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import com.zimpics.eds.app_wa01a.helpers.TPFileHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class TPTPFileHelperTest {
//THIS before AND after CODE has been copied directly from TPFileTest ...
//this code should really be put in its own class and called as needed.

//Preparation code
    @Before
    public void init() throws SQLException {
        DbHelper.getInstance().init();
        //this test obviously operates on an MT table ... for test on updating need to do something else
        try (Connection conn = DbHelper.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM tblFile;");
        }
    }

    //Cleaning code
    @After
    public void close() {
        DbHelper.getInstance().close();
    }

    @Test
    public void testLoad() throws SQLException {
        List<TPFile> files = TPFileHelper.getInstance().getFiles();
        Assert.assertNotNull(files);
        Assert.assertTrue(files.isEmpty());

        try (Connection conn = DbHelper.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO tblFile (file_name, date_processed, file_status, enquiries, calls, times_run) " +
                    "VALUES ('input_16_Q1', '2016-04-04', 'unprocessed', 1111, 999,0);");
            stmt.execute("INSERT INTO tblFile (file_name, date_processed, file_status, enquiries, calls, times_run) " +
                    "VALUES ('input_16_Q2', '2016-07-05', 'unprocessed', 2222, 1111,1);");
            stmt.execute("INSERT INTO tblFile (file_name, date_processed, file_status, enquiries, calls, times_run) " +
                    "VALUES ('input_16_Q3', '2016-10-06', 'unprocessed', 3333, 2222,2);");
            stmt.execute("INSERT INTO tblFile (file_name, date_processed, file_status, enquiries, calls, times_run) " +
                    "VALUES ('input_16_Q4', '2017-01-07', 'unprocessed', 4444, 3333,3);");
            stmt.execute("INSERT INTO tblFile (file_name, date_processed, file_status, enquiries, calls, times_run) " +
                    "VALUES ('input_17_Q1', '2017-04-08', 'unprocessed', 5555, 4444,4);");


            files = TPFileHelper.getInstance().getFiles();
            Assert.assertNotNull(files);
            Assert.assertEquals(5, files.size());

            //Retrieve first record in tblFile and verify it is what you inserted above.
            TPFile file = files.get(0);
            Assert.assertNotNull(file);
            Assert.assertEquals("input_16_Q1", file.getfName());
            Assert.assertEquals("2016-04-04", file.getDtProcessed());
            Assert.assertEquals("unprocessed", file.getfStatus());
            Assert.assertEquals(1111L, file.getEnquiries());
            Assert.assertEquals(999L, file.getCalls());
            Assert.assertEquals(0L, file.getfTimesRun());

            //Retrieve second record in tblFile and verify it is what you inserted above.
            file = files.get(1);
            Assert.assertNotNull(file);
            Assert.assertEquals("input_16_Q2", file.getfName());
            Assert.assertEquals("2016-07-05", file.getDtProcessed());
            Assert.assertEquals("unprocessed", file.getfStatus());
            Assert.assertEquals(2222L, file.getEnquiries());
            Assert.assertEquals(1111L, file.getCalls());
            Assert.assertEquals(1L, file.getfTimesRun());

            //Retrieve third record in tblFile and verify it is what you inserted above.
            file = files.get(2);
            Assert.assertNotNull(file);
            Assert.assertEquals("input_16_Q3", file.getfName());
            Assert.assertEquals("2016-10-06", file.getDtProcessed());
            Assert.assertEquals("unprocessed", file.getfStatus());
            Assert.assertEquals(3333L, file.getEnquiries());
            Assert.assertEquals(2222L, file.getCalls());
            Assert.assertEquals(2L, file.getfTimesRun());

            //Retrieve fourth record in tblFile and verify it is what you inserted above.
            file = files.get(3);
            Assert.assertNotNull(file);
            Assert.assertEquals("input_16_Q4", file.getfName());
            Assert.assertEquals("2017-01-07", file.getDtProcessed());
            Assert.assertEquals("unprocessed", file.getfStatus());
            Assert.assertEquals(4444L, file.getEnquiries());
            Assert.assertEquals(3333L, file.getCalls());
            Assert.assertEquals(3L, file.getfTimesRun());

            //Retrieve fifth record in tblFile and verify it is what you inserted above.
            file = files.get(4);
            Assert.assertNotNull(file);
            Assert.assertEquals("input_17_Q1", file.getfName());
            Assert.assertEquals("2017-04-08", file.getDtProcessed());
            Assert.assertEquals("unprocessed", file.getfStatus());
            Assert.assertEquals(5555L, file.getEnquiries());
            Assert.assertEquals(4444L, file.getCalls());
            Assert.assertEquals(4L, file.getfTimesRun());

        }

    }
}
