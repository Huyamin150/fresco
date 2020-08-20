package com.facebook.fresco.animation.bitmap.preparation;

import android.graphics.Bitmap;

import com.facebook.fresco.animation.backend.AnimationBackend;
import com.facebook.fresco.animation.bitmap.BitmapAnimationBackend2;

/**
 * Created by Nick.Hu at 2020/8/9 11:28.
 * Mail:huyamin@theduapp.com
 * Tel: +8618521550285
 * Description: The Class BitmapFrameRender
 */
public interface BitmapFrameRender {

    void advance();

    void loadNextFrame(AnimationBackend bitmapBackend, long targetTime, BitmapAnimationBackend2.FrameListener readyListener);

}
