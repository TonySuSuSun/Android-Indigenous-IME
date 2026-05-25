package com.litekite.ime.util

import android.content.res.TypedArray
import android.util.TypedValue
import kotlin.math.roundToInt

object DimensUtil {

    fun TypedArray.getDimensionOrFraction(index: Int, base: Int, defVal: Int): Int {
        val value = peekValue(index) ?: return defVal
        if (value.type == TypedValue.TYPE_DIMENSION) {
            return getDimensionPixelOffset(index, defVal)
        } else if (value.type == TypedValue.TYPE_FRACTION) {
            // Round it to avoid values like 47.9999 from getting truncated
            return getFraction(index, base, base, defVal.toFloat()).roundToInt()
        }
        return defVal
    }
}
