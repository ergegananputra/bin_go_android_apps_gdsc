package com.gdsc.bingo.ui.pop_up

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.gdsc.bingo.R

class SuccesReportPopUp : AppCompatActivity() {
    companion object {
        const val ANIMATION_TIME: Long = 3000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_succes_report_pop_up)

        Handler(this.mainLooper).postDelayed({
            setResult(RESULT_OK)
            finish()
        }, ANIMATION_TIME)
    }
}