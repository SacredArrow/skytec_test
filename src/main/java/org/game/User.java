package org.game;

import org.db.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Supplier;

public class User implements IMoneyHolder {
    private final long id;
    public User(long userId) {
        this.id = userId;
    }

    @Override
    public int getBalance() {
        Connection connection = DBConnector.getConnection();
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT balance from users WHERE user_id = ?");
            stm.setLong(1, this.id);
            stm.execute();
            stm.getResultSet().first();
            return stm.getResultSet().getInt("balance");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * All money transfers should be called through this method to evade money duplication.
     */
    public synchronized boolean transferMoney(int amount, Supplier<Boolean> callback) {
        if (getBalance() < amount) {
            return false;
        } else {
            return callback.get();
        }
    }

    public long getId() {
        return id;
    }
}
