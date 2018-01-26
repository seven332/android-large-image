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

package com.hippo.android.largeimage.demo;

/*
 * Created by Hippo on 2018/1/24.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.hippo.android.gesture.GestureRecognizer;
import com.hippo.android.largeimage.SkiaImageRegionDecoder;
import com.hippo.android.largeimage.TiledDrawable;
import com.hippo.android.largeimage.TransformableDrawable;

public class GestureImageView extends AppCompatImageView {

  private GestureRecognizer gestureRecognizer;
  private int resId;

  public GestureImageView(Context context) {
    super(context);
    init(context);
  }

  public GestureImageView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (resId != 0) {
      bindDrawable(resId);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (resId != 0) {
      unbindDrawable();
    }
  }

  private void bindDrawable(int resId) {
    SkiaImageRegionDecoder decoder = SkiaImageRegionDecoder.newInstance(getResources().openRawResource(resId));
    if (decoder != null) {
      TiledDrawable drawable = new TiledDrawable(decoder, AsyncTask.SERIAL_EXECUTOR, true);
      TransformableDrawable tDrawable = new TransformableDrawable(drawable);
      tDrawable.setVisibleRect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
      setImageDrawable(tDrawable);
    }
  }

  private void unbindDrawable() {
    Drawable drawable = getDrawable();
    if (drawable instanceof TransformableDrawable) {
      drawable = ((TransformableDrawable) drawable).getDrawable();
      if (drawable instanceof TiledDrawable) {
        ((TiledDrawable) drawable).recycle();
      }
    }
    setImageDrawable(null);
  }

  public void load(int resId) {
    int oldResId = this.resId;
    this.resId = resId;
    if (ViewCompat.isAttachedToWindow(this)) {
      if (oldResId != 0) {
        unbindDrawable();
      }
      if (resId != 0) {
        bindDrawable(resId);
      }
    }
  }

  private void init(Context context) {
    gestureRecognizer = new GestureRecognizer(context, new GestureRecognizer.SimpleOnGestureListener() {
      @Override
      public void onScroll(float dx, float dy, float totalX, float totalY, float x, float y) {
        Drawable drawable = getDrawable();
        if (drawable instanceof TransformableDrawable) {
          ((TransformableDrawable) drawable).scroll(dx, dy, null);
        }
      }
      @Override
      public void onScale(float x, float y, float scale) {
        Drawable drawable = getDrawable();
        if (drawable instanceof TransformableDrawable) {
          ((TransformableDrawable) drawable).scale(x, y, scale, null);
        }
      }
    });
    gestureRecognizer.setScaleEnabled(true);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    gestureRecognizer.onTouchEvent(event);
    return true;
  }
}
