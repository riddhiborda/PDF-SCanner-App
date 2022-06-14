package com.pdfscanner.pdf.scanpdf.stickerview;

import android.view.MotionEvent;

/**
 * @author wupanjie
 */

public interface StickerIconEvent {
  void onActionDown(TextStickerView textStickerView, MotionEvent event);

  void onActionMove(TextStickerView textStickerView, MotionEvent event);

  void onActionUp(TextStickerView textStickerView, MotionEvent event);
}
