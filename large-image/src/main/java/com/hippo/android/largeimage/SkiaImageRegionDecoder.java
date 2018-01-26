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
 * Created by Hippo on 2018/1/15.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

/**
 * A ImageRegionDecoder with {@link BitmapRegionDecoder}.
 */
public class SkiaImageRegionDecoder extends ImageRegionDecoder {

  private static final String LOG_TAG = "SkiaImageRegionDecoder";

  private final BitmapRegionDecoder decoder;
  private final int width;
  private final int height;

  private SkiaImageRegionDecoder(BitmapRegionDecoder decoder) {
    this.decoder = decoder;
    this.width = decoder.getWidth();
    this.height = decoder.getHeight();
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Nullable
  @Override
  public Bitmap decode(Rect rect, int sample) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = sample;
    try {
      return decoder.decodeRegion(rect, options);
    } catch (IllegalStateException | IllegalArgumentException e) {
      Log.e(LOG_TAG, "Can't decode region " + rect + " at sample " + sample, e);
      return null;
    }
  }

  @Override
  public void recycle(Bitmap bitmap) {
    bitmap.recycle();
  }

  @Override
  public void recycle() {
    super.recycle();
    decoder.recycle();
  }

  @Nullable
  public static SkiaImageRegionDecoder newInstance(InputStream is) {
    BitmapRegionDecoder bitmapDecoder;

    try {
      bitmapDecoder = BitmapRegionDecoder.newInstance(is, false);
    } catch (IOException e) {
      Log.e(LOG_TAG, "Can't create BitmapRegionDecoder", e);
      return null;
    }

    if (bitmapDecoder == null) {
      Log.e(LOG_TAG, "Can't create BitmapRegionDecoder");
      return null;
    }

    SkiaImageRegionDecoder decoder = new SkiaImageRegionDecoder(bitmapDecoder);
    decoder.generatePreview();

    if (decoder.getPreview() == null) {
      Log.e(LOG_TAG, "Can't create generate preview from SkiaImageRegionDecoder");
      decoder.recycle();
      return null;
    }

    return decoder;
  }
}
