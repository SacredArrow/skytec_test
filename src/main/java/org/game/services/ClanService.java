package org.game.services;

import org.db.DBConnector;
import org.game.Clan;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;

public class ClanService {

    @Nullable
    public static Clan get(long clanId) {
        ResultSet rs = DBConnector.runQuery("SELECT clan_id FROM clan where clan_id = ?", clanId);
        boolean res = DBConnector.isNotEmpty(rs);
        if (res) {
            return new Clan(clanId);
        } else {
            return null;
        }
    }
}
