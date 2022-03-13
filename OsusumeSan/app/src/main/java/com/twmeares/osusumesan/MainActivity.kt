package com.twmeares.osusumesan

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.twmeares.osusumesan.models.DictionaryResult
import com.twmeares.osusumesan.models.OsusumeSanTokenizer
import com.twmeares.osusumesan.services.DictionaryLookupService
import com.twmeares.osusumesan.services.iDictionaryLookupService
import com.twmeares.osusumesan.ui.RubySpan
import com.twmeares.osusumesan.utils.DataBaseHelper
import com.twmeares.osusumesan.utils.SysDictHelper
import com.twmeares.osusumesan.viewmodels.GlossDialog
import org.json.JSONObject
import java.lang.Integer.min

class MainActivity : AppCompatActivity() {
    private lateinit var tokenizer: OsusumeSanTokenizer
    private lateinit var mainTextView: TextView
    private lateinit var dbHelper: DataBaseHelper
    private lateinit var dictService: DictionaryLookupService
    private val displayDictCallback = iDictionaryLookupService.Callback(::DisplayDictResult)
    private val TAG: String = "MainActivity"
    //private lateinit var displayDictCallback: iDictionaryLookupService.Callback

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
        dictService = DictionaryLookupService(this)

        //displayDictCallback = ::DisplayDictResult
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
        var tokens = tokenizer.Tokenize(text)

        tokens.forEachIndexed { tokenIdx, token ->
            val reading = token.reading
            val dictForm = token.dictForm
            val basePosition = token.position
            val totalLength = min(dictForm.length, reading.length)
            var start = basePosition
            var end = basePosition + totalLength
            val underline = false

            // Add furigana to the tokens that contain kanji.
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
                                    rubyIdx = rubyIdxStr.first().digitToInt()
                                    rubyIdxEnd = rubyIdxStr.last().digitToInt() + 1
                                } else {
                                    Log.d(TAG, dictForm + " not found in JMDictFurigana.")
                                    rubyIdx = furiIdx
                                    rubyIdxEnd = rubyIdx + 1
                                }
                            }
                            // normal case when the furi position info was found in furiHelper.
                            val ruby = splitItem[1]
                            start = basePosition + rubyIdx
                            end = basePosition + rubyIdxEnd
                            ssb.setSpan(RubySpan(ruby, underline), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                } else {
                    // failed to find the word in jmDict.db, possible bad tokenization of compound word.
                    Log.d(TAG, dictForm + " not found in JMDictFurigana.")
                    ssb.setSpan(RubySpan(reading, underline), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            if (token.isKanjiWord || token.isKanaWord) {
                // TODO there is some kind of issue where clicking the word on the left edge of a row
                // activates the clickable region on the right side word on the previous row
                ssb.setSpan(GenClickableSpan(dictForm, token.isFuriganaEnabled), basePosition, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        mainTextView.text = ssb
        mainTextView.setMovementMethod(LinkMovementMethod.getInstance())
    }

    fun GenClickableSpan(text: String, isFuriganaEnabled: Boolean): ClickableSpan {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                dictService.Search(text, isFuriganaEnabled, displayDictCallback)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = Color.BLACK
                ds.bgColor = Color.TRANSPARENT
            }
        }
        return clickableSpan
    }

    // Display the dictionary info in a gloss dialog
    fun DisplayDictResult(dictResult: DictionaryResult) {
        GlossDialog.newInstance(dictResult).show(supportFragmentManager, GlossDialog.TAG)
    }
}