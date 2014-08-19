package needle;

import java.util.concurrent.Executor;

/**
 * Use for tasks that don't need the UI/main thread and should be executed on a background thread.
 */
public interface BackgroundThreadExecutor extends Executor {

    /**
     * Equivalent to {@code .withThreadPoolSizeOf(1)}
     *
     * @see #withThreadPoolSize(int)
     */
    BackgroundThreadExecutor serially();

    /**
     * Use this to define what kind of tasks the executor is used for. Tasks with different types will be executed
     * independently from each other, i.e. each type will have its separate executor.
     *
     * @param taskType Can be anything, e.g. "image-downloading", "rendering"
     */
    BackgroundThreadExecutor withTaskType(String taskType);

    /**
     * Use this to set the level of concurrency.
     *
     * @param poolSize Number of threads the executor will have in its thread pool
     */
    BackgroundThreadExecutor withThreadPoolSize(int poolSize);
}
