import org.apache.log4j.Logger;
import org.db.DBConnector;
import org.game.Clan;
import org.game.User;
import org.game.services.ClanService;
import org.game.services.TaskService;
import org.game.services.UserAddGoldService;
import org.game.services.UserService;
import org.game.tasks.HaveMoneyTask;
import org.game.tasks.ITask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class GoldAddingTest {
    final static Logger logger = Logger.getLogger(GoldAddingTest.class);

    @BeforeEach
    public void setUp() {
        Connection connection = DBConnector.getConnection();
        try {
            Statement stm = connection.createStatement();
            stm.execute("DROP TABLE clan IF EXISTS");
            stm.execute("DROP TABLE users IF EXISTS");
            stm.execute("CREATE TABLE clan (clan_id bigint PRIMARY KEY, balance bigint, name VARCHAR(255))");
            stm.execute("CREATE TABLE users (user_id bigint PRIMARY KEY, balance bigint, name VARCHAR(255))");
            stm.execute("INSERT INTO clan VALUES (1, 0, 'NAVI')");
            stm.execute("INSERT INTO clan VALUES (2, 0, 'Virtus Pro')");
            stm.execute("INSERT INTO users VALUES (1, 1000, 'Solo')");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void checkEntriesSimple() {
        assertNotNull(ClanService.get(1));
        assertNull(ClanService.get(3));
    }

    @Test
    public void addGoldSimple() throws ExecutionException, InterruptedException {
        Clan clan = ClanService.get(1);
        boolean res = clan.addGold(100).get();
        assertTrue(res);
        int balance = clan.getBalance();
        assertEquals(balance, 100);
        res = clan.addGold(132).get();
        assertTrue(res);
        balance = clan.getBalance();
        assertEquals(balance, 232);
    }

    @Test
    public void concurrentGoldAddition() throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        Clan clan = ClanService.get(1);
        int n_iterations = 1000;
        int addedValue = 10;
        List<CompletableFuture<Boolean>> features = new ArrayList<>(n_iterations);
        for (int i = 0; i < n_iterations; i++) {
            assert clan != null;
            features.add(clan.addGold(addedValue));
        }
        int result = addedValue * n_iterations;
        for (int i = 0; i < n_iterations; i++) {
            boolean res = features.get(i).get();
            assertTrue(res);
        }
        assertEquals(clan.getBalance(), result);
        logger.info("Test took %d milliseconds.".formatted(System.currentTimeMillis() - start));
    }

    @Test
    public void concurrentGoldAdditionToDifferentClans() throws ExecutionException, InterruptedException, SQLException {
        Connection connection = DBConnector.getConnection();
        int n_clans = 100;
        for (int i = 3; i < n_clans + 3; i++) {
            PreparedStatement stm = connection.prepareStatement("INSERT INTO clan VALUES (?, 0, 'name')");
            stm.setLong(1, i);
            stm.execute();
        }
        long start = System.currentTimeMillis();
        int n_iterations = 1000;
        int addedValue = 10;
        List<CompletableFuture<Boolean>> features = new ArrayList<>(n_iterations * n_clans);
        for (int i = 3; i < n_clans + 3; i++) {
            Clan clan = ClanService.get(i);
            for (int j = 0; j < n_iterations; j++) {
                assert clan != null;
                features.add(clan.addGold(addedValue));
            }
        }
        int result = addedValue * n_iterations;
        for (int i = 0; i < n_iterations * n_clans; i++) {
            boolean res = features.get(i).get();
            assertTrue(res);
        }
        for (int i = 3; i < n_clans + 3; i++) {
            Clan clan = ClanService.get(i);
            assert clan != null;
            assertEquals(clan.getBalance(), result);
        }
        // About 10 times longer than in one clan.
        // Since we added 100 clans, it means that we process inserts for different clans somewhat independently, as intended.
        logger.info("Test took %d milliseconds.".formatted(System.currentTimeMillis() - start));
    }

    @Test
    public void transferGoldToUser() {
        User user = UserService.get(1);
        assert user != null;
        boolean res = UserAddGoldService.addGoldToClan(user, 1, 322);
        assertTrue(res);
        int balance = user.getBalance();
        assertEquals(balance, 1000 - 322);
        int clanBalance = ClanService.get(1).getBalance();
        assertEquals(clanBalance, 322);
    }

    @Test
    public void completeTaskTest() throws ExecutionException, InterruptedException {
        Clan clan = ClanService.get(1);
        ITask task = new HaveMoneyTask(100, 300, clan);
        long id = TaskService.registerTask(task);
        assert clan != null;
        boolean result = TaskService.completeClanTask(clan.getId(), id).get();
        assertFalse(result);
        User user = UserService.get(1);
        assert user != null;
        UserAddGoldService.addGoldToClan(user, 1, 322);
        result = TaskService.completeClanTask(clan.getId(), id).get();
        assertTrue(result);
        assertEquals(322 + 100, clan.getBalance());
    }
}
