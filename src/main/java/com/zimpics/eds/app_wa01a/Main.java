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

import com.zimpics.eds.app_wa01a.GUI.Application;
import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        DbHelper.getInstance().init();
        DbHelper.getInstance().registerShutdownHook();
        WAClient.init();

        SwingUtilities.invokeLater(() -> {
            Main.LOGGER.debug("Starting application");

            final Application app = new Application();
            app.setTitle("Wentworth Advantage Report Generator");
            app.setSize(800, 400);
            app.setLocationRelativeTo(null);
            app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            app.setVisible(true);


            // app.addWindowListener(new WindowAdapter() {
            // @Override
            // public void windowClosing(WindowEvent e) {
            // Main.LOGGER.info("Done");
            // DbHelper.getInstance().close();
            // }
            // });
        });
    }
    //  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
}



