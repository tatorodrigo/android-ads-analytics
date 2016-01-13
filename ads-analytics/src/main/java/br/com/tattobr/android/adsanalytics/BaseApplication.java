package br.com.tattobr.android.adsanalytics;

import android.app.Application;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public abstract class BaseApplication extends Application {
    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    private InterstitialAd mInterstitialAd;
    private String mLastAdUnitId;

    protected abstract String getAnalyticsTrackingId();
    public abstract AdRequest createDefaultAdRequest();

    /**
     * Use to set analytics.setLocalDispatchPeriod().
     *
     * Defaults to 1800.
     *
     * @return
     */
    protected int getAnalyticsDispatchPeriodInSeconds() {
        return 1800;
    }

    /**
     * Use to set analytics.setDryRun()
     *
     * Defaults to false
     *
     * @return true: do not send analytics data. false: send analytics data
     */
    protected boolean getAnalyticsDryRun() {
        return false;
    }

    /**
     * Use to set tracker.enableExceptionReporting()
     *
     * @return
     */
    protected boolean getTrackerEnableExceptionReporting() {
        return true;
    }

    /**
     * Use to set tracker.enableAdvertisingIdCollection()
     * @return
     */
    protected boolean getTrackerEnableAdvertisingIdCollection() {
        return true;
    }

    /**
     * Use to set tracker.enableAutoActivityTracking()
     *
     * @return
     */
    protected boolean getTrackerEnableAutoActivityTracking() {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(getAnalyticsDispatchPeriodInSeconds());
        analytics.setDryRun(getAnalyticsDryRun());

        tracker = analytics.newTracker(getAnalyticsTrackingId());
        tracker.enableExceptionReporting(getTrackerEnableExceptionReporting());
        tracker.enableAdvertisingIdCollection(getTrackerEnableAdvertisingIdCollection());
        tracker.enableAutoActivityTracking(getTrackerEnableAutoActivityTracking());
    }

    public void setupInterstitialAd(String adUnitId) {
        if(adUnitId != null && !adUnitId.equals(mLastAdUnitId)) {
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
