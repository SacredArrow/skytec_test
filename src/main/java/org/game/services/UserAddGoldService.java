package org.game.services;

import org.apache.log4j.Logger;
import org.db.DBConnector;
import org.game.User;

import java.sql.Connection;
import java.sql.SQLException;

public class UserAddGoldService {
    final static Logger logger = Logger.getLogger(UserAddGoldService.class);

    public static boolean addGoldToClan(User user, long clanId, int gold)  {
        Connection connection = DBConnector.getConnection();
        return user.transferMoney(gold, () -> {
            try {
                connection.setAutoCommit(false);
                String query1 = """
                              UPDATE clan
                              SET balance = balance + ?
                              WHERE clan_id = ?
                        """;
                DBConnector.runQuery(query1, gold, clanId);

                String query2 = """
                              UPDATE users
                              SET balance = balance - ?
                              WHERE user_id = ?
                              """;
                DBConnector.runQuery(query2, gold, user.getId());
                connection.commit();
                return true;
            } catch (SQLException e) {
                logger.error(e);
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.error("Rollback failure!");
                    throw new RuntimeException(ex);
                }
                return false;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Auto-commit enablement error!");
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
