package com.isroid.app.studio.myadslibrary

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import org.jetbrains.annotations.NotNull

class AppOpenHandler(
    private var thisApp: Application,
    private var appOpenId: String,
    private var appStateListener: AppStateCallback? = null,
) : Application.ActivityLifecycleCallbacks,
    LifecycleEventObserver {

    private var adVisible = false
    private var appOpenAd: AppOpenAd? = null

    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var fullScreenContentCallback: FullScreenContentCallback? = null

    init {
        this.thisApp.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private var blackListActivities: MutableList<Activity>? = null
    fun setBlacklistActivity(activity: Activity) {
        if (blackListActivities == null) {
            blackListActivities = mutableListOf()
        }
        blackListActivities?.add(activity)
    }

    /**
     * Request an ad
     */
    fun fetchAd() {
        appStateListener?.showLog("fetchAd " + isAdAvailable())
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return
        }

        /*
          Called when an app open ad has failed to load.

          @param loadAdError the error.
         */
        // Handle the error.
        val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
            object : AppOpenAd.AppOpenAdLoadCallback() {
                /**
                 * Called when an app open ad has loaded.
                 *
                 * @param ad the loaded app open ad.
                 */

                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    appStateListener?.showLog("onAdLoaded")
                }

                /**
                 * Called when an app open ad has failed to load.
                 *
                 * @param loadAdError the error.
                 */
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error.
                    appStateListener?.showLog("onAdFailedToLoad ${loadAdError.message}")
                }
            }
        val request: AdRequest = getAdRequest()
        AppOpenAd.load(
            thisApp, appOpenId, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
        )
    }

    private fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.

        //AppOpen
        if (!isShowingAd && isAdAvailable() && !InterstitialHandler.isInterstitialShowing) {
            appStateListener?.showLog("Will show ad.")
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    adVisible = false
                    fetchAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appStateListener?.showLog("failed to show open ad ${adError.message}")
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }

            adVisible = true
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            currentActivity?.let { appOpenAd?.show(it) }

        } else {
            appStateListener?.showLog("Can not show ad.")
            fetchAd()
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

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onStateChanged(p0: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                appStateListener?.onAppStart()
                if (currentActivity != null && blackListActivities?.contains(currentActivity) != true) {
                    Looper.getMainLooper()?.let { looper ->
                        Handler(looper).postDelayed({
                            showAdIfAvailable()
                        }, 250)
                    }
                }
            }
            Lifecycle.Event.ON_PAUSE -> {
                appStateListener?.onAppPause()
            }
            Lifecycle.Event.ON_RESUME -> {
                appStateListener?.onAppResume()
            }
            Lifecycle.Event.ON_STOP -> {
                appStateListener?.onAppStop()
            }
            Lifecycle.Event.ON_DESTROY -> {
                appStateListener?.onAppDestroy()
            }
            Lifecycle.Event.ON_ANY -> {
                appStateListener?.showLog("App onAny")
            }
            else -> {
            }
        }
    }

}