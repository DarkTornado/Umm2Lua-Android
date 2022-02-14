package com.darktornado.ummexecuter

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.darktornado.umm2lua.Umm2Lua
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import org.luaj.vm2.lib.jse.JsePlatform

class MainActivity : Activity() {

    var editor: CodeEditor? = null;
    var stdout: StdoutConsole? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                val src = editor!!.text.toString()
                if (src.isBlank()) toast("입력된 내용이 없어요");
                else executeSource(src)
            }
            1 -> if (!editor!!.undo()) toast("더 이상 되돌릴 항목이 없어요.")
            2 -> if (!editor!!.redo()) toast("더 이상 다시 실행할 항목이 없어요.")
            3 -> stdout!!.clear()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, "실행")
        menu.add(0, 1, 0, "되돌리기")
        menu.add(0, 2, 0, "다시 실행")
        menu.add(0, 3, 0, "로그 초기화")
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = 1
        editor = CodeEditor(this)
        editor!!.hint = "엄랭 소스 입력..."
        layout.addView(editor)
        val pad = dip2px(16)
        layout.setPadding(pad, pad, pad, pad)
        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)

        stdout = StdoutConsole(this)
    }

    fun executeSource(source: String) {
        try {
            val compiled = Umm2Lua.compile(source)
            val globals: Globals = JsePlatform.standardGlobals()
            globals["print"] = CoerceJavaToLua.coerce(Print())
            val chunk = globals.load(compiled)
            chunk.call()
        } catch (e: Exception) {
            toast(e.toString())
        }
        stdout!!.open()
    }


    fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    fun dip2px(dips: Int) = Math.ceil(dips * resources.displayMetrics.density.toDouble()).toInt()

    internal class Print : OneArgFunction() {
        override fun call(msg: LuaValue): LuaValue {
            return LuaValue.valueOf(StdoutConsole.append(msg.tojstring()))
        }
    }

}