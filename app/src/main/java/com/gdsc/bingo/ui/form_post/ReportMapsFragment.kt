package com.gdsc.bingo.ui.form_post

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentReportMapsBinding
import com.gdsc.bingo.ui.form_post.viewmodel.FormPostViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class ReportMapsFragment : Fragment(), OnMapReadyCallback {

    private val binding by lazy {
        FragmentReportMapsBinding.inflate(layoutInflater)
    }

    private val formViewModel by lazy {
        ViewModelProvider(requireActivity())[FormPostViewModel::class.java]
    }

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastKnowLocation: Location

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var vicinity: GeoPoint? = null

    private var animateButtonMyLocationLowerY = 0f

    override fun onMapReady(maps: GoogleMap) {
        this.googleMap = maps

        Log.d("ReportMapsFragment", "onMapReady")
        enableMyLocation()
        setupGoogleMaps()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        (activity as FormPostActivity).apply {
            setToolbarTitle(this@ReportMapsFragment)
            hideToolbar()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.reportMapsCard.visibility = View.GONE

        val mapFragment = childFragmentManager.findFragmentById(R.id.report_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupButtonBack()
        setupButtonSave()
        setupCancelButton()
        setupMyLocationButton()
    }

    private fun setupMyLocationButton() {

        binding.reportMapsButtonMyLocation.setOnClickListener {
            getLastLocation()
        }
    }




    private fun setupGoogleMaps() {
        googleMap.setOnMapLongClickListener { latlng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latlng).title("Lokasi Terpilih"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM))

            Log.i("ReportMapsFragment", "onMapLongClickListener: $latlng")

            this.vicinity = GeoPoint(latlng.latitude, latlng.longitude)
            showCardView()
        }

        lifecycleScope.launch {
            vicinity = withContext(Dispatchers.IO) {
                formViewModel.vicinity.asFlow().firstOrNull()
            }

            if (vicinity != null) {
                googleMap.addMarker(MarkerOptions().position(LatLng(vicinity!!.latitude, vicinity!!.longitude)).title("Lokasi Terpilih"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(vicinity!!.latitude, vicinity!!.longitude), DEFAULT_ZOOM))
            }
        }
    }

    private fun setupCancelButton() {
        binding.reportMapsButtonCancel.setOnClickListener {
            googleMap.clear()
            hideCardView()
        }
    }

    private fun showCardView() {

        val startSet = ConstraintSet()
        startSet.clone(binding.reportMapsRoot)
        startSet.setVisibility(R.id.report_maps_card, ConstraintSet.GONE)

        val endSet = ConstraintSet()
        endSet.clone(requireContext(), R.layout.fragment_report_maps)

        val transition = AutoTransition()
        transition.duration = 300

        TransitionManager.beginDelayedTransition(binding.reportMapsRoot, transition)
        endSet.applyTo(binding.reportMapsRoot)

        val coordinate = "Koordinat: ${vicinity?.latitude}, ${vicinity?.longitude}"
        binding.reportMapsTextCoordinate.text = coordinate
    }

    private fun hideCardView() {

        val startSet = ConstraintSet()
        startSet.clone(binding.reportMapsRoot)

        val endSet = ConstraintSet()
        endSet.clone(requireContext(), R.layout.fragment_report_maps)
        endSet.setVisibility(R.id.report_maps_card, ConstraintSet.GONE)

        val transition = AutoTransition()
        transition.duration = 300

        TransitionManager.beginDelayedTransition(binding.reportMapsRoot, transition)
        endSet.applyTo(binding.reportMapsRoot)
    }


    private fun setupButtonSave() {
        binding.reportMapsButtonSimpan.setOnClickListener {
            lifecycleScope.launch {
                formViewModel.setVicinity(vicinity)

                try {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(
                            vicinity!!.latitude,
                            vicinity!!.longitude,
                            1
                        ) { addresses ->
                            if (addresses.isNotEmpty()) {
                                val address: String = addresses[0].getAddressLine(0)
                                Log.i("ReportMapsFragment", "onMapLongClickListener: $address")


                                formViewModel.address = address
                                try {
                                    findNavController().navigateUp() // EXIT
                                } catch (e: Exception) {
                                    (requireActivity() as FormPostActivity).formNavController.navigateUp()
                                }
                            }
                        }


                    } else {

                        val addresses = withContext(Dispatchers.IO) {
                            geocoder.getFromLocation(
                                vicinity!!.latitude,
                                vicinity!!.longitude,
                                1
                            )
                        }

                        if (addresses != null) {
                            if (addresses.isNotEmpty()) {
                                val address: String = addresses[0].getAddressLine(0)
                                Log.i("ReportMapsFragment", "onMapLongClickListener: $address")

                                formViewModel.address = address
                                findNavController().navigateUp() // EXIT
                            }
                        }

                    }



                } catch (e: IOException) {

                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupButtonBack() {
        binding.reportMapsButtonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings?.isMyLocationButtonEnabled = false
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastKnowLocation = location
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        com.google.android.gms.maps.model.LatLng(
                            lastKnowLocation.latitude,
                            lastKnowLocation.longitude
                        ), DEFAULT_ZOOM
                    )
                )
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
                lastKnowLocation = locationResult.lastLocation!!


                val currentLatLng = LatLng(lastKnowLocation.latitude, lastKnowLocation.longitude)
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        currentLatLng
                    )
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

    companion object {
        private const val DEFAULT_ZOOM = 15f
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}