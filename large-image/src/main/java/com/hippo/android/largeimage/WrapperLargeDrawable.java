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
 * Created by Hippo on 2018/1/9.
 */

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Arrays;

public class WrapperLargeDrawable extends LargeDrawable implements Drawable.Callback {

  private static final float MIN_SCALE = 1.0f;
  private static final float MAX_SCALE = 3.0f;

  @Nullable
  private Drawable drawable;

  @ScaleType
  private int scaleType = SCALE_TYPE_FIT;
  @StartPosition
  private int startPosition = START_POSITION_TOP_LEFT;

  private int width;
  private int height;

  private float offsetX;
  private float offsetY;
  private float scale;
  private float minScale;
  private float maxScale;
  private float[] scaleLevels;

  public void setWrappedDrawable(@Nullable Drawable drawable) {
    if (this.drawable == drawable) {
      return;
    }

    if (this.drawable != null) {
      this.drawable.setCallback(null);
    }

    this.drawable = drawable;

    if (drawable != null) {
      drawable.setCallback(this);
      onBoundsChange(getBounds());
    } else {
      width = 0;
      height = 0;
    }

    invalidateSelf();
  }

  @Override
  public int getIntrinsicWidth() {
    return drawable != null ? drawable.getIntrinsicWidth() : 0;
  }

  @Override
  public int getIntrinsicHeight() {
    return drawable != null ? drawable.getIntrinsicHeight() : 0;
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    if (drawable == null) {
      return;
    }

    if (bounds.isEmpty()) {
      width = 0;
      height = 0;
    } else {
      int dWidth = drawable.getIntrinsicWidth();
      int dHeight = drawable.getIntrinsicHeight();
      width = dWidth > 0 ? dWidth : bounds.width();
      height = dHeight > 0 ? dHeight : bounds.height();

      drawable.setBounds(0, 0, width, height);
      resetLayout(bounds);
    }
  }

  // Reset offset and scale to fit
  private void resetLayout(Rect bounds) {
    if (width == 0 || height == 0) {
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

    fixOffset();
  }

  private void fixOffset() {
    int vWidth = getBounds().width();
    int vHeight = getBounds().height();
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
  }

  @Override
  public void setScale(float scale) {
    this.scale = scale;
    this.scaleType = SCALE_TYPE_FIXED;

    Rect bounds = getBounds();
    if (!bounds.isEmpty() && drawable != null) {
      resetLayout(bounds);
      invalidateSelf();
    }
  }

  @Override
  public void setScaleType(int scaleType) {
    this.scaleType = scaleType;

    Rect bounds = getBounds();
    if (!bounds.isEmpty() && drawable != null) {
      resetLayout(bounds);
      invalidateSelf();
    }
  }

  @Override
  public void setStartPosition(int startPosition) {
    this.startPosition = startPosition;

    Rect bounds = getBounds();
    if (!bounds.isEmpty() && drawable != null) {
      resetLayout(bounds);
      invalidateSelf();
    }
  }

  @Override
  public void scroll(float dx, float dy, @Nullable float[] remain) {
    if (width == 0 || height == 0) {
      return;
    }

    float oldOffsetX = offsetX;
    float oldOffsetY = offsetY;
    offsetX += dx;
    offsetY += dy;
    fixOffset();

    if (remain != null) {
      remain[0] = dx - (offsetX - oldOffsetX);
      remain[1] = dy - (offsetY - oldOffsetY);
    }

    invalidateSelf();
  }

  @Override
  public void scale(float x, float y, float factor) {
    if (width == 0 || height == 0) {
      return;
    }

    float oldScale = scale;
    scale = Utils.clamp(scale * factor, minScale, maxScale);
    factor = scale / oldScale;
    if (scale == oldScale) {
      return;
    }

    offsetX = x - ((x - offsetX) * factor);
    offsetY = y - ((y - offsetY) * factor);
    fixOffset();

    invalidateSelf();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (drawable != null && width != 0 && height != 0) {
      int saved = canvas.save();
      canvas.translate(offsetX, offsetY);
      canvas.scale(scale, scale);
      drawable.draw(canvas);
      canvas.restoreToCount(saved);
    }
  }

  @Override
  public void setAlpha(int alpha) {}

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {}

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    onBoundsChange(getBounds());
    invalidateSelf();
  }

  @Override
  public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
    scheduleSelf(what, when);
  }

  @Override
  public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
    unscheduleSelf(what);
  }
}
