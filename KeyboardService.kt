package com.shuvo.glasskeyboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*

class KeyboardService : InputMethodService() {
    private lateinit var root: LinearLayout
    private var shift = 1
    private var bn = false
    private var sym = -1
    private var panel = "keys"
    private var th = 0
    private var rr: Runnable? = null
    private val h = Handler(Looper.getMainLooper())
    private val clips = ArrayList<String>()
    private val themes = intArrayOf(0xFF2F7BFF.toInt(), 0xFFA855F7.toInt(), 0xFFFF4D6D.toInt(), 0xFFFFB020.toInt(), 0xFF14D6C4.toInt())

    private val en = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm").map { r -> r.map { it.toString() } }
    private val bnN = listOf(
        listOf("ঙ", "য", "ড", "প", "ট", "চ", "জ", "হ", "গ", "ড়"),
        listOf("া", "ে", "্", "ি", "ু", "ব", "ক", "ত", "দ"),
        listOf("ং", "ম", "ন", "র", "ল", "স", "শ"))
    private val bnS = listOf(
        listOf("ণ", "ধ", "ঢ", "ফ", "ঠ", "ছ", "ঝ", "ঘ", "ঞ", "ঢ়"),
        listOf("ৌ", "ৈ", "ঃ", "ী", "ূ", "ভ", "খ", "থ", "ঁ"),
        listOf("ৎ", "ঋ", "ণ", "ড়", "ঊ", "ঈ", "ঐ"))
    private val sy = listOf(
        listOf("1234567890", "-/:;()\$&@\""),
        listOf("[]{}#%^*+=", "_\\|~<>€£¥•"))
    private val emoji = ("😀 😃 😄 😁 😆 😅 😂 🤣 😊 😇 🙂 😉 😍 🥰 😘 😋 😜 🤪 😎 🤩 🥳 😏 😒 😞 😔 😟 😢 😭 😤 😠 😡 🤯 😳 🥶 😱 😰 🤗 🤔 🤭 🤫 😶 😐 🙄 😴 🤤 😷 🤒 🤢 🥴 😈 💀 👻 🤖 💩 👍 👎 👊 ✊ 🤝 👏 🙌 🙏 💪 👋 ✌️ 🤞 👌 ❤️ 🧡 💛 💚 💙 💜 🖤 🤍 💔 💕 💖 💯 🔥 ✨ ⭐ 🎉 🎁 🏆 ⚽ 🎮 🎵 🎧 🚗 ✈️ 🌙 ☀️ ⚡ 🌹 🍕 🍔 ☕ 🍰").split(" ")
    private val stickers = "🥳 🤩 😎 🫶 🔥 💯 🎉 👑 😂 🥹 😭 🤝 🙌 💃 🕺 🚀 🏆 💎 🎯 🍿".split(" ")
    private val gifs = listOf("LOL 😂", "WOW 😮", "GG 🔥", "OK 👍", "LOVE ❤️", "NO 🙅", "HAHA 🤣", "BYE 👋")
    private val phrases = listOf("আসসালামু আলাইকুম", "ধন্যবাদ", "কেমন আছো?", "Thanks!", "On my way", "OK 👍", "Call me later", "Good night 🌙")

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()
        th = getSharedPreferences("gk", 0).getInt("th", 0)
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.addPrimaryClipChangedListener {
            val t = try { cm.primaryClip?.getItemAt(0)?.text?.toString() } catch (e: Exception) { null }
            if (!t.isNullOrBlank()) { clips.remove(t); clips.add(0, t); if (clips.size > 30) clips.removeAt(30) }
        }
    }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(4), dp(6), dp(4), dp(8)) }
        render()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        panel = "keys"; sym = -1; shift = if (bn) 0 else 1
        if (::root.isInitialized) render()
    }

    private fun glass() = GradientDrawable(GradientDrawable.Orientation.TL_BR,
        intArrayOf(0x30FFFFFF, 0x0DFFFFFF, 0x1AFFFFFF)).apply {
        cornerRadius = dp(14).toFloat(); setStroke(dp(1), 0x33FFFFFF)
    }

    private fun btn(t: String, w: Float, sz: Float = 21f, rep: Boolean = false, a: () -> Unit): Button =
        Button(this).apply {
            text = t; textSize = sz; setTextColor(Color.WHITE); isAllCaps = false
            background = glass(); stateListAnimator = null; minWidth = 0; minHeight = 0; setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, dp(46), w).apply { setMargins(dp(3), dp(3), dp(3), dp(3)) }
            if (rep) {
                setOnTouchListener { v, e ->
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> {
                            v.isPressed = true; v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); a()
                            rr = object : Runnable { override fun run() { a(); h.postDelayed(this, 55) } }
                            h.postDelayed(rr!!, 400)
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            v.isPressed = false; rr?.let { h.removeCallbacks(it) }
                        }
                    }
                    true
                }
            } else setOnClickListener { it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); a() }
        }

    private fun row(vararg v: View, pad: Int = 0) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(pad, 0, pad, 0)
        v.forEach { addView(it) }
    }

    private fun rowOf(list: List<View>, pad: Int = 0) = row(*list.toTypedArray(), pad = pad)

    private fun render() {
        rr?.let { h.removeCallbacks(it) }
        root.removeAllViews()
        root.background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf((themes[th] and 0xFFFFFF) or 0x40000000, 0xFF0A0D10.toInt(), 0xFF0A0D10.toInt()))
        root.addView(toolbar())
        when (panel) {
            "keys" -> keys()
            "emoji" -> panelGrid(emoji, 8, 22f)
            "sticker" -> panelGrid(stickers, 5, 34f)
            "gif" -> panelGrid(gifs, 2, 17f)
            "apps" -> panelGrid(phrases, 1, 16f)
            "clip" -> if (clips.isEmpty()) {
                root.addView(TextView(this).apply {
                    text = "Clipboard is empty. Copied text appears here."
                    setTextColor(0xFF9AA1A8.toInt()); gravity = Gravity.CENTER; setPadding(0, dp(50), 0, dp(50))
                }); bottom()
            } else panelGrid(clips, 1, 15f)
        }
    }

    private fun toolbar(): View {
        fun tb(t: String, a: () -> Unit) = btn(t, 1f, 17f, false, a).apply {
            (layoutParams as LinearLayout.LayoutParams).height = dp(40)
        }
        fun go(p: String) { panel = if (panel == p) "keys" else p; sym = -1; render() }
        return row(
            tb("⊞") { go("apps") }, tb("🏷") { go("sticker") }, tb("GIF") { go("gif") },
            tb("📋") { go("clip") },
            tb("⚙") { startActivity(Intent(this, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
            tb("🎨") { th = (th + 1) % themes.size; getSharedPreferences("gk", 0).edit().putInt("th", th).apply(); render() },
            tb("🎤") { (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() })
    }

    private fun keys() {
        val label = { c: String -> if (!bn && shift > 0) c.uppercase() else c }
        if (sym < 0) {
            val rows = if (bn) (if (shift > 0) bnS else bnN) else en
            fun k(c: String) = btn(label(c), 1f, 23f) { commit(label(c)) }
            root.addView(rowOf(rows[0].map { k(it) }))
            root.addView(rowOf(rows[1].map { k(it) }, dp(12)))
            val sh = btn(listOf("⇧", "⬆", "⇪")[shift], 1.5f, 20f) { shift = (shift + 1) % 3; render() }
            if (shift > 0) sh.setTextColor(themes[th])
            root.addView(rowOf(listOf<View>(sh) + rows[2].map { k(it) } + btn("⌫", 1.5f, 20f, true) { del() }))
        } else {
            val s = sy[sym]
            fun k(c: Char) = btn(c.toString(), 1f, 22f) { commit(c.toString()) }
            root.addView(rowOf(s[0].map { k(it) }))
            root.addView(rowOf(s[1].map { k(it) }))
            root.addView(rowOf(listOf<View>(btn(if (sym == 0) "#+=" else "123", 1.5f, 15f) { sym = 1 - sym; render() }) +
                ".,?!'\"".map { k(it) } + btn("⌫", 1.5f, 20f, true) { del() }))
        }
        root.addView(row(
            btn(if (sym < 0) "123" else "ABC", 1.4f, 15f) { sym = if (sym < 0) 0 else -1; render() },
            btn("😊", 1.2f, 20f) { panel = "emoji"; render() },
            btn("🌐", 1.2f, 20f) { bn = !bn; shift = if (bn) 0 else 1; sym = -1; render() },
            btn(if (bn) "বাংলা" else "English", 4f, 16f) { commit(" ") },
            btn(".", 1.1f, 22f) { commit(".") },
            btn("↵", 1.5f, 22f) { enter() }))
    }

    private fun panelGrid(items: List<String>, cols: Int, sz: Float) {
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        items.chunked(cols).forEach { chunk ->
            val views = chunk.map { s -> btn(s, 1f, sz) { commit(s) } }.toMutableList<View>()
            while (views.size < cols) views.add(View(this).apply { layoutParams = LinearLayout.LayoutParams(0, dp(46), 1f) })
            col.addView(rowOf(views))
        }
        root.addView(ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(190)); addView(col)
        })
        bottom()
    }

    private fun bottom() {
        root.addView(row(
            btn("ABC", 1.5f, 15f) { panel = "keys"; sym = -1; render() },
            btn("space", 5f, 16f) { commit(" ") },
            btn("⌫", 1.5f, 20f, true) { del() }))
    }

    private fun commit(s: String) {
        currentInputConnection?.commitText(s, 1)
        if (panel != "keys" || bn || shift == 2) return
        val b = currentInputConnection?.getTextBeforeCursor(2, 0)?.toString() ?: ""
        val want = if (b.isEmpty() || b.endsWith("\n") || Regex(".*[.!?]\\s$").matches(b)) 1 else 0
        if (want != shift) { shift = want; render() }
    }

    private fun del() {
        val ic = currentInputConnection ?: return
        if (ic.getSelectedText(0).isNullOrEmpty()) ic.deleteSurroundingTextInCodePoints(1, 0) else ic.commitText("", 1)
    }

    private fun enter() {
        val ei = currentInputEditorInfo
        val a = (ei?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
        if (ei != null && (ei.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0 &&
            a != EditorInfo.IME_ACTION_NONE && a != EditorInfo.IME_ACTION_UNSPECIFIED)
            currentInputConnection?.performEditorAction(a)
        else sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
    }
}
