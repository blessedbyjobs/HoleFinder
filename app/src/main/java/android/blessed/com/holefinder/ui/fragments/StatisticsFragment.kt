package android.blessed.com.holefinder.ui.fragments

import android.blessed.com.holefinder.R
import android.blessed.com.holefinder.presenters.StatisticsPresenter
import android.blessed.com.holefinder.services.TrackingService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.lang.Exception
import java.util.*

class StatisticsFragment : MvpAppCompatFragment(), StatisticsView {
    // Presenter
    @InjectPresenter
    lateinit var statisticsPresenter: StatisticsPresenter
    // Presenter

    // SavedInstance
    private var startWritingData : Boolean = false
    private lateinit var startService : Intent
    private var sb : StringBuilder? = StringBuilder()
    // SavedInstance

    // Receiving
    private lateinit var dataReceiver : BroadcastReceiver
    private var dataIntentFilter : IntentFilter = IntentFilter()
    // Receiving

    // UI
    private lateinit var timer : Timer
    // UI

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            startService = savedInstanceState.getParcelable("ActivityIntent")!!
            startWritingData = savedInstanceState.getBoolean("StartWritingData")
            sb = savedInstanceState.getCharSequence("DataText") as StringBuilder?
        } else {
            startService = Intent(activity, TrackingService::class.java)
        }

        setReceiver()

        start_recording_button.setOnClickListener {
            statisticsPresenter.tracking(startService, startWritingData)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // сохраняем информацию, чтобы иметь возможность остановить службу, когда activity уничтожена
        outState.putBoolean("StartWritingData", startWritingData)
        outState.putParcelable("ActivityIntent", startService)
        outState.putCharSequence("DataText", sb)
    }

    private fun setReceiver() {
        // устанавливаем настройки приема данных от службы
        dataReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                sb?.setLength(0)
                sb?.append(intent.getStringExtra("PASSED_DATA"))
            }
        }

        dataIntentFilter.addAction(TrackingService.MY_ACTION)
        activity?.registerReceiver(dataReceiver, dataIntentFilter)
    }

    override fun onResume() {
        super.onResume()
        try {
            activity?.registerReceiver(dataReceiver, dataIntentFilter)

            timer = Timer()
            val task = object : TimerTask() {
                override fun run() {
                    // обновляем информацию
                }
            }
            // задаем частоту обновления
            timer.schedule(task, 0, 100)
        } catch (e : Exception) {

        }
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }

    override fun onStop() {
        super.onStop()
        try {
            activity?.unregisterReceiver(dataReceiver)
        } catch (e: Exception) {
            Log.e("Error", e.toString())
        }

    }

    override fun showGPSToast() {
        Toast.makeText(activity, resources.getString(R.string.gps_error_toast), Toast.LENGTH_SHORT).show()
    }

    override fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Dexter.withActivity(activity)
                    .withPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_NETWORK_STATE,
                            android.Manifest.permission.FOREGROUND_SERVICE)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            statisticsPresenter.setPermission(true)
                        }

                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {

                        }

                    }
                    ).check()
        } else {
            Dexter.withActivity(activity)
                    .withPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_NETWORK_STATE)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            statisticsPresenter.setPermission(true)
                        }

                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {

                        }

                    }
                    ).check()
        }
    }

    override fun isGPSEnabled() {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        statisticsPresenter.setGPS(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
    }

    override fun setStartRecording() {
        // нажата кнопка "Старт"
        start_recording_button.background = resources.getDrawable(R.drawable.round_button_active)
        start_recording_button.text = "СТОП"
        startWritingData = true
    }

    override fun setStopRecording() {
        // нажата кнопка "Стоп"
        start_recording_button.background = resources.getDrawable(R.drawable.round_button_inactive)
        start_recording_button.text = "НАЧАЛО ЗАПИСИ"
        startWritingData = false
    }
}
