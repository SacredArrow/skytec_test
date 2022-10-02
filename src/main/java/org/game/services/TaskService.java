package org.game.services;

import org.game.Clan;
import org.game.tasks.ITask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TaskService {
    private static final Map<Long, ITask> taskMap = new HashMap<>();
    private static long counter = 0;

    public static long registerTask(ITask task) {
        task.setId(counter);
        taskMap.put(counter, task);
        counter++;
        return task.getId();
    }

    public static CompletableFuture<Boolean> completeClanTask(long clanId, long taskId) {
        ITask task = taskMap.get(taskId);
        Clan clan = ClanService.get(clanId);
        if (task.checkConditions()) {
            assert clan != null;
            return clan.addGold(task.getReward());
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }
}
