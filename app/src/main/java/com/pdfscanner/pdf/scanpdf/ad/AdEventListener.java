package com.pdfscanner.pdf.scanpdf.ad;

public interface AdEventListener {
    void onAdLoaded(Object object);

    void onAdClosed();

    void onLoadError(String errorCode);
}
