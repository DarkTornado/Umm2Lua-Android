package com.darktornado.ummexecuter

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import java.util.*

class StdoutConsole(private val ctx: Context) : PopupWindow() {

    companion object {
        private val stdout = ArrayList<CharSequence>()
        private var adapter: ArrayAdapter<CharSequence>? = null

        fun append(msg: String?): Boolean {
            stdout.add(msg!!)
            if (adapter == null) return false
            adapter!!.notifyDataSetChanged()
            return true
        }

        fun appendError(_msg: String?): Boolean {
            val msg = SpannableString(_msg)
            msg.setSpan(ForegroundColorSpan(Color.parseColor("#FF5252")),
                    0, msg.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            StdoutConsole.stdout.add(msg)
            if (adapter == null) return false
            adapter!!.notifyDataSetChanged()
            return true
        }
    }

    init {
        val layout = LinearLayout(ctx)
        layout.orientation = 1
        val title = TextView(ctx)
        title.text = " ▼ Log"
        title.textSize = 16f
        title.setTextColor(Color.BLACK)
        title.setOnClickListener { v: View? -> dismiss() }
        val longClicked = booleanArrayOf(false)
        title.setOnLongClickListener { v: View? ->
            longClicked[0] = true
            true
        }
        title.setOnTouchListener { v: View?, event: MotionEvent ->
            if (!longClicked[0]) return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_MOVE -> update(-1, (ctx.resources.displayMetrics.heightPixels - event.rawY).toInt())
                MotionEvent.ACTION_UP -> longClicked[0] = false
            }
            false
        }
        layout.addView(title)
        val list = ListView(ctx)
        adapter = ArrayAdapter(ctx, R.layout.console_output, StdoutConsole.stdout)
        list.adapter = adapter
        list.divider = null
        list.layoutParams = LinearLayout.LayoutParams(-1, -1)
        list.transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        list.onItemClickListener = OnItemClickListener { adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long -> toast("길게 누르면 복사되는거에요.") }
        list.onItemLongClickListener = OnItemLongClickListener { adapterView: AdapterView<*>?, view: View, pos: Int, id: Long ->
            val txt = view as TextView
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("label", txt.text.toString()))
            toast("클립보드로 복사되었어요.")
            true
        }
        layout.addView(list)
        val pad: Int = dip2px(5)
        list.setPadding(pad, pad, pad, pad)
        setBackgroundDrawable(ColorDrawable(Color.WHITE))
        width = -1
        height = ctx.resources.displayMetrics.heightPixels / 2
        animationStyle = android.R.style.Animation_InputMethod
        contentView = layout
        if (Build.VERSION.SDK_INT >= 21) {
            elevation = dip2px(5).toFloat()
        }
    }

    fun open() {
        adapter!!.notifyDataSetChanged()
        if (!isShowing) showAtLocation((ctx as Activity).window.decorView, Gravity.BOTTOM, 0, 0)
    }

    fun close() {
        dismiss()
    }

    fun clear(): Boolean {
        stdout.clear()
        if (adapter == null) return false
        adapter!!.notifyDataSetChanged()
        return true
    }

    fun toast(msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()

    fun dip2px(dips: Int) = Math.ceil(dips * ctx.resources.displayMetrics.density.toDouble()).toInt()

}