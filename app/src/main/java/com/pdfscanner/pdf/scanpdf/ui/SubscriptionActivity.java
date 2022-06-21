package com.pdfscanner.pdf.scanpdf.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.PreferencesManager;
import com.pdfscanner.pdf.scanpdf.ad.AdmobAdManager;
import com.pdfscanner.pdf.scanpdf.databinding.ActivitySubscriptionBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;
import static com.android.billingclient.api.BillingClient.SkuType.SUBS;
import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

public class SubscriptionActivity extends AppCompatActivity {

    ActivitySubscriptionBinding binding;
    int pos = 0;
    boolean showAd = false;

    private static final List<String> SKUS = Arrays.asList("android.test.purchased",
            "android.test.purchased", "android.test.purchased");

//    private static final List<String> SKUS = Arrays.asList("com.pdfconvertor.imgetopdf.allpdf.weekly",
//            "com.pdfconvertor.imgetopdf.allpdf.monthly", "com.pdfconvertor.imgetopdf.allpdf.yearly");


    List<SkuDetails> skuDetailsList = new ArrayList<>();
    private BillingClient billingClient;
    String PRODUCT_ID = "";
    AdmobAdManager admobAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_subscription);
        admobAdManager = AdmobAdManager.getInstance(this);
        intView();
    }

    public void intView() {

        if (getIntent() != null) {
             showAd = getIntent().getBooleanExtra("ShowAd", false);

        } else {
            showAd = false;
        }

        setClickListener();

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();


        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    initiatePurchase();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        final PropertyValuesHolder scaleXPVH = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f, 1f, 0.8f);
        final PropertyValuesHolder scaleYPVH = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f, 1f, 0.8f);

        final ObjectAnimator animation1 = ObjectAnimator.ofPropertyValuesHolder(binding.btnMonthly, scaleXPVH, scaleYPVH);
        animation1.setDuration(1500);
        animation1.setRepeatCount(Animation.INFINITE);
        animation1.setInterpolator(new AccelerateDecelerateInterpolator());
        animation1.start();


    }

    public void setClickListener() {

        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showAd){
                    admobAdManager.loadInterstitialAd(SubscriptionActivity.this, getResources().getString(R.string.interstitial_id), 1, new AdmobAdManager.OnAdClosedListener() {
                        @Override
                        public void onAdClosed(Boolean isShowADs) {

                        }
                    });
                }
                finish();
            }
        });

        binding.btnWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (skuDetailsList != null && skuDetailsList.size() != 0) {
                    pos = 0;

                    binding.btnWeekly.setClickable(false);
                    binding.btnMonthly.setClickable(false);
                    binding.btnYearly.setClickable(false);

                    SkuDetails skuDetails = null;
                    for (int i = 0; i < skuDetailsList.size(); i++) {
                        if (i == pos) {
                            skuDetails = skuDetailsList.get(i);
                            break;
                        }
                    }


                    if (skuDetails != null) {
//                        mCheckout.startPurchaseFlow(sku, null, new PurchaseListener());
                        PRODUCT_ID = skuDetails.getSku();
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(SubscriptionActivity.this, billingFlowParams).getResponseCode();
                    }

                } else {
                    Toast.makeText(SubscriptionActivity.this, "Currently there are no subscription plan available!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        binding.btnMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (skuDetailsList != null && skuDetailsList.size() != 0) {
                    pos = 1;

                    binding.btnWeekly.setClickable(false);
                    binding.btnMonthly.setClickable(false);
                    binding.btnYearly.setClickable(false);

                    SkuDetails skuDetails = null;
                    for (int i = 0; i < skuDetailsList.size(); i++) {
                        if (i == pos) {
                            skuDetails = skuDetailsList.get(i);
                            break;
                        }
                    }


                    if (skuDetails != null) {
//                        mCheckout.startPurchaseFlow(sku, null, new PurchaseListener());
                        PRODUCT_ID = skuDetails.getSku();
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(SubscriptionActivity.this, billingFlowParams).getResponseCode();
                    }

                } else {
                    Toast.makeText(SubscriptionActivity.this, "Currently there are no subscription plan available!", Toast.LENGTH_SHORT).show();
                }
            }
        });



        binding.btnYearly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (skuDetailsList != null && skuDetailsList.size() != 0) {
                    pos = 2;

                    binding.btnWeekly.setClickable(false);
                    binding.btnMonthly.setClickable(false);
                    binding.btnYearly.setClickable(false);

                    SkuDetails skuDetails = null;
                    for (int i = 0; i < skuDetailsList.size(); i++) {
                        if (i == pos) {
                            skuDetails = skuDetailsList.get(i);
                            break;
                        }
                    }


                    if (skuDetails != null) {
//                        mCheckout.startPurchaseFlow(sku, null, new PurchaseListener());
                        PRODUCT_ID = skuDetails.getSku();
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(SubscriptionActivity.this, billingFlowParams).getResponseCode();
                    }

                } else {
                    Toast.makeText(SubscriptionActivity.this, "Currently there are no subscription plan available!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

        private void initiatePurchase() {
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
//        params.setSkusList(SKUS).setType(INAPP);
        params.setSkusList(SKUS).setType(SUBS);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            // Process the result.
                            if (skuDetailList != null && skuDetailList.size() > 0) {

                                for (SkuDetails sku : skuDetailList) {
                                    skuDetailsList.add(sku);
                                    if (sku.getSku().equalsIgnoreCase(SKUS.get(0))) {
//                                        binding.firstRadio.setText("Weekly with a 3-day free trial at " + sku.getPrice());
                                        binding.txtPrice1.setText(sku.getPrice() + "/Weekly");
                                    } else if (sku.getSku().equalsIgnoreCase(SKUS.get(1))) {
                                        binding.txtPrice2.setText( sku.getPrice()+ "/Monthly");
                                    } else if (sku.getSku().equalsIgnoreCase(SKUS.get(2))) {
                                        binding.txtPrice3.setText(sku.getPrice()+ "/Yearly");
                                    }

                                }

                            } else {
                                //try to add item/product id "purchase" inside managed product in google play console
                                Toast.makeText(getApplicationContext(), "Purchase Item not Found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    " Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<com.android.billingclient.api.Purchase> purchases) {
            // To be implemented in a later section.
//            Toast.makeText(Prefrancemanager.this, "Changed", Toast.LENGTH_LONG).show();
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (com.android.billingclient.api.Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
//                com.android.billingclient.api.Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);
                com.android.billingclient.api.Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(SUBS);
                List<com.android.billingclient.api.Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
                if (alreadyPurchases != null)
                    for (com.android.billingclient.api.Purchase purchase : alreadyPurchases) {
                        handlePurchase(purchase);
                    }

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                binding.btnWeekly.setClickable(true);
                binding.btnMonthly.setClickable(true);
                binding.btnYearly.setClickable(true);
                Toast.makeText(getApplicationContext(), "Purchase Canceled", Toast.LENGTH_SHORT).show();
            } else {
                // Handle any other error codes.
                binding.btnWeekly.setClickable(true);
                binding.btnMonthly.setClickable(true);
                binding.btnYearly.setClickable(true);
                Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    void handlePurchase(com.android.billingclient.api.Purchase purchasee) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.
        com.android.billingclient.api.Purchase purchase = purchasee;

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.
        if (PRODUCT_ID.equals(purchase.getSku()) && purchase.getPurchaseState() == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
           /* if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                // Invalid purchase
                // show error to user
                Toast.makeText(getApplicationContext(), "Error : Invalid Purchase", Toast.LENGTH_SHORT).show();
                return;
            }
*/
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                binding.btnWeekly.setClickable(true);
                binding.btnMonthly.setClickable(true);
                binding.btnYearly.setClickable(true);
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            } else {
                // Grant entitlement to the user on item purchase
                // restart activity
                binding.btnWeekly.setClickable(true);
                binding.btnMonthly.setClickable(true);
                binding.btnYearly.setClickable(true);
                if (!PreferencesManager.getSubscription(SubscriptionActivity.this)) {
                    PreferencesManager.putSubscription(SubscriptionActivity.this, true);
                    setPurchased();
//                    Toast.makeText(getApplicationContext(), "Item Purchased", Toast.LENGTH_SHORT).show();
                }
            }
        }//if purchase is pending
        else if (PRODUCT_ID.equals(purchase.getSku()) && purchase.getPurchaseState() == com.android.billingclient.api.Purchase.PurchaseState.PENDING) {
            binding.btnWeekly.setClickable(true);
            binding.btnMonthly.setClickable(true);
            binding.btnYearly.setClickable(true);
            Toast.makeText(getApplicationContext(),
                    "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT).show();
        }//if purchase is unknown
        else if (PRODUCT_ID.equals(purchase.getSku()) && purchase.getPurchaseState() == com.android.billingclient.api.Purchase.PurchaseState.UNSPECIFIED_STATE) {
            PreferencesManager.putSubscription(SubscriptionActivity.this, false);
            binding.btnWeekly.setClickable(true);
            binding.btnMonthly.setClickable(true);
            binding.btnYearly.setClickable(true);
            Toast.makeText(SubscriptionActivity.this, "Failed to purchase!Please try again later!", Toast.LENGTH_SHORT).show();
        }

    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //if purchase is acknowledged
                // Grant entitlement to the user. and restart activity

                binding.btnWeekly.setClickable(true);
                binding.btnMonthly.setClickable(true);
                binding.btnYearly.setClickable(true);
                PreferencesManager.putSubscription(SubscriptionActivity.this, true);
                Calendar calendar = Calendar.getInstance();
                PreferencesManager.putPurchaseDate(SubscriptionActivity.this, getDate(calendar.getTimeInMillis()));
                setPurchased();
                Toast.makeText(getApplicationContext(), "Purchase Item successfully!", Toast.LENGTH_SHORT).show();

            }
        }
    };


    private void setPurchased() {
        setResult(RESULT_OK);
        finish();
    }

    public String getDate(long milliSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        if (pos == 0) {
            calendar.add(Calendar.DATE, 10);
        } else if (pos == 1) {
            calendar.add(Calendar.MONTH, 1);
        } else if (pos == 2) {
            calendar.add(Calendar.YEAR, 1);
        }
        return String.valueOf(calendar.getTimeInMillis());
    }


}