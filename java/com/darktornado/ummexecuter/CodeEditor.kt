package com.darktornado.ummexecuter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.Toast
import java.util.*

class CodeEditor(private val ctx: Context) : EditText(ctx) {

    private val before = Stack<History>()
    private val after = Stack<History>()
    private var block = false
    private val rect: Rect = Rect()

    init {
        initTextWatcher(ctx)
    }

    private fun initTextWatcher(ctx: Context) {
        val data = arrayOfNulls<CharSequence>(2)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (block) return
                data[0] = s.subSequence(start, start + count)
            }

            override fun onTextChanged(s: CharSequence, start: Int, _before: Int, count: Int) {
                if (block) return
                data[1] = s.subSequence(start, start + count)
                before.push(History(data, start))
            }

            override fun afterTextChanged(s: Editable) {
                try {
                    codeHighlight(s)
                } catch (e: Exception) {
                    Toast.makeText(ctx, e.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun codeHighlight(s: Editable) {
        val data = arrayOf("동탄", "?", "어떻게", "이 사람이름이냐ㅋㅋ")
        val str = s.toString()
        if (str.isBlank()) return
        val spans = s.getSpans(0, s.length, ForegroundColorSpan::class.java)
        for (n in spans.indices) {
            s.removeSpan(spans[n])
        }
        for (n in data.indices) {
            var start = 0
            while (start >= 0) {
                val index: Int = str.indexOf(data.get(n), start)
                var end: Int = index + data.get(n).length
                if (index >= 0) {
                    if (s.getSpans(index, end, ForegroundColorSpan::class.java).isEmpty() && isSeparated(str, index, end - 1)) s.setSpan(ForegroundColorSpan(Color.argb(255, 21, 101, 192)),
                            index, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    end = -1
                }
                start = end
            }
        }
    }

    private fun isSeparated(str: String, start: Int, end: Int): Boolean {
        var front = false
        val points = " []{}()+-*/%&|!?:;,<>=^~.".toCharArray()
        if (start == 0) {
            front = true
        } else if (str[start - 1] == '\n') {
            front = true
        } else {
            for (n in points.indices) {
                if (str[start - 1] == points[n]) {
                    front = true
                    break
                }
            }
        }
        if (front) {
            try {
                if (str[end + 1] == '\n') {
                    return true
                } else {
                    for (n in points.indices) {
                        if (str[end + 1] == points[n]) return true
                    }
                }
            } catch (e: java.lang.Exception) {
                return true
            }
        }
        return false
    }

    fun undo(): Boolean {
        if (before.empty()) return false
        val data = before.pop()
        val start = data.index
        val end: Int = start + data.after!!.length
        block = true
        editableText.replace(start, end, data.before)
        after.push(data)
        block = false
        return true
    }

    fun redo(): Boolean {
        if (after.empty()) return false
        val data = after.pop()
        val start = data.index
        val end: Int = start + data.before!!.length
        block = true
        editableText.replace(start, end, data.after)
        before.push(data)
        block = false
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val count = lineCount
        var num = 1
        for (n in 0 until count) {
            val baseline = getLineBounds(n, null)
            if (n == 0 || text[layout.getLineStart(n) - 1] === '\n') {
                canvas.drawText(num.toString() + "", (rect.left or rect.centerY()).toFloat(), baseline.toFloat(), paint)
                num++
            }
        }
        val pad = (num - 1).toString().length * 8
        val pad2 = dip2px(9)
        setPadding(dip2px(pad + 2), pad2, pad2, pad2)
        super.onDraw(canvas)
    }

    private fun dip2px(dips: Int) = Math.ceil(dips * ctx.resources.displayMetrics.density.toDouble()).toInt()

    class History(data: Array<CharSequence?>, var index: Int) {
        var before: CharSequence? = data[0]
        var after: CharSequence? = data[1]
    }

}