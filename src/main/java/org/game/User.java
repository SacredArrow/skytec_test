package org.game;

import org.apache.log4j.Logger;
import org.db.DBConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public class User implements IMoneyHolder {

    final static Logger logger = Logger.getLogger(User.class);
    private final long id;
    public User(long userId) {
        this.id = userId;
    }

    @Override
    public int getBalance() {
        try {
            ResultSet rs = DBConnector.runQuery("SELECT balance from users WHERE user_id = ?", this.id);
            rs.first();
            return rs.getInt("balance");
        } catch (SQLException e) {
            logger.error("Couldn't get balance for user " + id);
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
