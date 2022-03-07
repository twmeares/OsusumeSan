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
import com.twmeares.osusumesan.utils.SysDictHelper
import com.worksap.nlp.sudachi.Tokenizer
import java.lang.Integer.min

class MainActivity : AppCompatActivity() {
    private lateinit var tokenizer: OsusumeSanTokenizer
    private lateinit var mainTextView: TextView
    private lateinit var dbHelper: DataBaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //test strings for now
        //var text = "頑張り屋"
        //var text = "大人買い" //doesn't work properly due to being tokenized as two words instead of one
        var text = "村岡桃佳選手は、スキーで2つ目の金メダルに挑戦します。"
        //var text = "食べてる"

        //init
        initMainTextView()

        dbHelper = DataBaseHelper(this)
        dbHelper.createDataBase()
        dbHelper.openDataBase()
        var sysDictHelper = SysDictHelper(this)
        sysDictHelper.createDataBase()
        var dict = sysDictHelper.dictionary
        val useSudachi = false
        if (useSudachi){
            tokenizer = OsusumeSanTokenizer(dict)
        } else {
            tokenizer = OsusumeSanTokenizer()
        }

        displayText(text)
    }

    fun initMainTextView(){
        mainTextView = findViewById<View>(R.id.MainTextView) as TextView
        mainTextView.textSize = 28f
        mainTextView.setLineSpacing(0f, 1.5f) // IMPORTANT!
        val textSize = mainTextView.textSize
        mainTextView.setPadding(0, (textSize / 2 + 5).toInt(), 0, 0)
    }

    fun safeInt(text: String, fallback: Int): Int {
        return text.toIntOrNull() ?: fallback
    }

    fun displayText(text: String){
        val ssb = SpannableStringBuilder(text)

        //tokenize text
        var tokens = tokenizer.Tokenize(text)

        tokens.forEachIndexed { tokenIdx, token ->
            //val all = token.token.allFeatures
            val reading = token.reading
            val dictForm = token.dictForm
            val basePosition = token.position
            val totalLength = min(dictForm.length, reading.length)
            if (dictForm.length != reading.length){
                //TODO delete this just wanting to see how often this is the case
                val diff = dictForm.length - reading.length
            }
            if (token.isKanjiWord && token.isFuriganaEnabled && reading != null && dictForm != null){
                val furiHelper = dbHelper.getFuriganaFromDB(dictForm, reading)
                if (furiHelper != null) {
                    val furiList = furiHelper.split(";")
                    furiList.forEachIndexed { furiIdx, item ->
                        val splitItem = item.split(":")
                        if(splitItem.size == 2) {
                            var rubyIdx : Int
                            var rubyIdxEnd : Int
                            val rubyIdxStr = splitItem[0]
                            try {
                                rubyIdx = rubyIdxStr.toInt()
                                rubyIdxEnd = rubyIdx + 1
                            } catch (ex: NumberFormatException){
                                if (rubyIdxStr.contains("-") && rubyIdxStr.last().isDigit() && rubyIdxStr.first().isDigit()){
                                    // furigana that doesn't evenly split on the kanji word e.g.
                                    // 大人 is 0-1:おとな
                                    // grab the last digit as the rubyIdxEnd
                                    //TODO add logging
                                    rubyIdx = rubyIdxStr.first().digitToInt()
                                    rubyIdxEnd = rubyIdxStr.last().digitToInt() + 1
                                } else {
                                    //TODO add logging
                                    rubyIdx = furiIdx
                                    rubyIdxEnd = rubyIdx + 1
                                }
                            }
                            val ruby = splitItem[1]
                            val start = basePosition + rubyIdx
                            val end = basePosition + rubyIdxEnd
                            ssb.setSpan(RubySpan(ruby, true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                } else {
                    //failed to find the word in jmDict.db, possible bad tokenization of compound word.
                    var start = basePosition
                    var end = basePosition + totalLength
                    ssb.setSpan(RubySpan(reading, true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

            }
        }


        //when both the clickableSpan and rubySpan are on the same index the color change from the clickable span doesn't happen.
        //ssb.setSpan(GenClickableSpan("頑張り屋"), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        ssb.setSpan(RubySpan("がん", true), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        ssb.setSpan(RubySpan("ば", true), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        ssb.setSpan(RubySpan("や", true), 3, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        mainTextView.text = ssb
        mainTextView.setMovementMethod(LinkMovementMethod.getInstance())
    }
}