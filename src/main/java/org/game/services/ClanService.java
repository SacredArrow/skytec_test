package org.game.services;

import org.db.DBConnector;
import org.game.Clan;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
public class ClanService {

    @Nullable
    public static Clan get(long clanId) {
        boolean res;
        try {
            PreparedStatement st = DBConnector.getConnection().prepareStatement("SELECT clan_id FROM clan where clan_id = ?");
            st.setLong(1, clanId);
            st.execute();
            st.getResultSet().last();
            res = st.getResultSet().getRow() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (res) {
            return new Clan(clanId);
        } else {
            return null;
        }
    }
}
