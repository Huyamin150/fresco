/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.fresco.animation.bitmap.preparation;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.facebook.common.references.CloseableReference;
import com.facebook.fresco.animation.backend.AnimationBackend;
import com.facebook.fresco.animation.backend.AnimationInformation;
import com.facebook.fresco.animation.bitmap.BitmapAnimationBackend2;
import com.facebook.fresco.animation.bitmap.BitmapFrameRenderer;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;

import java.util.concurrent.ExecutorService;

/**
 * Default bitmap frame preparer that uses the given {@link ExecutorService} to schedule jobs. An
 * instance of this class can be shared between multiple animated images.
 */
public class DefaultBitmapFramePreparer2 implements BitmapFrameRender {

    private static final Class<?> TAG = DefaultBitmapFramePreparer2.class;

    private final PlatformBitmapFactory mPlatformBitmapFactory;
    private final BitmapFrameRenderer mBitmapFrameRenderer;
    private final Bitmap.Config mBitmapConfig;
    private final ExecutorService mExecutorService;
    private final SparseArray<Runnable> mPendingFrameDecodeJobs;

    private AnimationInformation mAnimationInformation;
    public DefaultBitmapFramePreparer2(
            PlatformBitmapFactory platformBitmapFactory,
            BitmapFrameRenderer bitmapFrameRenderer,
            Bitmap.Config bitmapConfig,
            ExecutorService executorService,
            AnimationInformation animationInformation) {
        mPlatformBitmapFactory = platformBitmapFactory;
        mBitmapFrameRenderer = bitmapFrameRenderer;
        mBitmapConfig = bitmapConfig;
        mExecutorService = executorService;
        mPendingFrameDecodeJobs = new SparseArray<>();
        mAnimationInformation = animationInformation;

    }


    @Override
    public void advance() {

    }



    @Override
    public void loadNextFrame(AnimationBackend bitmapBackend,long targetTime, BitmapAnimationBackend2.FrameListener readyListener) {

//        synchronized (mPendingFrameDecodeJobs) {
//            int frameId = getUniqueId(bitmapBackend, mCurrentFrameIndex);
////            Log.e("====","当前的对象是"+this.toString()+"frameNumber="+mCurrentFrameIndex+"frameId="+frameId);
//            // Check if already scheduled.
//            if (mPendingFrameDecodeJobs.get(frameId) != null) {
//                FLog.v(TAG, "Already scheduled decode job for frame %d", mCurrentFrameIndex);
//            }
//            Runnable frameDecodeRunnable =
//                    new FrameDecodeRunnable(bitmapBackend, mCurrentFrameIndex, frameId,readyListener);
////            mPendingFrameDecodeJobs.put(frameId, frameDecodeRunnable);
//            mExecutorService.execute(frameDecodeRunnable);
//        }
//        int frameId = getUniqueId(bitmapBackend, mCurrentFrameIndex);
//        Runnable frameDecodeRunnable = FrameDecodeRunnableFactory.createFrameDecodeRunnable(bitmapBackend,mCurrentFrameIndex,
//                frameId,readyListener,mBitmapConfig,mPlatformBitmapFactory,mBitmapFrameRenderer,targetTime);
////            mPendingFrameDecodeJobs.put(frameId, frameDecodeRunnable);
//        mExecutorService.execute(frameDecodeRunnable);
    }


    private static int getUniqueId(AnimationBackend backend, int frameNumber) {
        int result = backend.hashCode();
        result = 31 * result + frameNumber;
        return result;
    }

}
