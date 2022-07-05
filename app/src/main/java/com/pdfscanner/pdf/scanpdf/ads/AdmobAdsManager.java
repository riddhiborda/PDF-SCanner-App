package com.pdfscanner.pdf.scanpdf.ads;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;

public class AdmobAdsManager {

    private final String TAG = this.getClass().getSimpleName();
    private static AdmobAdsManager singleton;
    public static InterstitialAd interstitialAd;
    public boolean isAdLoad;
    public boolean isAdLoadProcessing;
    public boolean isAdLoadFailed;

    private ProgressDialog progressDialog;

    public AdmobAdsManager(Context context) {
        isAdLoad = false;
        isAdLoadProcessing = false;
        isAdLoadFailed = false;
        setUpProgress(context);
    }

    public static AdmobAdsManager getInstance(Context context) {
        if (singleton == null) {
            singleton = new AdmobAdsManager(context);
        } else {
            singleton.setUpProgress(context);
        }

        return singleton;
    }

    private void setUpProgress(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Ad Showing...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void showProgress(Activity activity) {
        activity.runOnUiThread(() -> {
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }
        });
    }

    public void dismissProgress(Activity activity) {
        activity.runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    public boolean getShowAds() {
        return true;
    }

    public void loadInterstitialAd(Context context, String interstitialAdID) {

        if (getShowAds())
            if (interstitialAd == null && !isAdLoadProcessing) {
                isAdLoadProcessing = true;
                AdRequest adRequest = new AdRequest.Builder().build();
                InterstitialAd.load(context, interstitialAdID, adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAds) {
                        interstitialAd = interstitialAds;
                        isAdLoad = true;
                        isAdLoadFailed = false;
                        isAdLoadProcessing = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        isAdLoad = false;
                        isAdLoadFailed = true;
                        isAdLoadProcessing = false;
                        Log.e(TAG, "InterstitialAd fail code: " + loadAdError.getCode() + " Message: " + loadAdError.getMessage());
                    }
                });
            }
    }

    public InterstitialAd getInterstitialAd() {
        if (interstitialAd == null) {
            return null;
        }
        return interstitialAd;
    }

    public void loadInterstitialAd(Activity context, String interstitialAdID, int number, OnAdClosedListener onAdClosedListener) {
        if (getShowAds()) {
            if (interstitialAd != null) {
                if (isAdLoad && !isAdLoadFailed && !isAdLoadProcessing) {
                    Constant.SHOW_OPEN_ADS = false;
                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            interstitialAd = null;
                            isAdLoad = false;
                            isAdLoadProcessing = false;
                            isAdLoadFailed = false;
                            Constant.showOpenAd();
                            Constant.SHOW_OPEN_ADS = true;
                            onAdClosedListener.onAdClosed(false);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            isAdLoad = false;
                            isAdLoadProcessing = false;
                            isAdLoadFailed = false;
                            interstitialAd = null;
                            Constant.SHOW_OPEN_ADS = true;
                            Constant.showOpenAd();
                            loadInterstitialAd(context, interstitialAdID);
                            onAdClosedListener.onAdClosed(true);
                        }
                    });
                    Constant.hideOpenAd();
                    interstitialAd.show(context);

                } else {
                    Log.e("Ads", "Ads still loading!");
                    onAdClosedListener.onAdClosed(false);
                }
            } else {
                if (!TextUtils.isEmpty(interstitialAdID)) {
                    loadInterstitialAd(context, interstitialAdID);
                }
                onAdClosedListener.onAdClosed(false);
            }
        } else {
            onAdClosedListener.onAdClosed(false);
        }
    }

    public interface OnAdClosedListener {
        void onAdClosed(Boolean isShowADs);
    }

    public void LoadBanner(Context context, RelativeLayout adContainerView, String bannerAdID, final AdsEventListener adEventListener) {
        if (getShowAds()) {
            try {
                if (isNetworkAvailable(context)) {
                    AdView adView = new AdView(context);
                    adView.setAdSize(AdSize.SMART_BANNER);
                    adView.setAdUnitId(bannerAdID);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            if (adEventListener != null) {
                                adEventListener.onAdLoaded(null);
                            }
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            if (adEventListener != null) {
                                adEventListener.onAdClosed();
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            Log.e(TAG, "onAdFailedToLoadBanner: " + loadAdError.getMessage());
                            if (adEventListener != null) {
                                adEventListener.onLoadError(loadAdError.getMessage());
                            }
                        }
                    });
                    adView.loadAd(adRequest);
                    adContainerView.addView(adView);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "LoadBanner: " + e.toString());
            }
        }
    }

    public static boolean isNetworkAvailable(Context c) {
        try {
            if (c != null) {
                ConnectivityManager manager = (ConnectivityManager)
                        c.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                boolean isAvailable = false;
                if (networkInfo != null && networkInfo.isConnected()) {
                    isAvailable = true;
                }
                return isAvailable;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public void LoadAdaptiveBanner(Context context, RelativeLayout adContainerView, String bannerAdID, final AdsEventListener adsEventListener) {
        if (getShowAds()) {
            try {

                if (isNetworkAvailable(context)) {
                    // Create an ad request. Check your logcat output for the hashed device ID to
                    // get test ads on a physical device. e.g.
                    // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
                    AdView adView = new AdView(context);
                    adView.setAdUnitId(bannerAdID);
                    adContainerView.removeAllViews();
                    adContainerView.addView(adView);

                    final AdSize adSize = getAdSize(context, adContainerView);
                    adView.setAdSize(adSize);

                    AdRequest adRequest =
                            new AdRequest.Builder().build();
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            if (adsEventListener != null) {
                                adsEventListener.onAdLoaded(null);
                            }
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();

                            if (adsEventListener != null) {
                                adsEventListener.onAdClosed();
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            if (adsEventListener != null) {
                                adsEventListener.onLoadError(loadAdError.getMessage());
                            }
                            Log.e(TAG, "onAdFailedAdaptiveBanner: " + loadAdError.toString());
                        }
                    });

                    // Start loading the ad in the background.
                    adView.loadAd(adRequest);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "LoadAdaptiveBanner: " + e.toString());
            }
        } else {
            if (adsEventListener != null) {
                adsEventListener.onLoadError("");
            }
        }
    }

    public AdSize getAdSize(Context context, RelativeLayout adContainerView) {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        //  return AdSize.getCurrentOrientationBannerAdSizeWithWidth(context, adWidth);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    public void LoadNativeAd(final Context context, String nativeAdID, final AdsEventListener adsEventListener) {
        if (getShowAds()) {
            AdLoader.Builder builder = new AdLoader.Builder(context, nativeAdID);

            builder.forNativeAd(unifiedNativeAd -> {
                if (adsEventListener != null) {
                    adsEventListener.onAdLoaded(unifiedNativeAd);
                }

            }).withAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    if (adsEventListener != null) {
                        adsEventListener.onLoadError(loadAdError.getMessage());
                    }
                    Log.e(TAG, "onAdFailedToLoadNative:" + loadAdError.getCode());
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                }
            });
            VideoOptions videoOptions = new VideoOptions.Builder()
                    .setStartMuted(true)
                    .build();
            com.google.android.gms.ads.nativead.NativeAdOptions adOptions = new com.google.android.gms.ads.nativead.NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
            builder.withNativeAdOptions(adOptions);
            AdLoader adLoader = builder.build();
            adLoader.loadAd(new AdRequest.Builder().build());
        } else {
            if (adsEventListener != null) {
                adsEventListener.onLoadError("");
            }
        }
    }

    public void populateUnifiedNativeAdView(Context context, FrameLayout frameLayout, NativeAd nativeAd, boolean isShowMedia, boolean isGrid) {
        if (getShowAds()) {
            if (isNetworkAvailable(context)) {
                LayoutInflater inflater = LayoutInflater.from(context);
                // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
                NativeAdView adView;
                if (isGrid) {
                    adView = (NativeAdView) inflater.inflate(R.layout.layout_big_native_ad_mob, null);
                } else {
                    adView = (NativeAdView) (isShowMedia ?
                            inflater.inflate(R.layout.layout_big_native_ad_mob, null) :
                            inflater.inflate(R.layout.layout_small_native_ad_mob, null));
                }

                if (frameLayout != null) {
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                    frameLayout.setVisibility(View.VISIBLE);
                }
                try {
                    com.google.android.gms.ads.nativead.MediaView mediaView = adView.findViewById(R.id.mediaView);
                    mediaView.setMediaContent(nativeAd.getMediaContent());
                    adView.setMediaView(mediaView);

                    adView.setHeadlineView(adView.findViewById(R.id.adTitle));
                    adView.setBodyView(adView.findViewById(R.id.adDescription));
                    adView.setIconView(adView.findViewById(R.id.adIcon));

                    ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

                    if (nativeAd.getBody() == null) {
                        adView.getBodyView().setVisibility(View.INVISIBLE);
                    } else {
                        adView.getBodyView().setVisibility(View.VISIBLE);
                        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
                    }

                    if (nativeAd.getIcon() == null) {
                        adView.getIconView().setVisibility(View.GONE);
                    } else {
                        ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
                        adView.getIconView().setVisibility(View.VISIBLE);
                    }

                    if (isShowMedia) {
                        adView.getMediaView().setVisibility(View.VISIBLE);
                    } else {
                        adView.getMediaView().setVisibility(View.GONE);
                    }

                    adView.setNativeAd(nativeAd);
                    VideoController vc = nativeAd.getMediaContent().getVideoController();
                    vc.mute(true);
                    if (vc.hasVideoContent()) {
                        vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                            @Override
                            public void onVideoEnd() {
                                super.onVideoEnd();
                            }
                        });
                    }

                    adView.setNativeAd(nativeAd);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "populateUnifiedNativeAdView Exception: " + e.getMessage());
                }
            }
        } else {
            frameLayout.setVisibility(View.GONE);
        }
    }
}
