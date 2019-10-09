package android.blessed.com.holefinder.accelerometer

import com.arellomobile.mvp.MvpView

interface AccelerometerView : MvpView {
    fun showInfo()
    fun showGPSToast()
    fun showTrackingInfo(started: Boolean)
    fun switchButtons(startCalculations: Boolean)

    fun requestPermissions()
    fun isGPSEnabled()
}