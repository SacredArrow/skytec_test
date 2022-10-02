package org.game.services;

import org.db.DBConnector;
import org.game.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static final Map<Long, User> userMap = new HashMap<>();
    public static User get(long userId) {
        if (userMap.containsKey(userId)) return userMap.get(userId);
        boolean res;
        try {
            PreparedStatement st = DBConnector.getConnection().prepareStatement("SELECT user_id FROM users where user_id = ?");
            st.setLong(1, userId);
            st.execute();
            st.getResultSet().last();
            res = st.getResultSet().getRow() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (res) {
            User user = new User(userId);
            userMap.put(userId, user);
            return user;
        } else {
            return null;
        }
    }
}
