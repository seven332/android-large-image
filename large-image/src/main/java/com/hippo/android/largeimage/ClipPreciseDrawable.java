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
 * ClipPreciseDrawable can only clip {@code PreciseDrawable}.
 */
public class ClipPreciseDrawable extends ClipDrawable implements PreciseDrawable {

  private RectF rectF1 = new RectF();

  @Override
  public void onSetWrappedDrawable(@Nullable Drawable oldDrawable, @Nullable Drawable newDrawable) {
    super.onSetWrappedDrawable(oldDrawable, newDrawable);
    if (newDrawable != null && !(newDrawable instanceof PreciseDrawable)) {
      throw new IllegalArgumentException("ClipPreciseDrawable only accepts PreciseDrawable");
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas, @NonNull RectF src, @NonNull RectF dst) {
    Drawable drawable = getDrawable();
    Rect clip = getClip();
    if (drawable != null && !clip.isEmpty()) {
      RectF source = rectF1;
      source.set(src);
      source.offset(clip.left, clip.top);
      ((PreciseDrawable) drawable).draw(canvas, source, dst);
    }
  }
}
