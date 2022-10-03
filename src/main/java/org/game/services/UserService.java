package org.game.services;

import org.db.DBConnector;
import org.game.User;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static final Map<Long, User> userMap = new HashMap<>();
    public static User get(long userId) {
        if (userMap.containsKey(userId)) return userMap.get(userId);
        ResultSet rs = DBConnector.runQuery("SELECT user_id FROM users where user_id = ?", userId);
        boolean res = DBConnector.isNotEmpty(rs);
        if (res) {
            User user = new User(userId);
            userMap.put(userId, user);
            return user;
        } else {
            return null;
        }
    }
}
