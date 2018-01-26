/*
 * Copyright 2018 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.android.largeimage;

/*
 * Created by Hippo on 2018/1/24.
 */

import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

/**
 * TransformableDrawable can apply scaling or scrolling to any kind of drawable.
 */
public class TransformableDrawable extends WrapperDrawable {

  private static final float MIN_SCALE = 1.0f;
  private static final float MAX_SCALE = 3.0f;

  @IntDef({SCALE_TYPE_ORIGIN, SCALE_TYPE_FIT_WIDTH, SCALE_TYPE_FIT_HEIGHT,
      SCALE_TYPE_FIT, SCALE_TYPE_FIXED})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ScaleType {}

  public static final int SCALE_TYPE_ORIGIN = 0;
  public static final int SCALE_TYPE_FIT_WIDTH = 1;
  public static final int SCALE_TYPE_FIT_HEIGHT = 2;
  public static final int SCALE_TYPE_FIT = 3;
  public static final int SCALE_TYPE_FIXED = 4;

  @IntDef({START_POSITION_TOP_LEFT, START_POSITION_TOP_RIGHT, START_POSITION_BOTTOM_LEFT,
      START_POSITION_BOTTOM_RIGHT, START_POSITION_CENTER})
  @Retention(RetentionPolicy.SOURCE)
  @interface StartPosition {}

  public static final int START_POSITION_TOP_LEFT = 0;
  public static final int START_POSITION_TOP_RIGHT = 1;
  public static final int START_POSITION_BOTTOM_LEFT = 2;
  public static final int START_POSITION_BOTTOM_RIGHT = 3;
  public static final int START_POSITION_CENTER = 4;

  @ScaleType
  private int scaleType = SCALE_TYPE_FIT;
  @StartPosition
  private int startPosition = START_POSITION_TOP_LEFT;

  private Rect visibleRect = new Rect();

  private RectF srcRectF = new RectF();
  private RectF dstRectF = new RectF();
  private boolean drawRectFDirty = true;

  private int width = -1;
  private int height = -1;

  private float offsetX;
  private float offsetY;
  private float scale;

  private float minScale;
  private float maxScale;
  private float[] scaleLevels;

  public TransformableDrawable() {
    super();
  }

  public TransformableDrawable(Drawable drawable) {
    super(drawable);
  }

  @Override
  public void onSetWrappedDrawable(@Nullable Drawable oldDrawable, @Nullable Drawable newDrawable) {
    if (newDrawable != null) {
      newDrawable.setCallback(this);
      updateWrapperDrawableBounds();
      updateScaleLevels();
      resetLayout();
    } else {
      width = -1;
      height = -1;
      scale = 0.0f;
      drawRectFDirty = true;
    }
  }

  /**
   * Set visible rect for this drawable.
   * The coordinate axis is the same as {@link #setBounds(Rect)}.
   */
  public final void setVisibleRect(int left, int top, int right, int bottom) {
    visibleRect.set(left, top, right, bottom);
    drawRectFDirty = true;
  }

  /**
   * Returns next scale level for automatic scale-level changing.
   *
   * It always returns a bigger scale level if current is not bigger than the max scale level,
   * or the smallest scale level.
   */
  public float getNextScaleLevel() {
    float result = scaleLevels[0];
    for (float value: scaleLevels) {
      if (scale < value - 0.01f) {
        result = value;
        break;
      }
    }
    return result;
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    drawRectFDirty = true;
    updateWrapperDrawableBounds();
    updateScaleLevels();
    if (scale == 0.0f) {
      resetLayout();
    }
  }

  /*
   * Update wrapper drawable bounds. Returns true if the bounds changes.
   */
  private boolean updateWrapperDrawableBounds() {
    Rect bounds = getBounds();
    int oldWidth = width;
    int oldHeight = height;
    Drawable drawable = getDrawable();

    if (drawable != null) {
      int dWidth = drawable.getIntrinsicWidth();
      int dHeight = drawable.getIntrinsicHeight();
      width = dWidth > 0 ? dWidth : Math.max(bounds.width(), 0);
      height = dHeight > 0 ? dHeight : Math.max(bounds.height(), 0);
      drawable.setBounds(0, 0, width, height);
    } else {
      width = -1;
      height = -1;
    }

    return oldWidth != width || oldHeight != height;
  }

  /*
   * Update max scale, min scale and scale levels.
   */
  private void updateScaleLevels() {
    Rect bounds = getBounds();
    if (width <= 0 || height <= 0 || bounds.isEmpty()) {
      return;
    }

    int vWidth = bounds.width();
    int vHeight = bounds.height();
    int dWidth = width;
    int dHeight = height;

    float wScale = (float) vWidth / (float) dWidth;
    float hScale = (float) vHeight / (float) dHeight;
    if (Math.max(wScale, hScale) < MAX_SCALE) {
      scaleLevels = new float[] {MIN_SCALE, wScale, hScale, MAX_SCALE};
    } else {
      scaleLevels = new float[] {MIN_SCALE, wScale, hScale};
    }
    Arrays.sort(scaleLevels);
    minScale = scaleLevels[0];
    maxScale = scaleLevels[scaleLevels.length - 1];
  }

  /*
   * Reset scale and offset to fit scale type and start position.
   */
  private void resetLayout() {
    Rect bounds = getBounds();
    if (width <= 0 || height <= 0 || bounds.isEmpty()) {
      return;
    }

    int vWidth = bounds.width();
    int vHeight = bounds.height();
    int dWidth = width;
    int dHeight = height;

    float wScale = (float) vWidth / (float) dWidth;
    float hScale = (float) vHeight / (float) dHeight;

    float tWidth;
    float tHeight;
    switch (scaleType) {
      case SCALE_TYPE_ORIGIN:
        scale = 1.0f;
        tWidth = dWidth;
        tHeight = dHeight;
        break;
      case SCALE_TYPE_FIT_WIDTH:
        scale = wScale;
        tWidth = vWidth;
        tHeight = dHeight * wScale;
        break;
      case SCALE_TYPE_FIT_HEIGHT:
        scale = hScale;
        tWidth = dWidth * hScale;
        tHeight = vHeight;
        break;
      default:
      case SCALE_TYPE_FIT:
        scale = Math.min(wScale, hScale);
        tWidth = dWidth * scale;
        tHeight = dHeight * scale;
        break;
      case SCALE_TYPE_FIXED:
        scale = Utils.clamp(scale, minScale, maxScale);
        tWidth = dWidth * scale;
        tHeight = dHeight * scale;
        break;
    }

    switch (startPosition) {
      default:
      case START_POSITION_TOP_LEFT:
        offsetX = 0;
        offsetY = 0;
        break;
      case START_POSITION_TOP_RIGHT:
        offsetX = vWidth - tWidth;
        offsetY = 0;
        break;
      case START_POSITION_BOTTOM_LEFT:
        offsetX = 0;
        offsetY = vHeight - tHeight;
        break;
      case START_POSITION_BOTTOM_RIGHT:
        offsetX = vWidth - tWidth;
        offsetY = vHeight - tHeight;
        break;
      case START_POSITION_CENTER:
        offsetX = (vWidth - tWidth) / 2;
        offsetY = (vHeight - tHeight) / 2;
        break;
    }

    fixLayout();

    drawRectFDirty = true;
  }

  /*
   * Fix scale and offset.
   */
  private void fixLayout() {
    Rect bounds = getBounds();
    if (width <= 0 || height <= 0 || bounds.isEmpty()) {
      return;
    }

    scale = Utils.clamp(scale, minScale, maxScale);

    int vWidth = bounds.width();
    int vHeight = bounds.height();
    float tWidth = width * scale;
    float tHeight = height * scale;

    if (tWidth > vWidth) {
      offsetX = Utils.clamp(offsetX, vWidth - tWidth, 0);
    } else {
      offsetX = (vWidth - tWidth) / 2;
    }

    if (tHeight > vHeight) {
      offsetY = Utils.clamp(offsetY, vHeight - tHeight, 0);
    } else {
      offsetY = (vHeight - tHeight) / 2;
    }

    drawRectFDirty = true;
  }

  public void setScale(float scale) {
    this.scale = scale;
    scaleType = SCALE_TYPE_FIXED;
    drawRectFDirty = true;
    resetLayout();
    invalidateSelf();
  }

  public void setScaleType(int scaleType) {
    this.scaleType = scaleType;
    resetLayout();
    invalidateSelf();
  }

  public void setStartPosition(int startPosition) {
    this.startPosition = startPosition;
    resetLayout();
    invalidateSelf();
  }

  public void scroll(float dx, float dy) {
    scroll(dx, dy, null);
  }

  public void scroll(float dx, float dy, @Nullable float[] remain) {
    if (width == 0 || height == 0) {
      return;
    }

    float oldOffsetX = offsetX;
    float oldOffsetY = offsetY;
    offsetX += dx;
    offsetY += dy;
    fixLayout();

    if (remain != null) {
      remain[0] = dx - (offsetX - oldOffsetX);
      remain[1] = dy - (offsetY - oldOffsetY);
    }

    drawRectFDirty = true;

    invalidateSelf();
  }

  public void scale(float x, float y, float factor) {
    scale(x, y, factor, null);
  }

  public void scale(float x, float y, float factor, @Nullable float[] remain) {
    if (width == 0 || height == 0) {
      return;
    }

    float oldScale = scale;
    scale = Utils.clamp(scale * factor, minScale, maxScale);
    float actualFactor = scale / oldScale;
    if (scale == oldScale) {
      return;
    }

    offsetX = x - ((x - offsetX) * actualFactor);
    offsetY = y - ((y - offsetY) * actualFactor);
    fixLayout();

    if (remain != null) {
      remain[0] = factor / actualFactor;
    }

    drawRectFDirty = true;

    invalidateSelf();
  }

  /*
   * Apply bounds, visibleRect, scale, offsetX, offsetY to
   * srcRectF and dstRectF.
   */
  private void updateDrawRectF() {
    if (!drawRectFDirty) {
      return;
    }
    drawRectFDirty = false;

    Rect bounds = getBounds();
    RectF srcRectF = this.srcRectF;
    RectF dstRectF = this.dstRectF;

    dstRectF.set(bounds);

    if (!dstRectF.intersect(visibleRect.left, visibleRect.top,
        visibleRect.right, visibleRect.bottom)) {
      srcRectF.setEmpty();
      dstRectF.setEmpty();
      return;
    }

    srcRectF.set(0, 0, width * scale, height * scale);
    srcRectF.offset(bounds.left + offsetX, bounds.top + offsetY);

    if (!dstRectF.intersect(srcRectF)) {
      srcRectF.setEmpty();
      dstRectF.setEmpty();
      return;
    }

    srcRectF.set(dstRectF);
    srcRectF.offset(-bounds.left - offsetX, -bounds.top - offsetY);
    srcRectF.left = srcRectF.left / scale;
    srcRectF.top = srcRectF.top / scale;
    srcRectF.right = srcRectF.right / scale;
    srcRectF.bottom = srcRectF.bottom / scale;

    if (!srcRectF.intersect(0, 0, width, height)) {
      srcRectF.setEmpty();
      dstRectF.setEmpty();
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    Rect bounds = getBounds();
    Drawable drawable = getDrawable();

    if (drawable != null && width > 0 && height > 0 && !bounds.isEmpty()) {
      if (drawable instanceof PreciseDrawable) {
        updateDrawRectF();
        ((PreciseDrawable) drawable).draw(canvas, srcRectF, dstRectF);
      } else {
        int saved = canvas.save();
        canvas.clipRect(visibleRect);
        canvas.translate(bounds.left + offsetX, bounds.top + offsetY);
        canvas.scale(scale, scale);
        drawable.draw(canvas);
        canvas.restoreToCount(saved);
      }
    }
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    if (updateWrapperDrawableBounds()) {
      updateScaleLevels();
      fixLayout();
    }
    super.invalidateDrawable(who);
  }
}
