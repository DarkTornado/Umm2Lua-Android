package com.darktornado.ummexecuter

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView

class MainActivity : Activity() {

    var editor: CodeEditor? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = 1
        editor = CodeEditor(this)
        editor!!.hint = "엄랭 소스 입력..."
        layout.addView(editor)
        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

}