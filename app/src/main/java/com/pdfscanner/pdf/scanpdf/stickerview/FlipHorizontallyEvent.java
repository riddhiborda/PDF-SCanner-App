package com.pdfscanner.pdf.scanpdf.stickerview;

import android.view.MotionEvent;

/**
 * @author wupanjie
 */

public class FlipHorizontallyEvent implements StickerIconEvent {
  @Override
  public void onActionDown(TextStickerView textStickerView, MotionEvent event) {

  }

  @Override
  public void onActionMove(TextStickerView textStickerView, MotionEvent event) {

  }

  @Override
  public void onActionUp(TextStickerView textStickerView, MotionEvent event) {
    textStickerView.flipCurrentSticker(TextStickerView.FLIP_HORIZONTALLY);
  }
}
