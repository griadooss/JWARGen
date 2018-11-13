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

import com.zimpics.eds.app_wa01a.TPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//This is a singleton class
public class TPFileHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TPFileHelper.class);

    private static final TPFileHelper INSTANCE = new TPFileHelper();

    public static TPFileHelper getInstance() {
        return TPFileHelper.INSTANCE;
    }

    private TPFileHelper(){
    }

    public List<TPFile> getFiles() throws SQLException {
        TPFileHelper.LOGGER.debug("Loading files");
        final List<TPFile> files = new ArrayList<>();

        final String sql = "SELECT * FROM tblFile ORDER BY file_name";
        try (Connection connection = DbHelper.getConnection();
             PreparedStatement psmt = connection.prepareStatement(sql);
             ResultSet rs = psmt.executeQuery()) {

            while (rs.next()) {
                final TPFile f = new TPFile();
                f.setfName(rs.getString("file_name"));
                f.setDtProcessed(rs.getString("date_processed"));
                f.setfStatus(rs.getString("file_status"));
                f.setEnquiries(rs.getInt("enquiries"));
                f.setCalls(rs.getInt("calls"));
                f.setfTimesRun(rs.getInt("times_run"));
                files.add(f);
            }
        }

        TPFileHelper.LOGGER.debug("Loaded {} files", files.size());
        return files;
    }
}
