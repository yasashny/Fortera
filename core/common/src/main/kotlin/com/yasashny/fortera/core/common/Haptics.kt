package com.yasashny.fortera.core.common

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Source of haptic feedback. Exposed as an interface so UI code doesn't depend on Android
 * vibrator APIs directly and can be tested with a no-op / recording impl.
 */
interface Haptics {
    fun click()
}

internal class AndroidHaptics(context: Context) : Haptics {

    private val appContext = context.applicationContext

    override fun click() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.cancel()
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
    }
}
