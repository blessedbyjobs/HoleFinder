package android.blessed.com.holefinder.presenters

import android.content.Intent

interface StatisticsPresenterMVP {
    fun tracking(intent: Intent, recordingStarted : Boolean)

    fun setGPS(value : Boolean)
    fun setPermission(value : Boolean)
}