package com.isroid.app.studio.myadslibrary

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

object NativeAdHandler {

    fun Activity.loadAndShowNativeAd(
        type:String = "large",
        layoutId: Int?=null,
        shimmerLayoutId:Int?=null,
        container: FrameLayout,
        adId:String,
        onLoad:((NativeAd)->Unit)?=null
    ) {
        val shimmerId = shimmerLayoutId ?:run{
            if(type=="large"){
                R.layout.shimmer_native_ad_layout_large
            }else{
                R.layout.shimmer_native_ad_small
            }
        }
        val adLayoutId = layoutId ?: run {
            if(type=="large"){
                R.layout.native_ad_layout_large
            }else{
                R.layout.native_ad_layout_small
            }
        }

        showNativeAdShimmer(shimmerId,container)

        loadNativeAd(adId,onLoad = {
            onLoad?.invoke(it)
            setNativeAd(container,adLayoutId,it)
        }){
            container.removeAllViews()
            container.visibility = View.GONE
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

    fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val mediaView: MediaView = adView.findViewById(R.id.ad_media)
        adView.mediaView = mediaView
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.bodyView = adView.findViewById(R.id.ad_advertiser)
        adView.starRatingView = adView.findViewById(R.id.rating_bar)

//        adView.mediaView?.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        if (adView.mediaView != null) {
            adView.mediaView?.visibility = View.VISIBLE
        } else {
            adView.mediaView?.visibility = View.GONE
        }

        if (nativeAd.headline != null) (adView.headlineView as TextView).text = nativeAd.headline
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.GONE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }
        if (nativeAd.icon != null) {
            adView.iconView?.visibility = View.VISIBLE
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        } else adView.iconView?.visibility = View.GONE
        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.GONE
        }
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.GONE
        } else {
            (adView.bodyView as TextView).text = nativeAd.body
            adView.bodyView?.visibility = View.VISIBLE
        }

        if(nativeAd.starRating == null){
            adView.starRatingView?.visibility = View.GONE
        }else{
            (adView.starRatingView as RatingBar?)?.progress = nativeAd.starRating?.toInt() ?: 0
            adView.starRatingView?.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }

    private fun Activity.showNativeAdShimmer(shimmerLayoutId:Int, container:FrameLayout){
        val shimmerFrameLayout = layoutInflater.inflate(shimmerLayoutId,null,false) as ShimmerFrameLayout
        container.removeAllViews()
        container.addView(shimmerFrameLayout)
        shimmerFrameLayout.startShimmer()
    }
}
