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

import com.zimpics.eds.app_wa01a.WAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//This is a singleton class
public class WAClientHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(WAClientHelper.class);

    private static final WAClientHelper INSTANCE = new WAClientHelper();

    public static WAClientHelper getInstance() {
        return WAClientHelper.INSTANCE;
    }

    private WAClientHelper(){
    }



    public List<WAClient> getClients() throws SQLException {
        WAClientHelper.LOGGER.debug("Loading clients from client helper");

        final List<WAClient> clients = new ArrayList<>();

        final String sql = "SELECT * FROM tblClient ORDER BY client_ident";
        try (Connection connection = DbHelper.getConnection();
             PreparedStatement psmt = connection.prepareStatement(sql);
             ResultSet rs = psmt.executeQuery()) {

            while (rs.next()) {
                final WAClient c = new WAClient();
                c.setcId(rs.getString("client_ident"));
                c.setcName(rs.getString("client_name"));
                c.setcRptFreq(rs.getString("client_rpt_freq"));
                clients.add(c);
            }
        }

        WAClientHelper.LOGGER.debug("Loaded {} clients", clients.size());
        return clients;
    }
}
