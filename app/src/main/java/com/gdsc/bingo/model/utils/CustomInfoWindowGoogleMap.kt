package com.gdsc.bingo.model.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.model.nearby.ModelResults
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
class CustomInfoWindowGoogleMap(private val context: Context) : InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        val view = (context as AppCompatActivity)
            .layoutInflater
            .inflate(R.layout.layout_tooltip_marker, null)

        val tvNamaLokasi = view.findViewById<TextView>(R.id.tvNamaLokasi)
        val tvAlamat = view.findViewById<TextView>(R.id.tvAlamat)

        val infoWindowData = marker.tag as? ModelResults

        if (infoWindowData != null) {
            tvNamaLokasi.text = infoWindowData.name
            tvAlamat.text = infoWindowData.vicinity
        } else {
            Toast.makeText(context, "InfoWindow data is null", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}