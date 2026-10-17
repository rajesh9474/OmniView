package com.example.core.ads

object AdConstants {
    // Official Google AdMob test IDs for Android development:
    // https://developers.google.com/admob/android/test-ads
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Default configuration: User can replace with their real AdMob Unit IDs from Google AdMob console
    var customBannerAdUnitId: String? = null
    var customInterstitialAdUnitId: String? = null

    val bannerAdUnitId: String
        get() = customBannerAdUnitId?.takeIf { it.isNotBlank() } ?: TEST_BANNER_AD_UNIT_ID

    val interstitialAdUnitId: String
        get() = customInterstitialAdUnitId?.takeIf { it.isNotBlank() } ?: TEST_INTERSTITIAL_AD_UNIT_ID

    val isUsingTestAds: Boolean
        get() = customBannerAdUnitId.isNullOrBlank() && customInterstitialAdUnitId.isNullOrBlank()
}
