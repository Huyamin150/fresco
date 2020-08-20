package com.facebook.samples.kotlin

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Nick.Hu at 2020/8/18 11:52.
 * Mail:huyamin@theduapp.com
 * Tel: +8618521550285
 * Description: The Class EmptyActivity
 */
public class EmptyActivity :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this))
    }
}
