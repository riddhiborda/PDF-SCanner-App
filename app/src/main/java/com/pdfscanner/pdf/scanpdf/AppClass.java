package com.pdfscanner.pdf.scanpdf;

import android.app.Application;

import androidx.multidex.MultiDex;

import com.onesignal.OneSignal;
import com.pdfscanner.pdf.scanpdf.Util.NotificationUtils;
import com.pdfscanner.pdf.scanpdf.ad.AppOpenManager;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

public class AppClass extends Application {

    AppOpenManager appOpenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationUtils notiUtils = new NotificationUtils(AppClass.this);
        }

        appOpenManager = new AppOpenManager(this);

        new Thread(new Runnable() {
            @Override
            public void run() {


                OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
                OneSignal.initWithContext(AppClass.this);
                OneSignal.setAppId(getResources().getString(R.string.onesignal_id) );

                YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(getResources().getString(R.string.app_metrica)).build();
                YandexMetrica.activate(getApplicationContext(), config);
                YandexMetrica.enableActivityAutoTracking(AppClass.this);

            }
        });
    }
}
