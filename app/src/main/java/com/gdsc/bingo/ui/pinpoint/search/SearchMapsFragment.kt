package com.gdsc.bingo.ui.pinpoint.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentSearchMapsBinding
import com.gdsc.bingo.model.nearby.ModelResults
import com.gdsc.bingo.model.utils.CustomInfoWindowGoogleMap
import com.gdsc.bingo.ui.pinpoint.search.viewmodel.SearchMapsViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textview.MaterialTextView
import java.util.*


class SearchMapsFragment : Fragment(), OnMapReadyCallback {

    private val binding by lazy {
        FragmentSearchMapsBinding.inflate(layoutInflater)
    }

    val desiredWidth = 144
    val desiredHeight = 144

    private lateinit var locationTextView: MaterialTextView
    private var selectedLocationText: String = ""

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var googleMap: GoogleMap? = null
    private lateinit var ViewModelMaps: SearchMapsViewModel
    private lateinit var strCurrentLocation: String

    private val markerList = mutableListOf<Marker>()

    // Properti untuk menyimpan hasil pencarian
    private var modelResultsArrayList = ArrayList<ModelResults>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        selectedLocationText = getString(R.string.belum_ada_lokasi_terpilih)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeaderCard()

        locationTextView = view.findViewById(R.id.pin_point_front_text_view_current_user_location)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (isLocationEnabled()) {
            requestLocation()
        } else {
            locationTextView.text = "Izinkan lokasi Anda untuk menemukan TPU terdekat"
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        getLastLocation()

        // Dapatkan referensi ke TextInputEditText di dalam TextInputLayout
        val searchTextInputEditText = binding.searchMapsTextInputLayoutSearch.editText

        // Tambahkan TextWatcher
        searchTextInputEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak ada aksi yang diperlukan sebelum teks berubah
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Ketika teks berubah, Anda dapat memicu pencarian disini
                val searchText = s.toString()
                if (searchText.isNotEmpty()) {
                    performSearch(searchText)
                } else {
                    // Jika teks pencarian kosong, tampilkan semua marker
                    getMarker(modelResultsArrayList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                // Tidak ada aksi yang diperlukan setelah teks berubah
            }
        })

        val btnNavigateRoute = view.findViewById<Button>(R.id.btn_navigate_route)
        btnNavigateRoute.setOnClickListener {
            // Panggil metode untuk membuka Google Maps dengan arah dari lokasi yang dipilih
            openGoogleMapsDirections()
        }
    }

    private fun openGoogleMapsDirections() {
        // Ambil lokasi terpilih dari teksview
        val selectedLocationText = binding.searchMapsTextViewLokasiTerpilih.text.toString()

        // Pastikan ada lokasi yang terpilih
        if (selectedLocationText.isNotEmpty()) {
            // Buat URI Intent untuk membuka Google Maps dengan arah dari lokasi yang dipilih
            val uri = "https://www.google.com/maps/dir/?api=1&destination=${selectedLocationText}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            // Set flag agar Google Maps dibuka dalam aplikasi yang tersedia
            intent.setPackage("com.google.android.apps.maps")
            // Cek apakah terdapat aplikasi yang bisa menangani intent ini
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                // Buka Google Maps
                startActivity(intent)
            } else {
                // Jika tidak ada aplikasi yang bisa menangani intent ini, tampilkan pesan
                Toast.makeText(requireContext(), "Google Maps tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Jika tidak ada lokasi yang terpilih, tampilkan pesan
            Toast.makeText(requireContext(), "Pilih lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val address = getAddressFromLocation(location)
                locationTextView.text = address ?: "Lokasi tidak ditemukan"
            } else {
                locationTextView.text = "Lokasi tidak ditemukan"
            }
        }
    }

    private fun getAddressFromLocation(location: Location): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val subLocality = address.subLocality
            return subLocality ?: "Lokasi terkini: ${location.latitude}, ${location.longitude}"
        }
        return null
    }

    private fun setupHeaderCard() {
        binding.searchMapsMyLocation.setOnClickListener {
            showUserLocation()
        }
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
                modelResultsArrayList.clear()
                modelResultsArrayList.addAll(modelResults)
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
            info.rating = modelResultsArrayList[i].rating // Menambahkan rating dari model ke info

            val customInfoWindow = CustomInfoWindowGoogleMap(requireContext())
            googleMap?.setInfoWindowAdapter(customInfoWindow)

            Glide.with(requireContext())
                .asBitmap()
                .load(R.drawable.ic_custom_marker_maps)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        val scaledBitmap = Bitmap.createScaledBitmap(resource, desiredWidth, desiredHeight, false)
                        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                        val markerOptions = MarkerOptions()
                            .position(latLngMarker)
                            .icon(bitmapDescriptor)
                        val marker = googleMap?.addMarker(markerOptions)
                        marker?.tag = info
                        marker?.showInfoWindow()
                        marker?.let { markerList.add(it) }
                    }
                })
        }
        // Tambahkan listener pada marker di luar loop
        googleMap?.setOnMarkerClickListener { marker ->
            // Ambil tag dari marker yang diklik
            val markerInfo = marker.tag as? ModelResults
            // Jika tag tidak null
            if (markerInfo != null) {
                // Update teks terpilih dengan informasi dari marker yang diklik
                selectedLocationText = "${markerInfo.name}"
                // Perbarui teks pada TextView
                binding.searchMapsTextViewLokasiTerpilih.text = selectedLocationText
                binding.searchMapsTextViewAddressLokasiTerpilih.text = markerInfo.vicinity

                // NOTE: Animasi disini
                TransitionManager.beginDelayedTransition(binding.searchMapsBottomDialogMainConstraintLayout)
                binding.searchMapsGroupMarkerInfo.visibility = View.VISIBLE

                // Periksa apakah tempat tersebut terbuka atau tutup
                val isOpen = markerInfo.isOpen ?: true // Defaultnya adalah false jika properti isOpen null
                if (isOpen) {
                    // Tempat terbuka, tampilkan chip "Buka" dan sembunyikan chip "Tutup"
                    binding.searchMapsChipBuka.visibility = View.VISIBLE
                    binding.searchMapsChipTutup.visibility = View.GONE
                } else {
                    // Tempat tutup, tampilkan chip "Tutup" dan sembunyikan chip "Buka"
                    binding.searchMapsChipTutup.visibility = View.VISIBLE
                    binding.searchMapsChipBuka.visibility = View.GONE
                }
                // Perbarui rating pada TextView
                binding.searchMapsTextViewLokasiRating.visibility = View.VISIBLE
                binding.searchMapsTextViewLokasiRating.text = "Rating: ${markerInfo.rating ?: "Rating tidak tersedia"}"
            }
            // Kembalikan false agar default behavior dari marker juga berlaku
            false
        }

        googleMap?.setOnMapClickListener {
            TransitionManager.beginDelayedTransition(binding.searchMapsBottomDialogMainConstraintLayout)
            binding.searchMapsGroupMarkerInfo.visibility = View.GONE
        }
    }

    private fun performSearch(searchText: String) {
        // Filter modelResultsArrayList berdasarkan teks pencarian
        val filteredResults = modelResultsArrayList.filter { it.name.contains(searchText, ignoreCase = true) }

        // Jika ditemukan hasil pencarian yang sesuai
        if (filteredResults.isNotEmpty()) {
            // Ambil lokasi dari hasil pencarian pertama
            val firstResult = filteredResults[0]
            val latLng = LatLng(firstResult.modelGeometry.modelLocation.lat, firstResult.modelGeometry.modelLocation.lng)

            // Atur kamera untuk melakukan zoom ke lokasi hasil pencarian pertama
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        }

        // Hapus semua marker yang ada sebelum menambahkan yang baru
        googleMap?.clear()

        // Tambahkan marker untuk hasil pencarian yang sesuai
        for (result in filteredResults) {
            val latLngMarker = LatLng(result.modelGeometry.modelLocation.lat, result.modelGeometry.modelLocation.lng)

            val info = ModelResults()
            info.name = result.name
            info.placeId = result.placeId
            info.vicinity = result.vicinity
            info.rating = result.rating

            val customInfoWindow = CustomInfoWindowGoogleMap(requireContext())
            googleMap?.setInfoWindowAdapter(customInfoWindow)

            Glide.with(requireContext())
                .asBitmap()
                .load(R.drawable.ic_custom_marker_maps)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        val scaledBitmap = Bitmap.createScaledBitmap(resource, desiredWidth, desiredHeight, false)
                        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                        val markerOptions = MarkerOptions()
                            .position(latLngMarker)
                            .icon(bitmapDescriptor)
                        val marker = googleMap?.addMarker(markerOptions)
                        marker?.tag = info
                        marker?.showInfoWindow()
                        marker?.let { markerList.add(it) }
                    }
                })
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15f
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}