package com.github.eric.tcp;

import java.util.concurrent.*;

/**
 * Created by eric567 on 8/18/2016.
 */
public class ThreadPool {

    private static Executor threadPoolExecutor;


    private ThreadPool() {

    }


    public static ThreadPool getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Singleton implementation helper.
     */
    private static class SingletonHolder {
        // This gets initialized on first reference
        // to SingletonHolder.
        static final ThreadPool instance = new ThreadPool();
    }

    public void execute(Runnable command) {
        threadPoolExecutor.execute(command);
    }

    public static void newFixedThreadPool(int cores) {
        System.out.println("cpu cores is " + cores);

        threadPoolExecutor = Executors.newFixedThreadPool(cores);
    }

    public Executor getExecutor() {
        return threadPoolExecutor;
    }

}
