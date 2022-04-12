package com.isroid.app.studio.speak.translate.dictionary.translator.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.isroid.app.studio.speak.translate.dictionary.translator.utils.getInterstitialId

class InterstitialHelper {
    private var ad:InterstitialAd? = null

    companion object {
        var isInterstitialShowing = false

        //singleton
//        private var instance:InterstitialHelper?=null
//        fun getInstance():InterstitialHelper{
//            return instance?:InterstitialHelper().also {
//                instance = it
//            }
//        }
    }

    private var reTryCount:Int = 0
    fun loadInterstitialAd(c: Context){
        if(ad!=null){
            return
        }
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(c,c.getInterstitialId(),adRequest,object : InterstitialAdLoadCallback(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                ad = null
                reTryCount++
                if(reTryCount<=3){
                    loadInterstitialAd(c)
                }
            }

            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                ad = p0
            }
        })
    }

    fun showInterstitial(a:Activity,onClosedOrFailed:(()->Unit)?=null) {
        ad?.let {
            it.fullScreenContentCallback = object : FullScreenContentCallback(){
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    isInterstitialShowing = false
                    ad = null
                    loadInterstitialAd(a)
                    onClosedOrFailed?.invoke()
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    isInterstitialShowing = false
                    ad = null
                    loadInterstitialAd(a)
                    onClosedOrFailed?.invoke()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    isInterstitialShowing = true
                }
            }
            it.show(a)
        } ?: run {
            loadInterstitialAd(a)
            onClosedOrFailed?.invoke()
        }
    }
}