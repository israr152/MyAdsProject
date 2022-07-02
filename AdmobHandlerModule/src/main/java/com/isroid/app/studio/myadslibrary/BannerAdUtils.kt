package com.isroid.app.studio.myadslibrary

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

object BannerAdUtils {

    fun Context.loadAdaptiveBanner(container:FrameLayout,bannerId:String,internetConnected:Boolean, maxHeight:Int = 0){
        if(container.childCount>0){
            container.visibility = View.VISIBLE
        }else if(internetConnected){
            container.visibility = View.VISIBLE
            val adView = AdView(this)
            adView.adUnitId = bannerId
            container.addView(adView)
            startLoadingBanner(adView,maxHeight)
        }else{
            container.visibility = View.GONE
        }
    }

    private fun Context.startLoadingBanner(adView: AdView,maxHeight:Int) {
        val adRequest = AdRequest.Builder().build()
        val adSize: AdSize = getAdSize(maxHeight)
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize)
        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest)
    }

    private fun Context.getAdSize(maxHeight:Int): AdSize {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val outMetrics = resources.displayMetrics

        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density

        val adWidth =  (widthPixels / density).toInt()

        return if(maxHeight==0){
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this,adWidth)
        }else{
            AdSize.getInlineAdaptiveBannerAdSize(adWidth, maxHeight)
        }
    }
}