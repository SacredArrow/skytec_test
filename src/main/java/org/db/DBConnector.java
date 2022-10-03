package org.db;


import org.apache.log4j.Logger;
import org.h2.tools.SimpleResultSet;

import java.sql.*;

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

    public static ResultSet runQuery(String query, Object ... parameters) {
        PreparedStatement st;
        try {
            st = getConnection().prepareStatement(query);
            for (int i = 0; i < parameters.length; i++) {
                Object parameter = parameters[i];
                if (parameter instanceof Long) {
                    st.setLong(i + 1, (Long) parameter);
                } else if (parameter instanceof Integer) {
                    st.setInt(i + 1, (Integer) parameter);
                } else {
                    logger.error("Unsupported type parameter for query: " + parameter.getClass());
                    return null;
                }
            }
            boolean hasResultSet = st.execute();
            if (hasResultSet) {
                return st.getResultSet(); // It isn't update statement
            } else if (st.getUpdateCount() > -1) {
                return new SimpleResultSet(); // It is update statement and it finished correctly
            } else {
                return null; // There was an error
            }

        } catch (SQLException e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * Should only be called on fresh set, since H2 doesn't support scrolling.
     * This method changes position of cursor, so be careful if you want to use ResultSet later.
     */
    public static boolean isNotEmpty(ResultSet rs) {
        try {
            return rs.next(); // Returns false if there is no first row
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
