package com.pdfscanner.pdf.scanpdf.Util;

import android.graphics.Bitmap;

public class Constant {
    public static Bitmap signatureBitmap = null;
    public static Bitmap EditBitmap = null;
    public static Bitmap cropBitmap1 = null;
    public static Bitmap cropBitmap2 = null;
    public static final String document = "Document";
    public static final String business = "Business Card";
    public static final String idCard = "Id Card";
    public static final String ocr = "OCR";
    public static final String hiddenImagePath = ".Image";
    public static final String ImageToPDFPath = "Image To PDF";
    public final static String SHARED_PREFS = "pdf_scanner";
    public final static String SHARED_PREFS_RATE_US = "pdf_scanner_rate_us";
    public final static String SHARED_PREFS_SUBSCRIPTION = "pdf_scanner_subcription";
    public final static String SHARED_PREFS_SUBSCRIPTION_DATE = "pdf_scanner_subcription_date";
    public static final int DEFAULT_QUALITY_VALUE = 30;
    public static final int DEFAULT_BORDER_WIDTH = 0;
    public static final String DEFAULT_PAGE_SIZE = "A4";
    public static final String pdfExtension = ".pdf";
    public static final String IMAGE_SCALE_TYPE_ASPECT_RATIO = "maintain_aspect_ratio";
    public static boolean SHOW_OPEN_ADS = true;
    public static void showOpenAd() {
        SHOW_OPEN_ADS = true;
    }
    public static void hideOpenAd() {
        SHOW_OPEN_ADS = false;
    }
    public static String APP_OPEN_ID = "app_open_id";
    public static String APP_UNIT_ID = "app_unit_id";
    public static String BANNER_ID = "banner_id";
    public static String INTERSTITIAL_ID = "interstitial_id";
    public static String NATIVE_APP_ID = "native_app_id";
    public static String MAIN_ACTIVITY = "main_activity";
    public static String EDIT_DOCUMENT = "edit_document";
    public static String PREVIEW = "preview";
    public static String PREVIEW_ORIGINAL = "preview_Original";
    public static String ADS_COUNTER = "AdsCounter";
}
