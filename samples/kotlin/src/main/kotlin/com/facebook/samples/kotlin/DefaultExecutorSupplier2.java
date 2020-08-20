/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.samples.kotlin;

import android.os.Process;

import com.facebook.imagepipeline.core.ExecutorSupplier;
import com.facebook.imagepipeline.core.PriorityThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Basic implementation of {@link ExecutorSupplier}.
 *
 * <p>Provides one thread pool for the CPU-bound operations and another thread pool for the IO-bound
 * operations.
 */
public class DefaultExecutorSupplier2 implements ExecutorSupplier {
  // Allows for simultaneous reads and writes.
  private static final int NUM_IO_BOUND_THREADS = 5;
  private static final int NUM_LIGHTWEIGHT_BACKGROUND_THREADS = 3;

  private final Executor mIoBoundExecutor;
  private final Executor mDecodeExecutor;
  private final Executor mBackgroundExecutor;
  private final Executor mLightWeightBackgroundExecutor;

  public DefaultExecutorSupplier2(int numCpuBoundThreads) {
    mIoBoundExecutor =
        Executors.newFixedThreadPool(
            NUM_IO_BOUND_THREADS*numCpuBoundThreads ,
            new PriorityThreadFactory(
                Process.THREAD_PRIORITY_BACKGROUND, "FrescoIoBoundExecutor", true));
    mDecodeExecutor =
        Executors.newFixedThreadPool(
            numCpuBoundThreads,
            new PriorityThreadFactory(
                Process.THREAD_PRIORITY_BACKGROUND * 10, "FrescoDecodeExecutor", true));
    mBackgroundExecutor =
        Executors.newFixedThreadPool(
            numCpuBoundThreads,
            new PriorityThreadFactory(
                Process.THREAD_PRIORITY_BACKGROUND, "FrescoBackgroundExecutor", true));
    mLightWeightBackgroundExecutor =
        Executors.newFixedThreadPool(
            NUM_LIGHTWEIGHT_BACKGROUND_THREADS*numCpuBoundThreads,
            new PriorityThreadFactory(
                Process.THREAD_PRIORITY_BACKGROUND, "FrescoLightWeightBackgroundExecutor", true));
  }

  @Override
  public Executor forLocalStorageRead() {
    return mIoBoundExecutor;
  }

  @Override
  public Executor forLocalStorageWrite() {
    return mIoBoundExecutor;
  }

  @Override
  public Executor forDecode() {
    return mDecodeExecutor;
  }

  @Override
  public Executor forBackgroundTasks() {
    return mBackgroundExecutor;
  }

  @Override
  public Executor forLightweightBackgroundTasks() {
    return mLightWeightBackgroundExecutor;
  }

  @Override
  public Executor forThumbnailProducer() {
    return mIoBoundExecutor;
  }
}
