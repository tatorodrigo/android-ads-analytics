package br.com.tattobr.android.adsanalytics;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;

public abstract class BaseApplication extends Application {
    public static FirebaseAnalytics analytics;
    private InterstitialAd mInterstitialAd;
    private String mLastAdUnitId;

    public abstract AdRequest createDefaultAdRequest();

    public abstract String getApplicationCode();

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        analytics = FirebaseAnalytics.getInstance(this);

        String applicationCode = getApplicationCode();
        if (applicationCode != null && !applicationCode.isEmpty()) {
            MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    onMobileAdsInitializationComplete(initializationStatus);
                }
            });
        }
    }

    public void setupInterstitialAd(String adUnitId) {
        if (adUnitId != null && !adUnitId.equals(mLastAdUnitId)) {
            mLastAdUnitId = adUnitId;
            requestNewInterstitial();
        }
    }

    protected void onMobileAdsInitializationComplete(InitializationStatus initializationStatus) {

    }

    private void clearInterstitialAd() {
        if(mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(null);
        }
        mInterstitialAd = null;
    }

    private void setupInterstitialAdContentCallback() {
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                BaseApplication.this.onAdFailedToShowFullScreenContent(adError);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                BaseApplication.this.onAdShowedFullScreenContent();
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                BaseApplication.this.onAdDismissedFullScreenContent();
                requestNewInterstitial();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestNewInterstitial() {
        clearInterstitialAd();

        AdRequest adRequest = createDefaultAdRequest();

        InterstitialAd.load(this, mLastAdUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        setupInterstitialAdContentCallback();
                        BaseApplication.this.onAdLoaded(interstitialAd);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        BaseApplication.this.onAdFailedToLoad(loadAdError);
                        clearInterstitialAd();
                    }
                });
    }

    protected void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

    }

    protected void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

    }

    protected void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
    }

    protected void onAdShowedFullScreenContent() {

    }

    protected void onAdDismissedFullScreenContent() {

    }

    public InterstitialAd getInterstitialAd() {
        return mInterstitialAd;
    }
}
