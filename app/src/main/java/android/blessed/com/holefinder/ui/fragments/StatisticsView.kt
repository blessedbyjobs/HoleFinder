package android.blessed.com.holefinder.ui.fragments

import com.arellomobile.mvp.MvpView

interface StatisticsView : MvpView {
    fun setStartRecording()
    fun setStopRecording()

    fun showGPSToast()

    fun requestPermissions()
    fun isGPSEnabled()
}