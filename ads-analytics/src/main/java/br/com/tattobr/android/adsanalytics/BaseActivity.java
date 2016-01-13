package br.com.tattobr.android.adsanalytics;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public abstract class BaseActivity extends AppCompatActivity {
    private AdView mAdView;
    private boolean mShowInterstitialAd;
    private boolean mSetupAdsCalled;

    /**
     * The AdUnitId to be loaded or null to not load at all
     *
     * @return
     */
    public abstract String getIntersticialAdUnitId();

    public boolean showInterstitialAd(boolean persistOnResume) {
        BaseApplication application = (BaseApplication) getApplication();
        boolean isShowing = false;
        if (application != null) {
            InterstitialAd interstitialAd = application.getInterstitialAd();
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                mShowInterstitialAd = false;
                interstitialAd.show();
                isShowing = true;
            } else {
                mShowInterstitialAd = persistOnResume;
            }
        }
        return isShowing;
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
}
