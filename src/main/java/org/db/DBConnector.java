package org.db;


import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    final static Logger logger = Logger.getLogger(DBConnector.class);

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            String jdbcURL = System.getenv("SERVER"); // Default is SERVER=jdbc:h2:~/test;user=sa;PASSWORD=
            if (jdbcURL == null) {
                logger.error("Need to set server URL!");
            }
            String username = System.getenv("USER");
            if (username == null) {
                logger.error("Need to set user name!");
            }
            String password = System.getenv("PASSWORD");
            if (password == null) {
                logger.error("Need to set user password!");
            }


            try {
                assert jdbcURL != null;
                connection = DriverManager.getConnection(jdbcURL, username, password);
            } catch (SQLException e) {
                logger.error("Couldn't connect to DB!");
                throw new RuntimeException(e);
            }

        }
        return connection;
    }
}
