package android.blessed.com.holefinder.presenters

import android.blessed.com.holefinder.global.Application
import android.blessed.com.holefinder.ui.fragments.StatisticsView
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter

@InjectViewState
class StatisticsPresenter : MvpPresenter<StatisticsView>(), StatisticsPresenterMVP {
    private var isGPSEnabled : Boolean = false
    private var isPermissionGranted : Boolean = false

    private var context: Context = Application.sInstance

    override fun tracking(intent: Intent, recordingStarted: Boolean) {
        if (!recordingStarted) {
            viewState.isGPSEnabled()
            viewState.requestPermissions()
            if (isPermissionGranted) {
                if (isGPSEnabled) {
                    viewState.setStartRecording()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(context, intent)
                    } else {
                        context.startService(intent)
                    }
                }
            }
        } else {
            viewState.setStopRecording()
            context.stopService(intent)
        }
    }

    override fun setGPS(value: Boolean) {
        isGPSEnabled = value
    }

    override fun setPermission(value: Boolean) {
        isPermissionGranted = value
    }
}