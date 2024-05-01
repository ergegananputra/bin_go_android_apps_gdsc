package com.gdsc.bingo.ui.pinpoint.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import coil.Coil
import coil.request.ImageRequest
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentSearchMapsBinding
import com.gdsc.bingo.model.BinLocation
import com.gdsc.bingo.model.nearby.ModelResults
import com.gdsc.bingo.model.utils.CustomInfoWindowGoogleMap
import com.gdsc.bingo.ui.pinpoint.PinPointActivity
import com.gdsc.bingo.ui.pinpoint.search.viewmodel.SearchMapsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.firestore.GeoPoint
import java.util.Locale


class SearchMapsFragment : Fragment(), OnMapReadyCallback {

    private val binding by lazy {
        FragmentSearchMapsBinding.inflate(layoutInflater)
    }

    private val bindingPinPointActivity by lazy {
        (requireActivity() as PinPointActivity).binding
    }

    private val desiredWidth = 144
    private val desiredHeight = 144

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
    private var binLocationList = ArrayList<BinLocation>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        selectedLocationText = getString(R.string.belum_ada_lokasi_terpilih)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeaderCard()

        locationTextView = binding.pinPointTextViewLocationAddress
        locationTextView.isSelected = true

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (isLocationEnabled()) {
            requestLocation()
        } else {
            locationTextView.text = "Izinkan lokasi Anda untuk menemukan TPU terdekat"
        }

        val mapFragment = childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        getLastLocation()

        binding.componentButtonNavigate.setOnClickListener {
            // Panggil metode untuk membuka Google Maps dengan arah dari lokasi yang dipilih
            openGoogleMapsDirections()
        }

        setupSearchBar()

        setupToggleButton()

    }

    private fun setupSearchBar() {

        with(bindingPinPointActivity) {
            pinPointOpenSearchButton.setOnClickListener {
                it.visibility = View.GONE
                pinpointHeaderButtonBack.visibility = View.GONE
                pinPointTextViewTitle.visibility = View.GONE

                pinPointTextInputLayoutSearch.visibility = View.VISIBLE
                pinPointTextInputLayoutSearch.editText?.requestFocus()

                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(pinPointTextInputLayoutSearch.editText, InputMethodManager.SHOW_IMPLICIT)
            }

            pinPointTextInputLayoutSearch.setEndIconOnClickListener {
                val text = pinPointTextInputLayoutSearch.editText?.text.toString()
                if (text.isNotEmpty()) {
                    pinPointTextInputLayoutSearch.editText?.setText("")
                } else {
                    pinPointOpenSearchButton.visibility = View.VISIBLE
                    pinpointHeaderButtonBack.visibility = View.VISIBLE
                    pinPointTextViewTitle.visibility = View.VISIBLE

                    pinPointTextInputLayoutSearch.visibility = View.GONE
                    pinPointTextInputLayoutSearch.editText?.setText("")

                    pinPointTextInputLayoutSearch.editText?.clearFocus()
                    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(pinPointTextInputLayoutSearch.editText?.windowToken, 0)
                }
            }

            pinPointTextInputLayoutSearch.editText?.addTextChangedListener(object : TextWatcher {
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
                        getMarker(binLocationList)
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                    // Tidak ada aksi yang diperlukan setelah teks berubah
                }
            })


        }
    }

    private fun setupToggleButton() {
        binding.binPointButtonToggleGroup.addOnButtonCheckedListener { toggleGroup, id, isChecked ->
            if (isChecked) {
                when (id) {
                    binding.pinPointButtonBinLocator.id -> {
                        // Tampilkan semua marker bin
                        val filteredResults = binLocationList.filterNot {
                            it.type == BinLocation.BinTypeCategory.REPORT.fieldName
                        }
                        getMarker(filteredResults as ArrayList<BinLocation>)
                    }
                    binding.pinPointButtonBinReport.id -> {
                        // Tampilkan semua marker report
                        val filteredResults = binLocationList.filter {
                            it.type == BinLocation.BinTypeCategory.REPORT.fieldName
                        }
                        getMarker(filteredResults as ArrayList<BinLocation>)
                    }
                    binding.pinPointButtonSemua.id -> {
                        // Tampilkan semua marker
                        getMarker(binLocationList)
                    }
                }
            }
        }
    }

    private fun setFirstLocation() {
        try {
            val latitude = (requireActivity() as PinPointActivity).args.latitude
            val longitude = (requireActivity() as PinPointActivity).args.longitude

            if (latitude != null && longitude != null) {
                val vicinity = GeoPoint(latitude.toDouble(), longitude.toDouble())
                Log.i("Location", "Location found: $latitude, $longitude")

                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(vicinity.latitude, vicinity.longitude),
                    DEFAULT_ZOOM
                ))

                // Find the marker by its position and open its info window
                val marker = markerList.find {
                    it.position.latitude == vicinity.latitude
                            && it.position.longitude == vicinity.longitude
                }
                marker?.showInfoWindow()
                performMarkerClickAction(marker)

            } else {
                Log.i("Location", "Location not found: $latitude, $longitude")
            }
        } catch (e: Exception) {
            Log.e("Location", "Error: ${e.message}")
        }

    }

    private fun performMarkerClickAction(marker: Marker?) : Boolean{
        // Ambil tag dari marker yang diklik
        Log.i("MarkerClicked", "Marker clicked")
        val markerInfoRaw = marker?.tag as? ModelResults ?: return false // Kembalikan false agar default behavior dari marker juga berlaku

        val markerInfo = BinLocation(
            referencePath = null,
            name = markerInfoRaw.name,
            address = markerInfoRaw.vicinity,
            location = GeoPoint(marker.position.latitude, marker.position.longitude),
            additionalInfo = mapOf(
                "place_id" to markerInfoRaw.placeId
            ),
            isOpen = markerInfoRaw.isOpen,
            rating = markerInfoRaw.rating
        )

        // Perbarui teks pada TextView
        binding.searchMapsTextViewLokasiTerpilih.text = markerInfo.name
        binding.searchMapsTextViewAddressLokasiTerpilih.text = markerInfo.address

        // NOTE: Animasi disini
        TransitionManager.beginDelayedTransition(binding.pinPointActivityRootLayout)
        binding.searchMapsGroupMarkerInfo.visibility = View.VISIBLE
        binding.pinPointButtonMyLocationCardView.visibility = View.GONE

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

        val ratingTextString =  "Rating: ${markerInfo.rating ?: "Rating tidak tersedia"}"
        binding.searchMapsTextViewLokasiRating.text = ratingTextString

        marker.showInfoWindow()

        return true
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
        binding.pinPointButtonMyLocationCardView.setOnClickListener {
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

    private fun setViewModel() {
        ViewModelMaps = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[SearchMapsViewModel::class.java]
        ViewModelMaps.setMarkerLocation(strCurrentLocation)
        ViewModelMaps.modelResultsMutableLiveData.observe(requireActivity()) { newBinLocationList ->
            if (newBinLocationList.isEmpty()) {
                requestLocationPermission()
                return@observe
            }

            binLocationList.clear()
            binLocationList.addAll(newBinLocationList)
            getMarker(newBinLocationList)
        }
    }

    private fun getMarker(binLocationList: ArrayList<BinLocation>) {
        // Hapus semua marker yang ada sebelum menambahkan yang baru
        googleMap?.clear()
        markerList.clear()

        for (binLocation in binLocationList) {
            if (binLocation.location?.latitude == null || binLocation.location?.longitude == null) {
                continue
            }

            val latLngMarker = LatLng(binLocation.location!!.latitude, binLocation.location!!.longitude)

            val customInfoWindow = CustomInfoWindowGoogleMap(requireContext())
            googleMap?.setInfoWindowAdapter(customInfoWindow)

            val iconResId = when (binLocation.type) {
                "bin" -> R.drawable.ic_custom_marker_maps // Gunakan ikon untuk marker bin
                "report" -> R.drawable.ic_custom_report_marker_maps // Gunakan ikon untuk marker report
                else -> R.drawable.ic_custom_marker_maps // Default: Gunakan ikon default
            }

            val request = ImageRequest.Builder(requireContext())
                .data(iconResId)
                .target { drawable ->
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    val scaleBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, false)
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
                    val markerOptions = MarkerOptions()
                        .position(latLngMarker)
                        .icon(bitmapDescriptor)

                    val marker = googleMap?.addMarker(markerOptions)
                    marker?.tag = ModelResults().apply {
                        name = binLocation.name!!
                        placeId = binLocation.additionalInfo?.get("place_id").toString()
                        vicinity = binLocation.address!!
                        rating = binLocation.rating ?: 0.0
                    }
                    marker?.showInfoWindow()
                    marker?.let { markerList.add(it) }
                }
                .build()

            val imageLoader = Coil.imageLoader(requireContext())
            imageLoader.enqueue(request)
        }

        // Tambahkan listener pada marker di luar loop
        googleMap?.setOnMarkerClickListener { marker ->

            return@setOnMarkerClickListener performMarkerClickAction(marker)
        }

        googleMap?.setOnMapClickListener {
            TransitionManager.beginDelayedTransition(binding.pinPointActivityRootLayout)
            binding.searchMapsGroupMarkerInfo.visibility = View.GONE
            binding.pinPointButtonMyLocationCardView.visibility = View.VISIBLE
        }

        setFirstLocation()
    }

    private fun performSearch(searchText: String) {
        // Filter modelResultsArrayList berdasarkan teks pencarian
        val filteredResults = binLocationList.filter {
            val name = it.name?.contains(searchText, ignoreCase = true) ?: false // not in the list
            val address = it.address?.contains(searchText, ignoreCase = true) ?: false // not in the list

            name || address
        }

        // Jika ditemukan hasil pencarian yang sesuai
        if (filteredResults.isNotEmpty()) {
            // Ambil lokasi dari hasil pencarian pertama
            val firstResult = filteredResults[0]

            val latLng = LatLng(firstResult.location!!.latitude, firstResult.location!!.longitude)

            // Atur kamera untuk melakukan zoom ke lokasi hasil pencarian pertama
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        }

        // Hapus semua marker yang ada sebelum menambahkan yang baru
        googleMap?.clear()

        // Tambahkan marker untuk hasil pencarian yang sesuai
        for (binLocation in filteredResults) {
            if (binLocation.location?.latitude == null || binLocation.location?.longitude == null) {
                continue
            }

            val latLngMarker = LatLng(binLocation.location!!.latitude, binLocation.location!!.longitude)

            val customInfoWindow = CustomInfoWindowGoogleMap(requireContext())
            googleMap?.setInfoWindowAdapter(customInfoWindow)

            val iconResId = when (binLocation.type) {
                "bin" -> R.drawable.ic_custom_marker_maps // Gunakan ikon untuk marker bin
                "report" -> R.drawable.ic_custom_report_marker_maps // Gunakan ikon untuk marker report
                else -> R.drawable.ic_custom_marker_maps // Default: Gunakan ikon default
            }

            val request = ImageRequest.Builder(requireContext())
                .data(iconResId)
                .target { drawable ->
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    val scaleBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, false)
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
                    val markerOptions = MarkerOptions()
                        .position(latLngMarker)
                        .icon(bitmapDescriptor)

                    val marker = googleMap?.addMarker(markerOptions)
                    marker?.tag = ModelResults().apply {
                        name = binLocation.name!!
                        placeId = binLocation.additionalInfo?.get("place_id").toString()
                        vicinity = binLocation.address!!
                        rating = binLocation.rating ?: 0.0
                    }
                    marker?.showInfoWindow()
                    marker?.let { markerList.add(it) }
                }
                .build()

            val imageLoader = Coil.imageLoader(requireContext())
            imageLoader.enqueue(request)
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15f
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}