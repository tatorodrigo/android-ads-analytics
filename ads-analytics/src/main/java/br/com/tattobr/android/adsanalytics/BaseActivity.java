package br.com.tattobr.android.adsanalytics;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public abstract class BaseActivity extends AppCompatActivity {
    private final String SI_SHOW_INTERSTITIAL_AD = "br.com.tattobr.android.adsanalytics.SI_SHOW_INTERSTITIAL_AD";
    private final String LAST_INTERSTITIAL_AD_MILIS = "br.com.tattobr.android.adsanalytics.LAST_INTERSTITIAL_AD_MILIS";
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
        super.onPause();

        if (mAdView != null) {
            mAdView.pause();
        }
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

    protected final void setupAds() {
        if (!mSetupAdsCalled) {
            mSetupAdsCalled = true;
            mAdView = (AdView) findViewById(R.id.adView);
            if (mAdView != null) {
                AdRequest adRequest = createDefaultAdRequest();

                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        super.onAdFailedToLoad(errorCode);

                        if (errorCode == AdRequest.ERROR_CODE_NETWORK_ERROR) {
                            mAdView.setVisibility(View.GONE);
                        }
                    }
                });
                mAdView.loadAd(adRequest);
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

    private AdRequest createDefaultAdRequest() {
        BaseApplication application = (BaseApplication) getApplication();
        return application != null ? application.createDefaultAdRequest() : null;
    }

    private boolean isInterstitialAdAllowedByTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long lastInterstitialAdDate = mSharedPreferences.getLong(LAST_INTERSTITIAL_AD_MILIS, 0l);
        return lastInterstitialAdDate == 0l || lastInterstitialAdDate > currentTimeMillis ||
                currentTimeMillis - lastInterstitialAdDate > getInterstitialAdMilisInterval();
    }
}
