package com.pdfscanner.pdf.scanpdf.stickerview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

/**
 * @author wupanjie
 */
public class BitmapStickerIcon extends DrawableSticker implements StickerIconEvent {
  public static final float DEFAULT_ICON_RADIUS = 30f;
  public static final float DEFAULT_ICON_EXTRA_RADIUS = 10f;

  public static final int LEFT_TOP = 0;
  public static final int RIGHT_TOP = 1;
  public static final int LEFT_BOTTOM = 2;
  public static final int RIGHT_BOTOM = 3;

  private float iconRadius = DEFAULT_ICON_RADIUS;
  private float iconExtraRadius = DEFAULT_ICON_EXTRA_RADIUS;
  private float x;
  private float y;
  private int position = LEFT_TOP;

  private StickerIconEvent iconEvent;

  public BitmapStickerIcon(Drawable drawable, int position) {
    super(drawable);
    this.position = position;
  }

  public void draw(Canvas canvas, Paint paint) {
    canvas.drawCircle(x, y, iconRadius, paint);
    super.draw(canvas);
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public float getIconRadius() {
    return iconRadius;
  }

  public void setIconRadius(float iconRadius) {
    this.iconRadius = iconRadius;
  }

  public float getIconExtraRadius() {
    return iconExtraRadius;
  }

  public void setIconExtraRadius(float iconExtraRadius) {
    this.iconExtraRadius = iconExtraRadius;
  }

  @Override
  public void onActionDown(TextStickerView textStickerView, MotionEvent event) {
    if (iconEvent != null) iconEvent.onActionDown(textStickerView, event);
  }

  @Override
  public void onActionMove(TextStickerView textStickerView, MotionEvent event) {
    if (iconEvent != null) iconEvent.onActionMove(textStickerView, event);
  }

  @Override
  public void onActionUp(TextStickerView textStickerView, MotionEvent event) {
    if (iconEvent != null) iconEvent.onActionUp(textStickerView, event);
  }

  public StickerIconEvent getIconEvent() {
    return iconEvent;
  }

  public void setIconEvent(StickerIconEvent iconEvent) {
    this.iconEvent = iconEvent;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }
}
