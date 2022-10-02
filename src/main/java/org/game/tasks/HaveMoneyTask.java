package org.game.tasks;

import org.game.IMoneyHolder;

public class HaveMoneyTask implements ITask {

    private long id;
    private final int reward;
    private final int target;
    private final IMoneyHolder holder;

    public HaveMoneyTask(long id, int reward, int target, IMoneyHolder holder) {
        this(reward, target, holder);
        this.id = id;
    }

    public HaveMoneyTask(int reward, int target, IMoneyHolder holder) {
        this.reward = reward;
        this.target = target;
        this.holder = holder;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int getReward() {
        return reward;
    }

    @Override
    public String getName() {
        return "Have %d money".formatted(target);
    }

    @Override
    public String getDescription() {
        return "You need to have %d money on your balance".formatted(target);
    }

    @Override
    public boolean checkConditions() {
        return holder.getBalance() >= target;
    }
}
