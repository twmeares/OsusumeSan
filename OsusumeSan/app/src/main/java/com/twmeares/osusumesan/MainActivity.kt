package com.twmeares.osusumesan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import com.twmeares.osusumesan.models.OsusumeSanTokenizer
import com.twmeares.osusumesan.ui.RubySpan
import com.twmeares.osusumesan.utils.DataBaseHelper

class MainActivity : AppCompatActivity() {
    private lateinit var tokenizer: OsusumeSanTokenizer
    private lateinit var mainTextView: TextView
    private val DB_NAME = "jmdict.db"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init
        initMainTextView()
        tokenizer = OsusumeSanTokenizer()
        val dbHelper = DataBaseHelper(this)
        dbHelper.createDataBase()
        dbHelper.openDataBase()
        val result = dbHelper.getFuriganaFromDB("東京", "とうきょう")

        var text = "頑張り屋"
        displayText(text)
    }

    fun initMainTextView(){
        mainTextView = findViewById<View>(R.id.MainTextView) as TextView
        mainTextView.textSize = 28f
        mainTextView.setLineSpacing(0f, 1.5f) // IMPORTANT!
        val textSize = mainTextView.textSize
        mainTextView.setPadding(0, (textSize / 2 + 5).toInt(), 0, 0)
    }

    fun displayText(text: String){
        //tokenize text
        var tokens = tokenizer.Tokenize(text)

        val ssb = SpannableStringBuilder(text)

        //when both the clickableSpan and rubySpan are on the same index the color change from the clickable span doesn't happen.
        //ssb.setSpan(GenClickableSpan("頑張り屋"), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(RubySpan("がん", true), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(RubySpan("ば", true), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(RubySpan("や", true), 3, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        mainTextView.text = ssb
        mainTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}