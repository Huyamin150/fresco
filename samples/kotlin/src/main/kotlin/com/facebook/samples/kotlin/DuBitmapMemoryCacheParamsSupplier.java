package com.facebook.samples.kotlin;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.imagepipeline.cache.MemoryCacheParams;

import java.util.concurrent.TimeUnit;

public class DuBitmapMemoryCacheParamsSupplier implements Supplier<MemoryCacheParams> {
    private static final int MAX_CACHE_ENTRIES = 256;
    private static final int MAX_EVICTION_QUEUE_SIZE = Integer.MAX_VALUE;
    private static final int MAX_EVICTION_QUEUE_ENTRIES = Integer.MAX_VALUE;
    private static final int MAX_CACHE_ENTRY_SIZE = Integer.MAX_VALUE;
    private static final long PARAMS_CHECK_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5);

    private final Context context;

    public DuBitmapMemoryCacheParamsSupplier(Context context) {
        this.context = context;
    }

    @Override
    public MemoryCacheParams get() {
        return new MemoryCacheParams(
                getMaxMemoryCacheSize((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 56 : MAX_CACHE_ENTRIES,
                MAX_EVICTION_QUEUE_SIZE,
                MAX_EVICTION_QUEUE_ENTRIES,
                MAX_CACHE_ENTRY_SIZE,
                PARAMS_CHECK_INTERVAL_MS);
    }

    private int getMaxMemoryCacheSize(ActivityManager mActivityManager) {
        int maxMemory = Math.min(mActivityManager.getMemoryClass() * ByteConstants.MB, Integer.MAX_VALUE);
         if (maxMemory < 32 * ByteConstants.MB) {
             return  4 * ByteConstants.MB;
        } else if (maxMemory < 64 * ByteConstants.MB) {
             return 6 * ByteConstants.MB;
        } else {
            // We don't want to use more ashmem on Gingerbread for now, since it doesn't respond well to
            // native memory pressure (doesn't throw exceptions, crashes app, crashes phone)
             return  maxMemory / 2;
        }
    }
}
