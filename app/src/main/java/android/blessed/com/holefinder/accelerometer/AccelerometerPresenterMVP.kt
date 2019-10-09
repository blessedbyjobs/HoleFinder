package android.blessed.com.holefinder.accelerometer

import android.content.Intent

interface AccelerometerPresenterMVP {
    fun startButtonClicked(intent: Intent)
    fun stopButtonClicked(intent: Intent)

    fun setGPS(value : Boolean)
    fun setPermission(value : Boolean)

    fun setClearFile(value : Boolean)
}