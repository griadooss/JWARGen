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
package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TPFileTest {
    @After
    public void close() {
        DbHelper.getInstance().close();
    }

    @Before
    public void init() throws SQLException {
        DbHelper.getInstance().init();

        try (Connection connection = DbHelper.getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM tblFile;");
        }
    }

    @Test
    public void testSave() throws SQLException {
        final TPFile f = new TPFile();
        f.setfName("input_18_Q3");
        f.setDtProcessed("2018-11-05");
        f.setfStatus("test");
        f.setEnquiries(1234);
        f.setCalls(987);
        f.setfTimesRun(0);
        f.save();

        try (Connection connection = DbHelper.getConnection(); Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tblFile")) {
                Assert.assertTrue("Count should return at least one row", rs.next());
                Assert.assertEquals(1L, rs.getLong(1));
                Assert.assertFalse("Count should not return more than one row", rs.next());
            }

            try (ResultSet rs = stmt.executeQuery("SELECT * FROM tblFile")) {
                Assert.assertTrue("Select should return at least one row", rs.next());
                Assert.assertEquals(0L, rs.getLong(1));
                Assert.assertEquals("input_18_Q3", rs.getString("file_name"));
                Assert.assertEquals("2018-11-05", rs.getString("date_processed"));
                Assert.assertFalse("Select should not return more than one row", rs.next());
            }
        }
    }
}
