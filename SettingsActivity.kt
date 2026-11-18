package com.shuvo.glasskeyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.*

class SettingsActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val d = resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(48), dp(22), dp(24))
            setBackgroundColor(0xFF0A0D10.toInt())
        }
        fun tv(t: String, s: Float, c: Int = Color.WHITE) = TextView(this).apply {
            text = t; textSize = s; setTextColor(c); setPadding(0, dp(6), 0, dp(6))
        }
        fun bt(t: String, a: () -> Unit) = Button(this).apply {
            text = t; isAllCaps = false; setOnClickListener { a() }
        }
        col.addView(tv("Glass Keyboard", 30f))
        col.addView(tv("Owner: Shuvo", 20f))
        col.addView(tv("WhatsApp: 01352035876", 20f))
        col.addView(bt("Open WhatsApp") {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/8801352035876")))
        })
        col.addView(tv("Setup", 18f, 0xFF9AA1A8.toInt()))
        col.addView(bt("1. Enable keyboard") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) })
        col.addView(bt("2. Choose keyboard") {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        })
        col.addView(tv("Try it", 18f, 0xFF9AA1A8.toInt()))
        col.addView(EditText(this).apply {
            hint = "Type here…"; setTextColor(Color.WHITE); setHintTextColor(0xFF8E949A.toInt())
        })
        setContentView(ScrollView(this).apply { addView(col) })
    }
}
