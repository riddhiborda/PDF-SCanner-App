package com.pdfscanner.pdf.scanpdf;

import android.app.Application;

import androidx.multidex.MultiDex;

import com.onesignal.OneSignal;
import com.pdfscanner.pdf.scanpdf.ads.AppOpenAdsManager;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

public class BaseClass extends Application {

    AppOpenAdsManager appOpenAdsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        appOpenAdsManager = new AppOpenAdsManager(this);

        new Thread(() -> {
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
            OneSignal.initWithContext(BaseClass.this);
            OneSignal.setAppId(getResources().getString(R.string.onesignal_id) );

            YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(getResources().getString(R.string.app_metrica)).build();
            YandexMetrica.activate(getApplicationContext(), config);
            YandexMetrica.enableActivityAutoTracking(BaseClass.this);
        });
    }
}
