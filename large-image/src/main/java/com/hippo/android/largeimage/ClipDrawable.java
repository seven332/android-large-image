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
 * Created by Hippo on 2018/1/26.
 */

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * ClipDrawable clips a drawable.
 */
public class ClipDrawable extends WrapperDrawable {

  private static final int CLIP_NONE = 0;
  private static final int CLIP_RECT = 1;
  private static final int CLIP_PERCENT = 2;

  private int clipMode = CLIP_NONE;
  private Rect clipRect = new Rect();
  private RectF clipPercent = new RectF();
  private Rect clip = new Rect();

  /**
   * Clips drawable to a specified region.
   * If the region is out of drawable size, clamp the region.
   */
  public void clipRect(Rect clip) {
    clipRect(clip.left, clip.top, clip.right, clip.bottom);
  }

  /**
   * Clips drawable to a specified region.
   * If the region is out of drawable size, clamp the region.
   */
  public void clipRect(int left, int top, int right, int bottom) {
    if (clipMode != CLIP_RECT || clipRect.left != left || clipRect.top != top ||
        clipRect.right != right || clipRect.bottom != bottom) {
      clipMode = CLIP_RECT;
      clipRect.set(left, top, right, bottom);
      updateClip();
      updateBounds();
      invalidateSelf();
    }
  }

  /**
   * Clips drawable to a specified region.
   * The region is described in percent, {@code [0.0f, 1.0f]}.
   * If the region is out of drawable size, clamp the region.
   */
  public void clipPercent(RectF clip) {
    clipPercent(clip.left, clip.top, clip.right, clip.bottom);
  }

  /**
   * Clips drawable to a specified region.
   * The region is described in percent, {@code [0.0f, 1.0f]}.
   * If the region is out of drawable size, clamp the region.
   */
  public void clipPercent(float left, float top, float right, float bottom) {
    if (clipMode != CLIP_PERCENT || clipPercent.left != left || clipPercent.top != top ||
        clipPercent.right != right || clipPercent.bottom != bottom) {
      clipMode = CLIP_PERCENT;
      clipPercent.set(left, top, right, bottom);
      updateClip();
      updateBounds();
      invalidateSelf();
    }
  }

  /**
   * Clear the clip region.
   */
  public void clearClip() {
    if (clipMode != CLIP_NONE) {
      clipMode = CLIP_NONE;
      updateClip();
      updateBounds();
      invalidateSelf();
    }
  }

  private void updateClip() {
    Drawable drawable = getDrawable();
    if (drawable == null) {
      clip.setEmpty();
      return;
    }

    switch (clipMode) {
      case CLIP_NONE:
        clip.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        break;
      case CLIP_RECT:
        if (clipRect.isEmpty()) {
          clip.setEmpty();
        } else {
          clip.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
          if (!clip.intersect(clipRect)) {
            clip.setEmpty();
          }
        }
        break;
      case CLIP_PERCENT:
        if (clipPercent.isEmpty()) {
          clip.setEmpty();
        } else {
          clip.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
          if (!clip.intersect(
              (int) (clipPercent.left * drawable.getIntrinsicWidth()),
              (int) (clipPercent.top * drawable.getIntrinsicHeight()),
              (int) (clipPercent.right * drawable.getIntrinsicWidth()),
              (int) (clipPercent.bottom * drawable.getIntrinsicHeight()))) {
            clip.setEmpty();
          }
        }
        break;
    }
  }

  private void updateBounds() {
    Drawable drawable = getDrawable();
    if (drawable == null) {
      return;
    }

    if (clip.isEmpty()) {
      drawable.setBounds(clip);
    } else {
      // Map rect, rect -> getBounds(), (0, 0, dWidth, dHeight) -> dBounds
      Rect bounds = getBounds();
      float scaleX = (float) bounds.width() / (float) clip.width();
      float scaleY = (float) bounds.height() / (float) clip.height();
      drawable.setBounds(
          bounds.left + (int) (-clip.left * scaleX),
          bounds.top + (int) (-clip.top * scaleY),
          bounds.left + (int) ((drawable.getIntrinsicWidth() - clip.left) * scaleX),
          bounds.top + (int) ((drawable.getIntrinsicHeight() - clip.top) * scaleY)
      );
    }
  }

  protected Rect getClip() {
    return clip;
  }

  @Override
  public void onSetWrappedDrawable(@Nullable Drawable oldDrawable, @Nullable Drawable newDrawable) {
    updateClip();
    updateBounds();
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    updateClip();
    updateBounds();
  }

  @Override
  public int getIntrinsicWidth() {
    return clip.width();
  }

  @Override
  public int getIntrinsicHeight() {
    return clip.height();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    int saved = canvas.save();
    // only the clipped region is visible
    canvas.clipRect(getBounds());
    super.draw(canvas);
    canvas.restoreToCount(saved);
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    updateClip();
    updateBounds();
    super.invalidateDrawable(who);
  }
}
