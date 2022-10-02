package org.game.services;

import org.apache.log4j.Logger;
import org.db.DBConnector;
import org.game.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
                PreparedStatement stm = connection.prepareStatement(query1);
                stm.setInt(1, gold);
                stm.setLong(2, clanId);
                stm.executeUpdate();

                String query2 = """
                              UPDATE users
                              SET balance = balance - ?
                              WHERE user_id = ?
                              """;
                PreparedStatement stm2 = connection.prepareStatement(query2);
                stm2.setInt(1, gold);
                stm2.setLong(2, user.getId());
                stm2.executeUpdate();
                connection.commit();
                return true;
            } catch (SQLException e) {
                logger.error(e);
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                return false;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
