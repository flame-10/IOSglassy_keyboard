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
        try {
            setContentView(build())
        } catch (e: Throwable) {
            setContentView(ScrollView(this).apply {
                addView(TextView(this@SettingsActivity).apply {
                    text = "ERROR:\n" + e.stackTraceToString().take(1500)
                    setTextColor(Color.RED)
                    setBackgroundColor(Color.BLACK)
                    setPadding(30, 80, 30, 30)
                })
            })
        }
    }

    private fun build(): ScrollView {
        val d = resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(22), dp(48), dp(22), dp(24))
        col.setBackgroundColor(Color.parseColor("#0A0D10"))

        fun tv(t: String, s: Float, c: Int = Color.WHITE): TextView {
            val v = TextView(this)
            v.text = t; v.textSize = s; v.setTextColor(c)
            v.setPadding(0, dp(6), 0, dp(6))
            return v
        }
        fun bt(t: String, a: () -> Unit): Button {
            val v = Button(this)
            v.text = t; v.isAllCaps = false
            v.setOnClickListener { a() }
            return v
        }

        col.addView(tv("Glass Keyboard", 30f))
        col.addView(tv("Owner: Shuvo", 20f))
        col.addView(tv("WhatsApp: 01352035876", 20f))
        col.addView(bt("Open WhatsApp") {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/8801352035876")))
        })
        col.addView(tv("Setup", 18f, Color.GRAY))
        col.addView(bt("1. Enable keyboard") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        col.addView(bt("2. Choose keyboard") {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        })
        col.addView(tv("Try it", 18f, Color.GRAY))
        val et = EditText(this)
        et.hint = "Type here…"
        et.setTextColor(Color.WHITE)
        et.setHintTextColor(Color.GRAY)
        col.addView(et)

        val sv = ScrollView(this)
        sv.addView(col)
        return sv
    }
}
