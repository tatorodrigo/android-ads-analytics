package br.com.tattobr.android.adsanalytics;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;

public abstract class BaseActivity extends AppCompatActivity {
    private final String SI_SHOW_INTERSTITIAL_AD = "br.com.tattobr.android.adsanalytics.SI_SHOW_INTERSTITIAL_AD";
    private final String LAST_INTERSTITIAL_AD_MILIS = "br.com.tattobr.android.adsanalytics.LAST_INTERSTITIAL_AD_MILIS";
    private ViewGroup adContainerView;
    private AdView mAdView;
    private boolean mShowInterstitialAd;
    private boolean mSetupAdsCalled;

    private SharedPreferences mSharedPreferences;

    /**
     * The AdUnitId to be loaded or null to not load at all
     *
     * @return
     */
    public abstract String getIntersticialAdUnitId();

    /**
     * The AdUnitId to be loaded or null to not load at all
     *
     * @return
     */
    public abstract String getBannerAdUnitId();

    protected abstract long getInterstitialAdMilisInterval();

    public boolean showInterstitialAd(boolean persistOnResume) {
        BaseApplication application = (BaseApplication) getApplication();
        boolean isShowing = false;
        if (application != null) {
            InterstitialAd interstitialAd = application.getInterstitialAd();
            if (isInterstitialAdAllowedByTime() && interstitialAd != null && interstitialAd.isLoaded()) {
                mShowInterstitialAd = false;
                mSharedPreferences.edit().putLong(
                        LAST_INTERSTITIAL_AD_MILIS, System.currentTimeMillis()
                ).apply();
                interstitialAd.show();
                isShowing = true;
            } else {
                mShowInterstitialAd = persistOnResume;
            }
        }
        return isShowing;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mShowInterstitialAd = savedInstanceState != null && savedInstanceState.getBoolean(SI_SHOW_INTERSTITIAL_AD);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SI_SHOW_INTERSTITIAL_AD, mShowInterstitialAd);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mSetupAdsCalled) {
            throw new IllegalStateException("must call setupAds on onCreate.");
        }

        if (mAdView != null) {
            mAdView.resume();
        }

        if (mShowInterstitialAd) {
            showInterstitialAd(true);
        }
    }

    @SuppressLint("MissingPermission")
    protected final void setupAds() {
        if (!mSetupAdsCalled) {
            mSetupAdsCalled = true;
            adContainerView = findViewById(R.id.ad_view_container);

            if (adContainerView != null) {
                adContainerView.post(new Runnable() {
                    @Override
                    public void run() {
                        loadBanner();
                    }
                });
            }

            String adUnitId = getIntersticialAdUnitId();
            if (adUnitId != null) {
                BaseApplication application = (BaseApplication) getApplication();
                if (application != null) {
                    application.setupInterstitialAd(adUnitId);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void loadBanner() {
        String adUnitId = getBannerAdUnitId();
        if (adUnitId != null && !adUnitId.isEmpty()) {
            // Create an ad request.
            mAdView = new AdView(this);
            mAdView.setAdUnitId(adUnitId);
            adContainerView.removeAllViews();
            adContainerView.addView(mAdView);

            AdSize adSize = getAdSize();
            mAdView.setAdSize(adSize);

            AdRequest adRequest = createDefaultAdRequest();

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    // old loadAdError.getCode() == AdRequest.ERROR_CODE_NETWORK_ERROR
                    mAdView.setVisibility(View.GONE);
                    adContainerView.setVisibility(View.GONE);
                }
            });

            mAdView.loadAd(adRequest);
        } else {
            adContainerView.setVisibility(View.GONE);
        }
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private AdRequest createDefaultAdRequest() {
        BaseApplication application = (BaseApplication) getApplication();
        return application != null ?
                application.createDefaultAdRequest() :
                new AdRequest.Builder().build();
    }

    private boolean isInterstitialAdAllowedByTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long lastInterstitialAdDate = mSharedPreferences.getLong(LAST_INTERSTITIAL_AD_MILIS, 0l);
        return lastInterstitialAdDate == 0l || lastInterstitialAdDate > currentTimeMillis ||
                currentTimeMillis - lastInterstitialAdDate > getInterstitialAdMilisInterval();
    }
}
