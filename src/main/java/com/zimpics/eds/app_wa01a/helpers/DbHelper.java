package com.zimpics.eds.app_wa01a.helpers;


import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

//This is a singleton class
public class DbHelper {

    //This is a singleton class
    //instantiation of the singleton through a field of the same type ...usually called INSTANCE
    //#########################
    public static DbHelper getInstance() {
        return DbHelper.INSTANCE;
    }

    private static final DbHelper INSTANCE = new DbHelper();//when this class is loaded this field is initialized, and this will be the only instance of this class

    //private constructor .. only this class can instantiate it ..nobody else can create an instance of this .. it is a singleton
    private DbHelper() {}
    //#########################

    //the datasource is the only field of this class!!
    private BasicDataSource ds;

    private static final Logger LOGGER = LoggerFactory.getLogger(DbHelper.class);

    public void close() {
        if (ds != null) {
            DbHelper.LOGGER.debug("Closing the data source: DbHelper");
            try {
                ds.close();
            } catch (SQLException e) {
                DbHelper.LOGGER.error("Failed to close the datasource", e);
            }
        }
    }

    private BasicDataSource getDataSource() {
        return ds;
    }

    public void init() {
        DbHelper.LOGGER.debug("Creating the data source");
        ds = new BasicDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:./target/wa.db");
        ds.setUsername("");
        ds.setPassword("");

        //Flyway put here as it needs to be the very first thing that runs after
        //the datasource has been created .. to make sure the database is latest version.
        DbHelper.LOGGER.debug("Executing Flyway (database migration)");
        Flyway flyway = new Flyway();
        try {
            flyway.setDataSource(ds);
            flyway.migrate();
        } catch (FlywayException e) {
            //e.printStackTrace();
            DbHelper.LOGGER.error("Flyway unable to obtain database connection: DbHelper");
        }
    }

    public void registerShutdownHook() {
        //This takes away the responsibility of closing threads properly
        //so whenever the JVM shuts down it will always close the datasource.
        Runtime.getRuntime().addShutdownHook(new Thread(this::close)); //, "DB-Helper-Thread"));

        //Give the thread a name for ease of spotting in the log.
        //However, the gotcha is that thread names have to be unique ., so in order not to complicate
        //matters best to let the system handle the naming .. so i have removed it!

    }

    public static Connection getConnection() throws SQLException {
        return DbHelper.getInstance().getDataSource().getConnection();
    }

}

