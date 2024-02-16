package com.gdsc.bingo.ui

import android.os.Build
import android.util.TypedValue
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class CustomSystemTweak(private val activity: AppCompatActivity) {

    fun statusBarTweak() : CustomSystemTweak {
        with(activity) {
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            val backgroundColor = typedValue.data
            window.statusBarColor = backgroundColor


            val bottomNavTypedValue = TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, bottomNavTypedValue, true)
            window.navigationBarColor = bottomNavTypedValue.data


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val attributes = window.attributes
                attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = attributes
            }

        }
        return this
    }

    fun customStatusBarColorSet(resId : Int) : CustomSystemTweak {
        with(activity) {
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            val backgroundColor = typedValue.data
            window.statusBarColor = backgroundColor
        }

        return this
    }

    fun customNavigationBarColorSet(resId : Int) : CustomSystemTweak {
        with(activity) {
            val bottomNavTypedValue = TypedValue()
            theme.resolveAttribute(resId, bottomNavTypedValue, true)
            window.navigationBarColor = bottomNavTypedValue.data
        }

        return this
    }

}