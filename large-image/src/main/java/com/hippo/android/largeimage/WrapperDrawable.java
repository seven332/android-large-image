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
 * Created by Hippo on 2018/1/25.
 */

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Drawable container with only one child element.
 */
class WrapperDrawable extends Drawable implements Drawable.Callback {

  @Nullable
  private Drawable drawable;

  public final void setDrawable(@Nullable Drawable drawable) {
    if (this.drawable == drawable) {
      return;
    }

    if (this.drawable != null) {
      this.drawable.setCallback(null);
    }
    if (drawable != null) {
      drawable.setCallback(this);
    }

    onSetWrappedDrawable(this.drawable, drawable);

    this.drawable = drawable;

    invalidateSelf();
  }

  @Nullable
  public Drawable getDrawable() {
    return drawable;
  }

  public void onSetWrappedDrawable(@Nullable Drawable oldDrawable, @Nullable Drawable newDrawable) {
    if (newDrawable != null) {
      newDrawable.setBounds(getBounds());
    }
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
    if (drawable != null) {
      drawable.setBounds(bounds);
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (drawable != null) {
      drawable.draw(canvas);
    }
  }

  @Override
  public void setAlpha(int alpha) {
    if (drawable != null) {
      drawable.setAlpha(alpha);
    }
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    if (drawable != null) {
      drawable.setColorFilter(colorFilter);
    }
  }

  @Override
  public int getOpacity() {
    if (drawable != null) {
      return drawable.getOpacity();
    } else {
      return PixelFormat.TRANSPARENT;
    }
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
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
