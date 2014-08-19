package needle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Use this class to execute tasks on the UI/main or on a background thread. You can execute them concurrently or
 * serially. You can even define different levels of concurrency for different task types.
 *
 * @see #onMainThread()
 * @see #onBackgroundThread()
 * @see needle.CancelableTask
 * @see needle.UiRelatedTask
 * @see needle.UiRelatedProgressTask
 * @see <a href="http://github.com/ZsoltSafrany/needle">Needle on GitHub</a>
 */
public class Needle {

    public static final int DEFAULT_POOL_SIZE = 3;
    public static final String DEFAULT_TASK_TYPE = "default";

    private static Executor sMainThreadExecutor = new MainThreadExecutor();

    /**
     * Use for tasks that need to run on the UI/main thread.
     */
    public static Executor onMainThread() {
        return sMainThreadExecutor;
    }

    /**
     * Use for tasks that don't need the UI/main thread and should be executed on a background thread.
     */
    public static BackgroundThreadExecutor onBackgroundThread() {
        return new ExecutorObtainer();
    }

    static class ExecutorObtainer implements BackgroundThreadExecutor {

        private static Map<ExecutorId, Executor> sCachedExecutors = new HashMap<ExecutorId, Executor>();

        private int mDesiredThreadPoolSize = DEFAULT_POOL_SIZE;
        private String mDesiredTaskType = DEFAULT_TASK_TYPE;

        @Override
        public BackgroundThreadExecutor serially() {
            withThreadPoolSize(1);
            return this;
        }

        @Override
        public BackgroundThreadExecutor withTaskType(String taskType) {
            if (taskType == null) {
                throw new IllegalArgumentException("Task type cannot be null");
            }
            mDesiredTaskType = taskType;
            return this;
        }

        @Override
        public BackgroundThreadExecutor withThreadPoolSize(int poolSize) {
            if (poolSize < 1) {
                throw new IllegalArgumentException("Thread pool size cannot be less than 1");
            }
            mDesiredThreadPoolSize = poolSize;
            return this;
        }

        @Override
        public void execute(Runnable runnable) {
            getExecutor().execute(runnable);
        }

        Executor getExecutor() {
            final ExecutorId executorId = new ExecutorId(mDesiredThreadPoolSize, mDesiredTaskType);
            synchronized (ExecutorObtainer.class) {
                Executor executor = sCachedExecutors.get(executorId);
                if (executor == null) {
                    executor = Executors.newFixedThreadPool(mDesiredThreadPoolSize);
                    sCachedExecutors.put(executorId, executor);
                }
                return executor;
            }
        }
    }

    private static class ExecutorId {
        private final int mThreadPoolSize;
        private final String mTaskType;

        private ExecutorId(int threadPoolSize, String taskType) {
            mThreadPoolSize = threadPoolSize;
            mTaskType = taskType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExecutorId executorId = (ExecutorId) o;
            if (mThreadPoolSize != executorId.mThreadPoolSize) return false;
            if (!mTaskType.equals(executorId.mTaskType)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 31 * mThreadPoolSize + mTaskType.hashCode();
        }
    }

}
