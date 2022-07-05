package com.pdfscanner.pdf.scanpdf.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.pdfscanner.pdf.scanpdf.R;

import java.util.Date;

import static androidx.lifecycle.Lifecycle.Event.ON_START;
import static com.pdfscanner.pdf.scanpdf.Util.Constant.SHOW_OPEN_ADS;

public class AppOpenAdsManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "AppOpenManager";
    //    public static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";     // Test ID
    public static String AD_UNIT_ID;
    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private final Application myApplication;
    private Activity currentActivity;
    private static boolean isShowingAd = false;
    private long loadTime = 0;

    public AppOpenAdsManager(Application myApplication) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public boolean getShowAds(Context context) {
        return true;
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        Log.e("ads", "open ads start: " + SHOW_OPEN_ADS);
        if (SHOW_OPEN_ADS) {
            if (getShowAds(currentActivity))
                if (currentActivity != null) {
                    showAdIfAvailable();
                } else {
                    fetchAd();
                }
        }
        Log.d(LOG_TAG, "onStart");
    }

    /**
     * Request an ad
     */
    public void fetchAd() {
        if (isAdAvailable()) {
            return;
        }

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAppOpenAdLoaded(AppOpenAd ad) {
                appOpenAd = ad;
                Log.d(LOG_TAG, " open ad loaded");
            }

            @Override
            public void onAppOpenAdFailedToLoad(LoadAdError loadAdError) {
                Log.d(LOG_TAG, loadAdError.getMessage());
            }

        };

        AdRequest request = getAdRequest();
        if (currentActivity != null) {
            AD_UNIT_ID = currentActivity.getResources().getString(R.string.app_open_id);
            AppOpenAd.load(
                    myApplication, AD_UNIT_ID, request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
        }
    }

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isAdAvailable() {
        return appOpenAd != null;
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    public void showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null;
                            isShowingAd = false;
                            fetchAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            Log.d(LOG_TAG, "Error display show ad." + adError.getMessage());
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            Log.d(LOG_TAG, String.valueOf(currentActivity));
            appOpenAd.show(currentActivity, fullScreenContentCallback);

        } else {
            Log.d(LOG_TAG, "Can not show ad.");
            fetchAd();
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Log.d(LOG_TAG, "onActivityCreated: ====> create");
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.d(LOG_TAG, "onActivityCreated: ====> started");
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Log.d(LOG_TAG, "onActivityCreated: ====> resume");
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.d(LOG_TAG, "onActivityCreated: ====> pause");
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.d(LOG_TAG, "onActivityCreated: ====> stopped");
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        Log.d(LOG_TAG, "onActivityCreated: ====> save");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.d(LOG_TAG, "onActivityCreated: ====> destroy");
        currentActivity = null;
    }
}
