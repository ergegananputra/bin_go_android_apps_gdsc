package com.gdsc.bingo.ui.pop_up

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.gdsc.bingo.R
import com.gdsc.bingo.ui.komunitas.KomunitasFragment

class SuccesReportPopUp : AppCompatActivity() {
    companion object {
        const val ANIMATION_TIME: Long = 2100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_succes_report_pop_up)

        Handler(this.mainLooper).postDelayed({
            startActivity(Intent(this@SuccesReportPopUp, KomunitasFragment::class.java))
            finish()
        }, ANIMATION_TIME)
    }
}