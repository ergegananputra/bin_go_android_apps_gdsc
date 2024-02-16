package com.gdsc.bingo.ui.pinpoint.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentSearchMapsBinding
import com.gdsc.bingo.model.nearby.ModelResults
import com.gdsc.bingo.model.utils.CustomInfoWindowGoogleMap
import com.gdsc.bingo.ui.pinpoint.search.viewmodel.SearchMapsViewModel
import com.google.android.gms.maps.model.Marker

class SearchMapsFragment : Fragment(), OnMapReadyCallback {

    private val binding by lazy {
        FragmentSearchMapsBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var googleMap: GoogleMap? = null
    private lateinit var ViewModelMaps: SearchMapsViewModel
    private lateinit var strCurrentLocation: String

    private val markerList = mutableListOf<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabMyLocation: FloatingActionButton = binding.fabMyLocation
        fabMyLocation.setOnClickListener {
            showUserLocation()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        getLastLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        } else {
            requestLocationPermission()
        }
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
                strCurrentLocation = "${location.latitude},${location.longitude}"
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
            interval = 10000
            fastestInterval = 5000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                lastKnownLocation = locationResult.lastLocation!!

                // Simpan marker yang ada
                val savedMarkers = mutableListOf<Marker>()
                for (marker in markerList) {
                    savedMarkers.add(marker)
                    marker.isVisible = false // Nonaktifkan semua marker yang disimpan
                }

                val currentLatLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        currentLatLng
                    )
                )

                // Aktifkan kembali marker yang disimpan
                for (marker in savedMarkers) {
                    marker.isVisible = true
                }

                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun showUserLocation() {
        getLastLocation()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
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

    @SuppressLint("FragmentLiveDataObserve")
    private fun setViewModel() {
        ViewModelMaps = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[SearchMapsViewModel::class.java]
        ViewModelMaps.setMarkerLocation(strCurrentLocation)
        ViewModelMaps.getMarkerLocation().observe(this) { modelResults: ArrayList<ModelResults> ->
            if (modelResults.isNotEmpty()) {
                getMarker(modelResults)
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun getMarker(modelResultsArrayList: ArrayList<ModelResults>) {
        // Hapus semua marker yang ada sebelum menambahkan yang baru
        googleMap?.clear()
        markerList.clear()

        for (i in modelResultsArrayList.indices) {
            val latLngMarker = LatLng(
                modelResultsArrayList[i]
                    .modelGeometry
                    .modelLocation
                    .lat, modelResultsArrayList[i]
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            val marker = googleMap?.addMarker(markerOptions)
            marker?.tag = info
            marker?.showInfoWindow()

            // Tambahkan marker ke daftar marker
            marker?.let { markerList.add(it) }
        }
}

    companion object {
        private const val DEFAULT_ZOOM = 15f
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}