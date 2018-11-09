package com.zimpics.eds.app_wa01a;

import com.zimpics.eds.app_wa01a.gui.Application;
import com.zimpics.eds.app_wa01a.helpers.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        DbHelper.getInstance().init();
        DbHelper.getInstance().registerShutdownHook();

        SwingUtilities.invokeLater(() -> {
            Main.LOGGER.debug("Starting application");

            final Application app = new Application();
            app.setTitle("Simple Java Database Swing Application");
            app.setSize(800, 600);
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



