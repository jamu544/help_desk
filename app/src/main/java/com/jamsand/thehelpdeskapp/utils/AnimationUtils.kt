package com.jamsand.thehelpdeskapp.utils

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator

//display polyline
object AnimationUtils {
    fun polyAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(0, 100)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 4000
        return valueAnimator
    }

    //
    fun carAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 3000
        valueAnimator.interpolator = LinearInterpolator()
        return valueAnimator
    }
}
