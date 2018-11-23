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

import com.zimpics.eds.app_wa01a.TPAnomaly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//This is a singleton class
public class TPAnomalyHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TPAnomalyHelper.class);

    private static final TPAnomalyHelper INSTANCE = new TPAnomalyHelper();

    public static TPAnomalyHelper getInstance() {
        return TPAnomalyHelper.INSTANCE;
    }

    private TPAnomalyHelper() {
    }

    public List<TPAnomaly> getAnomalies(String f) throws SQLException {
        TPAnomalyHelper.LOGGER.debug("Loading anomalies");
        final List<TPAnomaly> anomalies = new ArrayList<>();
        final String sql = "SELECT * FROM tblAnomalies WHERE file_name = " + "'" + f + "'" + " ORDER BY err_code";
        try (Connection connection = DbHelper.getConnection();
             PreparedStatement psmt = connection.prepareStatement(sql);
             ResultSet rs = psmt.executeQuery()) {

            while (rs.next()) {
                final TPAnomaly an = new TPAnomaly();
                an.setAnFName(rs.getString("file_name"));
                an.setAnLineNo(rs.getInt("line_no"));
                an.setAnColNo(rs.getInt("col_no"));
                an.setAnAttrib(rs.getString("attrib"));
                an.setAnErrCode(rs.getString("err_code"));
                an.setAnDescr(rs.getString("descr"));
                an.setAnAccept(rs.getString("accept"));
                anomalies.add(an);
            }
        }

        TPAnomalyHelper.LOGGER.debug("Loaded {} anomalies", anomalies.size());
        return anomalies;
    }

    public static void createAnomaly(String f, int l, int c, String a, String e, String d) throws SQLException {
        final TPAnomaly an = new TPAnomaly();
        an.setAnFName(f);
        an.setAnLineNo(l);
        an.setAnColNo(c);
        an.setAnAttrib(a);
        an.setAnErrCode(e);
        an.setAnDescr(d);
        an.setAnAccept("#");
        an.save();
    }

}
