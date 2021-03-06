package com.pdfscanner.pdf.scanpdf.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextPaint;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.pdfscanner.pdf.scanpdf.MainActivity;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.Util.PreferencesManager;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.ads.AdmobAdsManager;
import com.pdfscanner.pdf.scanpdf.service.ImageDataService;

import java.util.List;

import static com.android.billingclient.api.BillingClient.SkuType.SUBS;

public class SplashActivity extends AppCompatActivity {

    private BillingClient billingClienttt;
    AdmobAdsManager admobAdsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_splash);
        admobAdsManager = AdmobAdsManager.getInstance(this);

        gradientText();
        initAds();
        initBillingClient();
        permissionGrant();
    }

    private void gradientText(){
        TextView textView = findViewById(R.id.tvTitle);

        TextPaint paint = textView.getPaint();
        float width = paint.measureText(getString(R.string.app_name));

        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{
                        Color.parseColor("#00D8FF"),
                        Color.parseColor("#FFFFFF"),
                }, null, Shader.TileMode.CLAMP);

        textView.getPaint().setShader(textShader);
    }

    private void initAds(){
        Utils.getAdsIds(SplashActivity.this);
        Utils.getAdsCounter(SplashActivity.this);

        if (PreferencesManager.getString(SplashActivity.this,Constant.INTERSTITIAL_ID).isEmpty()){
            Utils.getAdsIds(SplashActivity.this);
        }else {
            admobAdsManager.loadInterstitialAd(SplashActivity.this, PreferencesManager.getString(SplashActivity.this,Constant.INTERSTITIAL_ID));
        }
    }

    private void initBillingClient(){
        billingClienttt = BillingClient.newBuilder(this)
                .enablePendingPurchases().setListener(purchasesUpdatedListener).build();

        billingClienttt.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    com.android.billingclient.api.Purchase.PurchasesResult queryPurchase = billingClienttt.queryPurchases(SUBS);
                    List<Purchase> queryPurchases = queryPurchase.getPurchasesList();

                    if (queryPurchases != null && queryPurchases.size() > 0) {
                        for (com.android.billingclient.api.Purchase purchase : queryPurchases) {
                            PreferencesManager.putSubscription(SplashActivity.this, true);
                        }
                    } else {
                        PreferencesManager.putSubscription(SplashActivity.this, false);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    private void permissionGrant(){
        boolean isPermission = isPermissionGranted();
        if (isPermission) {
            startService(new Intent(SplashActivity.this, ImageDataService.class));
        }

        new Handler(Looper.myLooper()).postDelayed(() -> {
            if (isPermission) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, PermissionActivity.class));
            }
            finish();
        }, 2000);
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<com.android.billingclient.api.Purchase> purchases) {
            // To be implemented in a later section.
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (com.android.billingclient.api.Purchase purchase : purchases) {
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                com.android.billingclient.api.Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClienttt.queryPurchases(SUBS);
                List<com.android.billingclient.api.Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
                if (alreadyPurchases != null)
                    for (com.android.billingclient.api.Purchase purchase : alreadyPurchases) {
                    }

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                Toast.makeText(getApplicationContext(), "Purchase Canceled", Toast.LENGTH_SHORT).show();
            } else {
                // Handle any other error codes.
                Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }
}