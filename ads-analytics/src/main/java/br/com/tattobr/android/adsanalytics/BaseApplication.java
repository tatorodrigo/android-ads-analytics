package br.com.tattobr.android.adsanalytics;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.RequiresPermission;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
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
                }
            });
        }
    }

    public void setupInterstitialAd(String adUnitId) {
        if (adUnitId != null && !adUnitId.equals(mLastAdUnitId)) {
            mLastAdUnitId = adUnitId;
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(adUnitId);

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });

            requestNewInterstitial();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewInterstitial() {
        AdRequest adRequest = createDefaultAdRequest();
        mInterstitialAd.loadAd(adRequest);
    }

    public InterstitialAd getInterstitialAd() {
        return mInterstitialAd;
    }
}
