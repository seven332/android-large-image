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

import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class LargeDrawable extends Drawable {

  /**
   * Only the images, which the width or height is larger than it, are large image
   */
  public static final int LARGE_IMAGE_THRESHOLD = 1024;

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

  public abstract void setScale(float scale);

  public abstract void setScaleType(@ScaleType int scaleType);

  public abstract void setStartPosition(@StartPosition int startPosition);

  public void scroll(float dx, float dy) {
    scroll(dx, dy, null);
  }

  public abstract void scroll(float dx, float dy, @Nullable float[] remain);

  public abstract void scale(float x, float y, float factor);
}
