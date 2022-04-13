package com.isroid.app.studio.myadsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.isroid.app.studio.myadslibrary.NativeAdHandler.loadAndShowNativeAd

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}