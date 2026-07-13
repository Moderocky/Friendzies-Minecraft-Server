package mx.kenzie.survival.builder.task;

import mx.kenzie.survival.builder.BuildManager;

public abstract class CancellableTask implements Task {

    protected final int id = BuildManager.createTaskId();
    protected boolean finished, cancelled;

    @Override
    public void finish() {
        this.finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isStopped() {
        return this.isCancelled() || this.isFinished();
    }

    @Override
    public int taskId() {
        return id;
    }
}
