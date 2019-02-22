package com.android.paul.myapplication;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author puzhao
 */
public abstract class MyAsyncTask<Result> {
    private ThreadFactory innerThreadFactory = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "thread with name " + count.getAndIncrement());
        }
    };

    private ExecutorService innerExecutors = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<Runnable>(), innerThreadFactory);
    private AbstractWorkerRunnable workerRunnable;
    private FutureTask futureTask;
    private Handler mainHandler;
    private final static int ON_TASK_FINISH = 10001;

    public MyAsyncTask() {
        mainHandler = new InnerHandler(Looper.getMainLooper());

        workerRunnable = new AbstractWorkerRunnable() {
            @Override
            public Object call() throws Exception {
                return doInBackground();
            }
        };

        futureTask = new FutureTask(workerRunnable){
            @Override
            protected void done() {
                super.done();
                Message msg = Message.obtain(mainHandler, ON_TASK_FINISH, MyAsyncTask.this);
                msg.sendToTarget();
            }
        };
    }

    @MainThread
    public abstract void preExecute();

    public abstract Result doInBackground();

    @MainThread
    public abstract void onPostExecute();

    public MyAsyncTask execute(){
        return executeOnExecutor();
    }

    public MyAsyncTask executeOnExecutor(){
        preExecute();
        innerExecutors.execute(futureTask);
        return this;
    }

    private void finish(){
        onPostExecute();
    }

    public static abstract class AbstractWorkerRunnable implements Callable{

    }

    public static class InnerHandler extends Handler{
        public InnerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ON_TASK_FINISH:
                    MyAsyncTask asyncTask = (MyAsyncTask) msg.obj;
                    asyncTask.finish();
                default:
            }
        }
    }

}

