package android.blessed.com.holefinder.ui.fragments

import android.annotation.SuppressLint
import android.blessed.com.holefinder.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import android.location.Criteria
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager

class RoutsFragment : Fragment() {
    private lateinit var mMapView: MapView
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_routs, container, false)

        mMapView = rootView.findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)

        mMapView.onResume() // немедленное отображение карты

        try {
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMapView.getMapAsync { mMap ->
            googleMap = mMap

            Dexter.withActivity(activity)
                    .withPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(object : MultiplePermissionsListener {
                        @SuppressLint("MissingPermission")
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            // кнопка "Текущее местоположение"
                            googleMap!!.isMyLocationEnabled = true

                            // кнопки "Увеличить-уменьшить"
                            googleMap!!.uiSettings.isZoomControlsEnabled = true

                            // zoom камеры
                            val locationManager = activity!!.getSystemService(LOCATION_SERVICE) as LocationManager?
                            val criteria = Criteria()
                            val bestProvider = locationManager!!.getBestProvider(criteria, true)
                            val location = locationManager.getLastKnownLocation(bestProvider!!)
                            if (location != null) {
                                val currentLocation = LatLng(location.latitude, location.longitude)
                                val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(12f).build()
                                googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {

                        }

                    }
                    ).check()
        }

        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            //Restore the fragment's state here
        }
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}
