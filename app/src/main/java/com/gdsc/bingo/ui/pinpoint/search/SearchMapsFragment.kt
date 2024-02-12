package com.gdsc.bingo.ui.pinpoint.search

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentSearchMapsBinding
import com.gdsc.bingo.model.nearby.ModelResults
import com.gdsc.bingo.model.utils.CustomInfoWindowGoogleMap
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList
import com.gdsc.bingo.ui.pinpoint.search.viewmodel.SearchMapsViewModel
import androidx.lifecycle.ViewModelProvider


class SearchMapsFragment : Fragment(), OnMapReadyCallback, SensorEventListener {

    private val binding by lazy {
        FragmentSearchMapsBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor
    private lateinit var magnetometerSensor: Sensor

    private val rotationMatrix = FloatArray(9)
    private val orientationValues = FloatArray(3)
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private var googleMap: GoogleMap? = null

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var fabCompass: FloatingActionButton

    private lateinit var ViewModelMaps: SearchMapsViewModel
    private lateinit var strCurrentLocation: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val fabMyLocation: FloatingActionButton = view.findViewById(R.id.fabMyLocation)

        fabMyLocation.setOnClickListener {
            checkLocationPermission()
        }

        // Panggil getLastLocation() di sini sebelum setViewModel()
        getLastLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap


        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getMarker(SearchMapsViewModel().modelResultsMutableLiveData.value ?: arrayListOf())
        } else {
            requestLocationPermission()
        }

        // Ambil data dari Firestore Database
        firestore.collection("places")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Ambil informasi lokasi dari dokumen
                    val name = document.getString("name")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")

                    // Tambahkan marker ke peta
                    if (name != null && latitude != null && longitude != null) {
                        val location = LatLng(latitude, longitude)
                        googleMap.addMarker(
                            MarkerOptions().position(location).title(name)
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        // Aktifkan lokasi pengguna jika diperlukan
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        googleMap?.isMyLocationEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false // Disable default "My Location" button
        getLastLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastKnownLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLatLng,
                        DEFAULT_ZOOM
                    )
                )
                googleMap?.addMarker(
                    MarkerOptions().position(currentLatLng)
                        .title("Your Current Location")
                )
                // Inisialisasi strCurrentLocation di sini
                strCurrentLocation = "${location.latitude},${location.longitude}"
                // Panggil setViewModel setelah inisialisasi strCurrentLocation
                setViewModel()
            } else {
                requestNewLocation()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocation() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                lastKnownLocation = locationResult.lastLocation!!

                val currentLatLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLatLng,
                        DEFAULT_ZOOM
                    )
                )
                googleMap?.addMarker(
                    MarkerOptions().position(currentLatLng)
                        .title("Your Current Location")
                )

                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isLocationEnabled()) {
                updateMapWithCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Please enable location services", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun updateMapWithCurrentLocation() {
        getLastLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not implemented
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == accelerometerSensor) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor == magnetometerSensor) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationValues)
        val azimuthInRadians = orientationValues[0]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

        updateCompassIcon(azimuthInDegrees)
    }

    private fun updateCompassIcon(azimuthInDegrees: Float) {
        val matrix = Matrix()
        fabCompass.drawable?.let { drawable ->
            matrix.postRotate(-azimuthInDegrees, drawable.intrinsicWidth / 2f, drawable.intrinsicHeight / 2f)
            val rotatedBitmap = Bitmap.createBitmap(
                drawable.toBitmap(),
                0,
                0,
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                matrix,
                true
            )
            val rotatedDrawable = BitmapDrawable(resources, rotatedBitmap)
            rotatedDrawable.setBounds(0, 0, rotatedBitmap.width, rotatedBitmap.height)
            fabCompass.setImageDrawable(rotatedDrawable)
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun setViewModel() {
        ViewModelMaps = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[SearchMapsViewModel::class.java]
        ViewModelMaps.setMarkerLocation(strCurrentLocation)
        ViewModelMaps.getMarkerLocation().observe(this) { modelResults: ArrayList<ModelResults> ->
            if (modelResults.size != 0) {
                //get multiple marker
                getMarker(modelResults)
            } else {
                checkLocationPermission()
            }
        }
    }
    private fun getMarker(modelResultsArrayList: ArrayList<ModelResults>) {
        for (i in modelResultsArrayList.indices) {

            //set LatLong from API
            val latLngMarker = LatLng(
                modelResultsArrayList[i]
                    .modelGeometry
                    .modelLocation
                    .lat, modelResultsArrayList[i]
                    .modelGeometry
                    .modelLocation
                    .lng
            )

            //show Marker
            val latLngResult = LatLng(
                modelResultsArrayList[0]
                    .modelGeometry
                    .modelLocation
                    .lat, modelResultsArrayList[0]
                    .modelGeometry
                    .modelLocation
                    .lng
            )

            val info = ModelResults()
            info.name = modelResultsArrayList[i].name
            info.placeId = modelResultsArrayList[i].placeId
            info.vicinity = modelResultsArrayList[i].vicinity

            val customInfoWindow = CustomInfoWindowGoogleMap(requireContext())
            googleMap?.setInfoWindowAdapter(customInfoWindow)

            val markerOptions = MarkerOptions()
            markerOptions.position(latLngMarker)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

            val marker = googleMap?.addMarker(markerOptions)
            marker?.tag = info
            marker?.showInfoWindow()

            //set position marker
            googleMap?.moveCamera(CameraUpdateFactory.newLatLng(latLngResult))
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latLngResult.latitude,
                        latLngResult.longitude
                    ), 14f
                )
            )
            googleMap?.uiSettings?.setAllGesturesEnabled(true)
            googleMap?.uiSettings?.isZoomGesturesEnabled = true
        }

    }

    companion object {
        private const val DEFAULT_ZOOM = 14f
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}