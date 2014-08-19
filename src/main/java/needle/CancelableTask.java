package needle;

/**
 * This is a task that can be canceled with {@code cancel()}.
 *
 * @see #cancel()
 * @see #isCanceled()
 */
public abstract class CancelableTask extends AbstractCancelableRunnable implements CancelableRunnable {

    @Override
    public void run() {
        if (!isCanceled()) {
            doWork();
        }
    }

    /**
     * Defines the task that will be executed. Won't be called if {@code cancel()} is already called.
     *
     * @see #cancel()
     */
    protected abstract void doWork();
}
