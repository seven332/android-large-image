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

class Utils {

  /**
   * Returns the input value a clamped to the range [bound1, bound2] if bound2 &gt;= bound1,
   * otherwise [bound2, bound1].
   *
   * @param a the input
   * @param bound1 the first bound
   * @param bound2 the second bound
   * @return the result which has been clamped
   */
  public static float clamp(float a, float bound1, float bound2) {
    if (bound2 >= bound1) {
      if (a > bound2) return bound2;
      if (a < bound1) return bound1;
    } else {
      if (a > bound1) return bound1;
      if (a < bound2) return bound2;
    }
    return a;
  }

  /**
   * Returns the result which is the smallest (closest to zero)
   * {@code int} value that is greater than or equal to the input
   * and is power of 2. n is treated as unsigned int.
   *
   * @param n the input
   * @return the result
   */
  public static int nextPow2(int n) {
    if (n == 0) return 1;
    n -= 1;
    n |= n >> 1;
    n |= n >> 2;
    n |= n >> 4;
    n |= n >> 8;
    n |= n >> 16;
    return n + 1;
  }

  /**
   * Returns the result which is the biggest (closest to positive infinity)
   * {@code long} value that is smaller than or equal to the input
   * and is power of 2. n is treated as unsigned int.
   * <p>
   * Return 0 if {@code n} is 0.
   *
   * @param n the input
   * @return the result
   */
  public static int prevPow2(int n) {
    n |= n >> 1;
    n |= n >> 2;
    n |= n >> 4;
    n |= n >> 8;
    n |= n >> 16;
    return n - (n >>> 1);
  }

  /**
   * Returns the largest (closest to positive infinity)
   * {@code int} value that is less than or equal to the algebraic quotient.
   *
   * @param a the dividend
   * @param b the divisor
   * @return the quotient
   */
  public static int floorDiv(int a, int b) {
    int r = a / b;
    // if the signs are different and modulo not zero, round down
    if ((a ^ b) < 0 && (r * b != a)) {
      --r;
    }
    return r;
  }

  /**
   * Returns the smallest (closest to positive infinity)
   * {@code int} value that is greater than or equal to the algebraic quotient.
   *
   * @param a the dividend
   * @param b the divisor
   * @return the quotient
   */
  public static int ceilDiv(int a, int b) {
    return -floorDiv(-a, b);
  }
}
