package org.game.tasks;

public interface ITask {

    long getId();

    void setId(long id);

    int getReward();

    String getName();

    String getDescription();

    boolean checkConditions();
}
