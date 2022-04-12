package com.isroid.app.studio.myadslibrary

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.isroid.app.studio.myadsproject.gone
import com.isroid.app.studio.myadsproject.show

object NativeAdHandler {

    fun Activity.loadAndShowNativeAd(
        container: FrameLayout,
        layoutId: Int,
        shimmerLayoutId:Int,
        adId:String,
        onLoad:((NativeAd)->Unit)?=null
    ) {
        showShimmer(shimmerLayoutId,container)

        loadNativeAd(adId,onLoad = {
            onLoad?.invoke(it)
            setNativeAd(container,layoutId,it)
        }){
            container.removeAllViews()
            container.gone()
        }
    }

    fun Activity.loadNativeAd(adId: String, onLoad:((NativeAd)->Unit), onFail:(()->Unit)?=null){
        val builder = AdLoader.Builder(this, adId)
        builder.forNativeAd { na: NativeAd ->
            onLoad.invoke(na)
        }
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .setVideoOptions(videoOptions).build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    showLog("onFailedToLoadNativeAd: ${loadAdError.message}")
                    onFail?.invoke()
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun Activity.setNativeAd(container: FrameLayout,layoutId: Int,nativeAd: NativeAd){
        val adView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                layoutId,
                null
            ) as NativeAdView
        populateUnifiedNativeAdView(nativeAd, adView)
        container.removeAllViews()
        container.addView(adView)
    }

    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val mediaView: MediaView = adView.findViewById(R.id.ad_media)
        adView.mediaView = mediaView
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.bodyView = adView.findViewById(R.id.ad_advertiser)
        adView.starRatingView = adView.findViewById(R.id.rating_bar)

//        adView.mediaView?.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        if (adView.mediaView != null) {
            adView.mediaView?.show()
        } else {
            adView.mediaView?.gone()
        }

        if (nativeAd.headline != null) (adView.headlineView as TextView).text = nativeAd.headline
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.gone()
        } else {
            adView.callToActionView?.show()
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }
        if (nativeAd.icon != null) {
            adView.iconView?.show()
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        } else adView.iconView?.gone()
        if (nativeAd.starRating == null) {
            adView.starRatingView?.gone()
        }
        if (nativeAd.body == null) {
            adView.bodyView?.gone()
        } else {
            (adView.bodyView as TextView).text = nativeAd.body
            adView.bodyView?.show()
        }

        if(nativeAd.starRating == null){
            adView.starRatingView?.gone()
        }else{
            (adView.starRatingView as RatingBar?)?.progress = nativeAd.starRating?.toInt() ?: 0
            adView.starRatingView?.show()
        }

        adView.setNativeAd(nativeAd)
    }

    private fun Activity.showShimmer(shimmerLayoutId:Int,container:FrameLayout){
        val shimmerFrameLayout = layoutInflater.inflate(shimmerLayoutId,null,false) as ShimmerFrameLayout
        container.removeAllViews()
        container.addView(shimmerFrameLayout)
        shimmerFrameLayout.startShimmer()
    }
}
