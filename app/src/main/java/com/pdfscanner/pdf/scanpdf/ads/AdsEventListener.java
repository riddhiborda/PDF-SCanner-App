package com.pdfscanner.pdf.scanpdf.ads;

public interface AdsEventListener {
    void onAdLoaded(Object object);
    void onAdClosed();
    void onLoadError(String errorCode);
}
