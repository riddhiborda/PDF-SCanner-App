package com.pdfscanner.pdf.scanpdf.Util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    public static void setRateUs(Context context, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constant.SHARED_PREFS_RATE_US, value);
        editor.commit();

    }

    public static boolean getRate(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getBoolean(Constant.SHARED_PREFS_RATE_US, false);
    }


    public static void putSubscription(Context context, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constant.SHARED_PREFS_SUBSCRIPTION, value);
        editor.commit();

    }

    public static boolean getSubscription(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getBoolean(Constant.SHARED_PREFS_SUBSCRIPTION, false);
    }

    public static void putPurchaseDate(Context context, String value) {

        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constant.SHARED_PREFS_SUBSCRIPTION_DATE, value);
        editor.commit();

    }

    public static String getPurchaseDate(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getString(Constant.SHARED_PREFS_SUBSCRIPTION_DATE,"");
    }

    public static void saveInteger(Context context, String key, int value){
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInteger(Context context, String key){
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getInt(key,0);
    }

    public static void saveString(Context context, String key, String value){
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key){
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getString(key,"");
    }
}
