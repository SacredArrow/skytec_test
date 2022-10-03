package org.game;

import org.apache.log4j.Logger;
import org.db.DBConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class Clan implements IMoneyHolder{
    final static Logger logger = Logger.getLogger(Clan.class);
    private final long id;     // id клана

    public Clan(long clanId) {
        this.id = clanId;
    }

    // I've decided to rely on DB ACID, because we need our data to be synchronized between code and database.
    // By using multiple threads to write to database, we make sure that data will be written in parallel to different clans.
    // That's why we hold no balance as a variable, but query DB for it.
    public CompletableFuture<Boolean> addGold(int amount) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Start adding %d amount to clan with id %d".formatted(amount, this.id));
            boolean result = runAddGoldQuery(amount);
            if (result) {
                logger.info("Added %d amount to clan with id %d".formatted(amount, this.id));
            } else {
                logger.error("Error while adding %d amount to clan with id %d".formatted(amount, this.id));
            }
            return result;
        });
    }

    @Override
    public int getBalance() {
        try {
            ResultSet rs = DBConnector.runQuery("SELECT balance from clan WHERE clan_id = ?", this.id);
            rs.first();
            return rs.getInt("balance");
        } catch (SQLException e) {
            logger.error("Couldn't get balance for clan " + id);
            throw new RuntimeException(e);
        }
    }

    private boolean runAddGoldQuery(int amount) {
        ResultSet rs = DBConnector.runQuery("UPDATE clan SET balance = balance + ? WHERE clan_id = ?", amount, this.id);
        return rs != null;
    }

    public long getId() {
        return id;
    }
}