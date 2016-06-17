package br.com.tattobr.android.adsanalytics;

import android.app.Application;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

public abstract class BaseApplication extends Application {
    public static FirebaseAnalytics analytics;
    private InterstitialAd mInterstitialAd;
    private String mLastAdUnitId;

    public abstract AdRequest createDefaultAdRequest();

    public abstract String getApplicationCode();

    @Override
    public void onCreate() {
        super.onCreate();

        analytics = FirebaseAnalytics.getInstance(this);

        String applicationCode = getApplicationCode();
        if (applicationCode != null && !applicationCode.isEmpty()) {
            MobileAds.initialize(getApplicationContext(), applicationCode);
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

    private void requestNewInterstitial() {
        AdRequest adRequest = createDefaultAdRequest();
        mInterstitialAd.loadAd(adRequest);
    }

    public InterstitialAd getInterstitialAd() {
        return mInterstitialAd;
    }
}
