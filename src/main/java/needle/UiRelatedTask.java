package needle;

import android.os.Handler;
import android.os.Looper;

/**
 * Use for tasks where you first want to do some work (usually on a background thread) and then some UI-related work
 * on the UI/main thread based on the result of the preceding work.
 *
 * @param <Result> Type of the result that is passed to thenDoUiRelatedWork() from doWork()
 */
public abstract class UiRelatedTask<Result> extends AbstractCancelableRunnable implements CancelableRunnable {

    protected static Handler sUiHandler = new Handler(Looper.getMainLooper());

    @Override
    public void run() {
        if (!isCanceled()) {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            final Result result = doWork();
            if (!isCanceled()) {
                sUiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (!isCanceled()) {
                            thenDoUiRelatedWork(result);
                        }
                    }
                });
            }
        }
    }

    /**
     * Defines the task that will be executed. Won't be called if {@code cancel()} is already called.
     *
     * @return The result that will be then used on the UI/main thread (gets passed to thenDoUiRelatedWork())
     * @see #thenDoUiRelatedWork(Object)
     * @see #cancel()
     */
    protected abstract Result doWork();

    /**
     * Defines the task that will be executed on the UI/main thread. Won't be called if {@code cancel()} is already called.
     *
     * @param result Result of the preceding work (comes from doWork())
     * @see #doWork()
     * @see #cancel()
     */
    protected abstract void thenDoUiRelatedWork(Result result);
}
