package mx.kenzie.survival.utility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Timings {
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    public static final ExecutorService executor = Executors.newCachedThreadPool();
}
