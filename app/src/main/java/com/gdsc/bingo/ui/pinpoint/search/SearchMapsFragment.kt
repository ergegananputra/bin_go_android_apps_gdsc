package com.gdsc.bingo.ui.pinpoint.search

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentSearchMapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SearchMapsFragment : Fragment() {

    private val binding by lazy {
        FragmentSearchMapsBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        // Inisialisasi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Cek izin lokasi
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Mendapatkan lokasi terakhir
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lastKnownLocation = location

                    // Menyesuaikan kamera ke lokasi pengguna
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLatLng,
                            DEFAULT_ZOOM
                        )
                    )
                    // Menambahkan marker ke lokasi pengguna
                    googleMap.addMarker(
                        MarkerOptions().position(currentLatLng)
                            .title("Your Current Location")
                    )
                }
            }
        } else {
            // Jika izin lokasi tidak diberikan, minta izin
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        val fabCompass: FloatingActionButton = view.findViewById(R.id.fabCompass)
        val fabMyLocation: FloatingActionButton = view.findViewById(R.id.fabMyLocation)

        fabCompass.setOnClickListener {
            TODO("Not yet implemented")
            // Implementasikan aksi yang diambil saat FAB Compass diklik
            // Misalnya, arahkan peta ke utara atau lakukan operasi kompas
            // ...

        }

        fabMyLocation.setOnClickListener {
            TODO("Not yet implemented")
            // Implementasikan aksi yang diambil saat FAB MyLocation diklik
            // Misalnya, arahkan peta ke lokasi pengguna saat ini
            // ...

        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15f
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}