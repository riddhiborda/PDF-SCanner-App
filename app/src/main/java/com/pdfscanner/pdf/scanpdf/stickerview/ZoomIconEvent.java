package com.pdfscanner.pdf.scanpdf.stickerview;

import android.view.MotionEvent;

/**
 * @author wupanjie
 */

public class ZoomIconEvent implements StickerIconEvent {
  @Override
  public void onActionDown(TextStickerView textStickerView, MotionEvent event) {

  }

  @Override
  public void onActionMove(TextStickerView textStickerView, MotionEvent event) {
    textStickerView.zoomAndRotateCurrentSticker(event);
  }

  @Override
  public void onActionUp(TextStickerView textStickerView, MotionEvent event) {
    if (textStickerView.getOnStickerOperationListener() != null) {
      textStickerView.getOnStickerOperationListener()
          .onStickerZoomFinished(textStickerView.getCurrentSticker());
    }
  }
}
