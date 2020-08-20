package com.facebook.samples.kotlin;

import com.facebook.common.executors.ConstrainedExecutorService;
import com.facebook.common.executors.SerialExecutorService;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultSerialExecutorService2 extends ConstrainedExecutorService
    implements SerialExecutorService {

  public DefaultSerialExecutorService2(Executor executor) {
    // SerialExecutorService is just a ConstrainedExecutorService with a concurrency limit
    // of one and an unbounded work queue.
    super("SerialExecutor", 5, executor, new LinkedBlockingQueue<Runnable>());
  }

  /**
   * Synchronized override of {@link ConstrainedExecutorService#execute(Runnable)} to ensure that
   * view of memory is consistent between different threads executing tasks serially.
   *
   * @param runnable The task to be executed.
   */
  @Override
  public synchronized void execute(Runnable runnable) {
    super.execute(runnable);
  }
}
