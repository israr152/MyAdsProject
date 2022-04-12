package com.isroid.app.studio.myadslibrary

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.isroid.app.studio.speak.translate.dictionary.translator.ads.InterstitialHelper
import org.jetbrains.annotations.NotNull

class AppOpenHandler(private val globalClass: Application,var appOpenId:String) : Application.ActivityLifecycleCallbacks,
    LifecycleObserver {
    private val log = "AppOpenManager"
    private var adVisible = false
    private var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var myApplication: Application? = globalClass
    private var fullScreenContentCallback: FullScreenContentCallback? = null

    init {
        this.myApplication?.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        fetchAd()
    }

    fun fetchAd() {
        if (isAdAvailable()) {
            return
        }

        val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    super.onAdLoaded(ad)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    // Handle the error.
                    super.onAdFailedToLoad(p0)
                }
            }
        val request: AdRequest = getAdRequest()
        AppOpenAd.load(
            globalClass.applicationContext,
            appOpenId,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            loadCallback
        )
    }

    private fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    adVisible = false
                    fetchAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
            adVisible = true
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            currentActivity?.let { appOpenAd?.show(it) }

        } else {
            Log.d(log, "Can not show ad.")
            fetchAd()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackgrounded() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
        currentActivity?.let {
            if(!InterstitialHelper.isInterstitialShowing){
                showAdIfAvailable()
            }
        }
    }

    /**
     * Creates and returns ad request.
     */
    @NotNull
    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {}
}