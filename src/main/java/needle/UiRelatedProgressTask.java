package needle;

/**
 * Use for tasks where you first want to do some work (usually on a background thread) and then some UI-related work
 * on the UI/main thread based on the result of the preceding work.
 * <p/>
 * It is also possible to publish progress updates with {@code publishProgress()} inside {@code doWork()}
 * and handle them on the UI/main thread in {@code onProgressUpdate()}.
 * <p/>
 * If you don't need to publish progress updates use class UiRelatedTask instead.
 *
 * @param <Result>   Type of the result that is passed to thenDoUiRelatedWork() from doWork()
 * @param <Progress> Type of the progress that is passed to onProgressUpdate() from publishProgress()
 * @see needle.UiRelatedTask
 */
public abstract class UiRelatedProgressTask<Result, Progress> extends UiRelatedTask<Result> implements CancelableRunnable {

    /**
     * You can publish work progress and call it inside doWork().
     *
     * @param progress Progress that is passed to onProgressUpdate()
     * @see #onProgressUpdate(Object)
     */
    protected void publishProgress(final Progress progress) {
        if (!isCanceled()) {
            sUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onProgressUpdate(progress);
                }
            });
        }
    }

    /**
     * Handles progress update. It is executed on the UI/main thread. Won't be called if {@code cancel()} is already called.
     *
     * @param progress Progress that was published via publishProgress()
     * @see #publishProgress(Object)
     * @see #cancel()
     */
    protected abstract void onProgressUpdate(Progress progress);
}
