package com.facebook.fresco.animation.bitmap.preparation;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.common.logging.FLog;
import com.facebook.common.references.CloseableReference;
import com.facebook.fresco.animation.backend.AnimationBackend;
import com.facebook.fresco.animation.bitmap.BitmapAnimationBackend2;
import com.facebook.fresco.animation.bitmap.BitmapFrameRenderer;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;

/**
 * Created by Nick.Hu at 2020/8/11 13:12.
 * Mail:huyamin@theduapp.com
 * Tel: +8618521550285
 * Description: The Class FrameDecodeRunnableFactory
 */
public class FrameDecodeRunnableFactory {
    private static long creationOrder = 0; //use to tag sequence of FrameDecodeRunnable


    public static Runnable createFrameDecodeRunnable(AnimationBackend animationBackend,
                                                     int frameNumber,
                                                     int hashCode, BitmapAnimationBackend2.FrameListener readyListener,
                                                     Bitmap.Config mBitmapConfig,
                                                     PlatformBitmapFactory mPlatformBitmapFactory,
                                                     BitmapFrameRenderer mBitmapFrameRenderer,
                                                     long targetTime
    ) {
        return new DefaultFrameDecodeRunnable(animationBackend, frameNumber, hashCode, readyListener, mBitmapConfig, mPlatformBitmapFactory, mBitmapFrameRenderer, creationOrder++,targetTime);

    }


    static class DefaultFrameDecodeRunnable implements Runnable, Comparable<DefaultFrameDecodeRunnable> {

        private final AnimationBackend mAnimationBackend;
        private final int mFrameNumber;
        private final int mHashCode;
        private final BitmapAnimationBackend2.FrameListener mReadyListener;
        private final long mOrder;
        private final PlatformBitmapFactory mPlatformBitmapFactory;
        private final BitmapFrameRenderer mBitmapFrameRenderer;
        private final Bitmap.Config mBitmapConfig;
        private final String TAG = DefaultFrameDecodeRunnable.class.getSimpleName();
        private final long mTargetTime;

        public DefaultFrameDecodeRunnable(
                AnimationBackend animationBackend,
                int frameNumber,
                int hashCode, BitmapAnimationBackend2.FrameListener readyListener, Bitmap.Config bitmapConfig,
                PlatformBitmapFactory platformBitmapFactory,
                BitmapFrameRenderer bitmapFrameRenderer, long order,
                long targetTime) {
            mAnimationBackend = animationBackend;
            mFrameNumber = frameNumber;
            mHashCode = hashCode;
            mReadyListener = readyListener;
            mOrder = order;
            mPlatformBitmapFactory = platformBitmapFactory;
            mBitmapConfig = bitmapConfig;
            mBitmapFrameRenderer = bitmapFrameRenderer;
            mTargetTime = targetTime;
        }

        public long getOrder(){
            return mOrder;
        }

        @Override
        public void run() {
                if (((BitmapAnimationBackend2)mAnimationBackend).isCanceled) return;
                // Prepare the frame.
                long time = System.currentTimeMillis();
                if (prepareFrame(mFrameNumber)) {
                    FLog.v(TAG, "Prepared frame frame %d.", mFrameNumber);
                } else {
                    FLog.e(TAG, "Could not prepare frame %d.", mFrameNumber);
                }


        }


        private boolean prepareFrame(
                int frameNumber) {
            CloseableReference<Bitmap> bitmapReference = null;
            boolean created;
            try {
                bitmapReference =
                        mPlatformBitmapFactory.createBitmap(
                                mAnimationBackend.getIntrinsicWidth(),
                                mAnimationBackend.getIntrinsicHeight(),
                                mBitmapConfig);
                Log.e("===","正在执行"+mAnimationBackend.toString()+"currentTime"+ SystemClock.uptimeMillis()+"targetTime"+mTargetTime);
                created = mBitmapFrameRenderer.renderFrame(frameNumber, bitmapReference.get());
                Log.e("===","执行完毕"+mAnimationBackend.toString()+"currentTime"+ SystemClock.uptimeMillis()+"targetTime"+mTargetTime+"本次耗时="+(System.currentTimeMillis() - time));
                if (created)
                    mReadyListener.onFrameReady(frameNumber, bitmapReference,mTargetTime); //callback
            } catch (RuntimeException e) {
                FLog.w(TAG, "Failed to create frame bitmap", e);
                return false;
            }
            return created;
        }

        @Override
        public int compareTo(DefaultFrameDecodeRunnable o) {
            return (int) (mOrder - o.mOrder);
        }
    }
}
