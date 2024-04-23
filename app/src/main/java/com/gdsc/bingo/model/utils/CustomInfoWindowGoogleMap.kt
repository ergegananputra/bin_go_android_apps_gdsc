package com.gdsc.bingo.model.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.model.nearby.ModelResults
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.google.android.material.textview.MaterialTextView

class CustomInfoWindowGoogleMap(private val context: Context) : InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        Log.i("InfoWindowAdapter", "getInfoWindow")
        val view = (context as AppCompatActivity)
            .layoutInflater
            .inflate(R.layout.layout_tooltip_marker, null)

        val tvNamaLokasi = view.findViewById<MaterialTextView>(R.id.tooltip_text_view_title)

        val infoWindowData = marker.tag as? ModelResults

        if (infoWindowData != null) {
            tvNamaLokasi.text = infoWindowData.name

        } else {
            Toast.makeText(context, "InfoWindow data is null", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}