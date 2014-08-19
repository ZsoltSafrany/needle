package needle;

import static org.junit.Assert.assertEquals;

public class TestUiRelatedTask extends UiRelatedTask<Integer> {

    private static final Integer RESULT = 42;
    private boolean mDidRun;
    private Thread mWorkThread;
    private Thread mUiThread;

    @Override
    protected Integer doWork() {
        mWorkThread = Thread.currentThread();
        return RESULT;
    }

    @Override
    protected void thenDoUiRelatedWork(Integer result) {
        assertEquals(RESULT, result);
        mUiThread = Thread.currentThread();
        mDidRun = true;
    }

    public boolean didRun() {
        return mDidRun;
    }

    public Thread getWorkThread() {
        return mWorkThread;
    }

    public Thread getUiThread() {
        return mUiThread;
    }
}
