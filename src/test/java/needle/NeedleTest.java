package needle;

import android.os.Looper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NeedleTest {

    private static final String TASK_TYPE_1 = "video-downloading";
    private static final String TASK_TYPE_2 = "3d-rendering";
    private static final int CUSTOM_THREAD_POOL_SIZE_1 = 6;
    private static final int CUSTOM_THREAD_POOL_SIZE_2 = 9;

    @Test
    public void testExecutorCaching() {
        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially()).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially()).getExecutor());
        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread()).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread()).getExecutor());

        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially().withTaskType(TASK_TYPE_1)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially().withTaskType(TASK_TYPE_1)).getExecutor());
        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially().withTaskType(TASK_TYPE_2)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially().withTaskType(TASK_TYPE_2)).getExecutor());
        assertNotSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially().withTaskType(TASK_TYPE_1)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially().withTaskType(TASK_TYPE_2)).getExecutor());

        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().withTaskType(TASK_TYPE_1)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withTaskType(TASK_TYPE_1)).getExecutor());
        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().withTaskType(TASK_TYPE_2)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withTaskType(TASK_TYPE_2)).getExecutor());
        assertNotSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().withTaskType(TASK_TYPE_1)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withTaskType(TASK_TYPE_2)).getExecutor());

        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially()).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(1)).getExecutor());
        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().serially()).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(1).withTaskType(Needle.DEFAULT_TASK_TYPE)).getExecutor());

        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread()).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(Needle.DEFAULT_POOL_SIZE)).getExecutor());
        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread()).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(Needle.DEFAULT_POOL_SIZE).withTaskType(Needle.DEFAULT_TASK_TYPE)).getExecutor());
        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().withTaskType(TASK_TYPE_1)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(Needle.DEFAULT_POOL_SIZE).withTaskType(TASK_TYPE_1)).getExecutor());

        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(CUSTOM_THREAD_POOL_SIZE_1)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(CUSTOM_THREAD_POOL_SIZE_1)).getExecutor());
        assertSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(CUSTOM_THREAD_POOL_SIZE_1).withTaskType(TASK_TYPE_1)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(CUSTOM_THREAD_POOL_SIZE_1).withTaskType(TASK_TYPE_1)).getExecutor());
        assertNotSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(CUSTOM_THREAD_POOL_SIZE_1).withTaskType(TASK_TYPE_1)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(CUSTOM_THREAD_POOL_SIZE_1).withTaskType(TASK_TYPE_2)).getExecutor());
        assertNotSame(((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(CUSTOM_THREAD_POOL_SIZE_1).withTaskType(TASK_TYPE_1)).getExecutor(), ((Needle.ExecutorObtainer) Needle.onBackgroundThread().withThreadPoolSize(CUSTOM_THREAD_POOL_SIZE_2).withTaskType(TASK_TYPE_1)).getExecutor());
    }

    @Test
    public void testWorkOnBackgroundThread() {
        TestCancelableTask task = new TestCancelableTask();
        Needle.onBackgroundThread().execute(task);
        sleepABit();
        assertTrue(task.didRun());
        assertNotSame(Thread.currentThread(), task.getWorkThread());
    }

    @Test
    public void testWornOnMainThread() {
        TestCancelableTask task = new TestCancelableTask();
        Needle.onMainThread().execute(task);
        sleepABit();
        Robolectric.runUiThreadTasks();
        assertTrue(task.didRun());
        assertSame(Looper.getMainLooper().getThread(), task.getWorkThread());
    }

    @Test
    public void testCanceledWork() {
        TestCancelableTask task = new TestCancelableTask();
        task.cancel();
        Needle.onBackgroundThread().execute(task);
        sleepABit();
        assertTrue(task.isCanceled());
        assertFalse(task.didRun());
        assertNull(task.getWorkThread());
    }

    @Test
    public void testUiRelatedTask() {
        TestUiRelatedTask task = new TestUiRelatedTask();
        Needle.onBackgroundThread().execute(task);
        sleepABit();
        Robolectric.runUiThreadTasks();
        assertTrue(task.didRun());
        assertNotSame(Thread.currentThread(), task.getWorkThread());
        assertNotSame(task.getWorkThread(), task.getUiThread());
        assertSame(Looper.getMainLooper().getThread(), task.getUiThread());
    }

    @Test
    public void testTaskTypeSeparation() {
        Executor executor1 = Needle.onBackgroundThread().serially().withTaskType(TASK_TYPE_1);
        Executor executor2 = Needle.onBackgroundThread().serially().withTaskType(TASK_TYPE_2);

        List<TestCancelableTask> tasksNeedToBeExecutedSerially1 = new LinkedList<TestCancelableTask>();
        List<TestCancelableTask> tasksNeedToBeExecutedSerially2 = new LinkedList<TestCancelableTask>();
        for (int i = 0; i < 30; i++) {
            TestCancelableTask task1 = new TestCancelableTask();
            tasksNeedToBeExecutedSerially1.add(task1);
            executor1.execute(task1);

            TestCancelableTask task2 = new TestCancelableTask();
            tasksNeedToBeExecutedSerially2.add(task2);
            executor2.execute(task2);
        }
        sleepABit();

        TestCancelableTask ref1 = tasksNeedToBeExecutedSerially1.get(0);
        TestCancelableTask ref2 = tasksNeedToBeExecutedSerially2.get(0);
        assertNotSame(ref1.getWorkThread(), ref2.getWorkThread());
        for (TestCancelableTask task : tasksNeedToBeExecutedSerially1) {
            assertTrue(task.didRun());
            assertSame(ref1.getWorkThread(), task.getWorkThread());
            assertNotSame(Thread.currentThread(), task.getWorkThread());
        }
        for (TestCancelableTask task : tasksNeedToBeExecutedSerially2) {
            assertTrue(task.didRun());
            assertSame(ref2.getWorkThread(), task.getWorkThread());
            assertNotSame(Thread.currentThread(), task.getWorkThread());
        }
    }

    private void sleepABit() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}