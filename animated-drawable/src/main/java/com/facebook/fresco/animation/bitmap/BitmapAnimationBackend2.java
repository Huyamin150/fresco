/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.fresco.animation.bitmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.IntRange;

import com.facebook.common.references.CloseableReference;
import com.facebook.fresco.animation.backend.AnimationBackend;
import com.facebook.fresco.animation.backend.AnimationBackendDelegateWithInactivityCheck;
import com.facebook.fresco.animation.backend.AnimationInformation;
import com.facebook.fresco.animation.bitmap.preparation.BitmapFrameRender;
import com.facebook.fresco.animation.bitmap.preparation.FrameDecodeRunnableFactory;
import com.facebook.fresco.animation.bitmap.preparation.GlideExecutor;
import com.facebook.fresco.animation.drawable.AnimatedDrawable2;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;

import java.lang.annotation.Retention;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Bitmap animation backend that renders bitmap frames.
 *
 * <p>The given {@link BitmapFrameCache} is used to cache frames and create new bitmaps. {@link
 * AnimationInformation} defines the main animation parameters, like frame and loop count. {@link
 * BitmapFrameRenderer} is used to render frames to the bitmaps aquired from the {@link
 * BitmapFrameCache}.
 */
public class BitmapAnimationBackend2
        implements AnimationBackend, AnimationBackendDelegateWithInactivityCheck.InactivityListener {

    public interface FrameListener {

        void onFrameReady(int frameNumber, CloseableReference<Bitmap> frame, long targetTime);

    }

    /**
     * Frame type that has been drawn. Can be used for logging.
     */
    @Retention(SOURCE)

    public @interface FrameType {
    }

    private static final Class<?> TAG = BitmapAnimationBackend2.class;
    private final AnimationInformation mAnimationInformation;
    private final BitmapFrameRenderer mBitmapFrameRenderer;
    private final PlatformBitmapFactory mPlatformBitmapFactory;
    @Nullable
//    private final BitmapFrameRender mBitmapFramePreparer;
    private final Paint mPaint;
    private final ExecutorService mExecutorService;
    @Nullable
    private Rect mBounds;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private int mCurrentFrameIndex = 0;
    @Nullable
    private CloseableReference<Bitmap> mCurrentFrame;
    public CloseableReference<Bitmap> mFirstFrame;
    @Nullable
    private AnimatedDrawable2.FrameCallBack mFrameListener;
    public volatile boolean isCanceled;

    static ArraySet<BitmapAnimationBackend2> arraySet = new ArraySet<>();

    public BitmapAnimationBackend2(
            PlatformBitmapFactory platformBitmapFactory, ExecutorService executorServiceForFramePreparing, @Nullable CloseableReference<Bitmap> firstFrame,
            AnimationInformation animationInformation,
            BitmapFrameRenderer bitmapFrameRenderer,
            @Nullable BitmapFrameRender bitmapFramePreparer) {
        mAnimationInformation = animationInformation;
        mCurrentFrame = firstFrame;
        mFirstFrame = firstFrame;
        mBitmapFrameRenderer = bitmapFrameRenderer;
//        mBitmapFramePreparer = bitmapFramePreparer;
        mPlatformBitmapFactory = platformBitmapFactory;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        updateBitmapDimensions();
        mExecutorService = executorServiceForFramePreparing;
        arraySet.add(this);
    }

    private BitmapAnimationBackend2.FrameListener readyListener = new BitmapAnimationBackend2.FrameListener() {
        @Override
        public void onFrameReady(int frameNumber, CloseableReference<Bitmap> currentFrame, long targetTime) {
            if (isCanceled) {
                CloseableReference.closeSafely(currentFrame);
                return;
            }
            Message message = handler.obtainMessage();
            message.obj = currentFrame;
            if (SystemClock.uptimeMillis() > targetTime){
                Log.e("===","时间差不对currentTime="+SystemClock.uptimeMillis()+"targetTime="+targetTime+"position="+BitmapAnimationBackend2.this.toString());
            }
            handler.sendMessageAtTime(message, targetTime);
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mCurrentFrame = (CloseableReference<Bitmap>) msg.obj;
            if (!mCurrentFrame.isValid()) mCurrentFrame = mFirstFrame;
            Log.e("===","onFrameReadyToDraw"+mCurrentFrameIndex+"CurrentTime="+SystemClock.uptimeMillis()+"position="+BitmapAnimationBackend2.this.toString());
            mFrameListener.onFrameReadyToDraw();
            if (!isCanceled) {
                advance();
                loadNextFrame();
            }else {
                CloseableReference.closeSafely(mCurrentFrame);
            }
        }
    };

    private void advance(){
      mCurrentFrameIndex = (mCurrentFrameIndex + 1) % mAnimationInformation.getFrameCount();
    }

    private long getNextFrameTime() {
        int duration = getFrameDurationMs(mCurrentFrameIndex);
        return SystemClock.uptimeMillis() + (long) duration;
    }


    public void setFrameListener(@Nullable AnimatedDrawable2.FrameCallBack frameListener) {
        mFrameListener = frameListener;
    }

    public CloseableReference<Bitmap> getFirst(){
        return mFirstFrame;
    }

    public void startLoadingFrame() {
        if (!isCanceled){
            loadNextFrame();
        }else {
            CloseableReference.closeSafely(mCurrentFrame);
        }
    }

    public void stopLoadingFrame() {
        isCanceled = true;
    }

    private void loadNextFrame(){
      int frameId = getUniqueId(mCurrentFrameIndex);
      long targetTime = getNextFrameTime();
      Runnable frameDecodeRunnable = FrameDecodeRunnableFactory.createFrameDecodeRunnable(this,mCurrentFrameIndex,
              frameId,readyListener, Bitmap.Config.RGB_565,mPlatformBitmapFactory,mBitmapFrameRenderer,targetTime);
//            mPendingFrameDecodeJobs.put(frameId, frameDecodeRunnable);
      mExecutorService.execute(frameDecodeRunnable);
        Log.e("===","准备执行"+SystemClock.uptimeMillis()+"targetTime="+targetTime+"index"+mCurrentFrameIndex+"position="+BitmapAnimationBackend2.this.toString());
//        Log.e("===","阻塞队列size"+((ThreadPoolExecutor) ((GlideExecutor) mExecutorService).delegate).getQueue().size());
    }


    @Override
    public int getFrameCount() {
        return mAnimationInformation.getFrameCount();
    }

    public CloseableReference<Bitmap> getCurrentFrame() {
        return mCurrentFrame;
    }

    @Override
    public int getFrameDurationMs(int frameNumber) {
        return mAnimationInformation.getFrameDurationMs(frameNumber);
    }

    @Override
    public int getLoopCount() {
        return mAnimationInformation.getLoopCount();
    }

    @Override
    public boolean drawFrame(Drawable parent, Canvas canvas, int frameNumber) {
        return true;
    }


    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public void setBounds(@Nullable Rect bounds) {
        mBounds = bounds;
        mBitmapFrameRenderer.setBounds(bounds);
        updateBitmapDimensions();
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }

    @Override
    public int getSizeInBytes() {
        return 0;
    }

    @Override
    public void clear() {
//    mBitmapFrameCache.clear();
    }

    @Override
    public void onInactive() {
        clear();
    }

    private void updateBitmapDimensions() {
        // Calculate the correct bitmap dimensions
        mBitmapWidth = mBitmapFrameRenderer.getIntrinsicWidth();
        if (mBitmapWidth == INTRINSIC_DIMENSION_UNSET) {
            mBitmapWidth = mBounds == null ? INTRINSIC_DIMENSION_UNSET : mBounds.width();
        }

        mBitmapHeight = mBitmapFrameRenderer.getIntrinsicHeight();
        if (mBitmapHeight == INTRINSIC_DIMENSION_UNSET) {
            mBitmapHeight = mBounds == null ? INTRINSIC_DIMENSION_UNSET : mBounds.height();
        }
    }


  private int getUniqueId(int frameNumber) {
    int result = this.hashCode();
    result = 31 * result + frameNumber;
    return result;
  }

}
