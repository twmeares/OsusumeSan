package com.twmeares.osusumesan.view

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.*
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.databinding.ActivityMainMenuBinding
import com.twmeares.osusumesan.databinding.ActivityReadingBinding
import com.twmeares.osusumesan.models.DictionaryResult
import com.twmeares.osusumesan.models.OsusumeSanToken
import com.twmeares.osusumesan.models.OsusumeSanTokenizer
import com.twmeares.osusumesan.services.DictionaryLookupService
import com.twmeares.osusumesan.services.KnowledgeService
import com.twmeares.osusumesan.services.iDictionaryLookupService
import com.twmeares.osusumesan.ui.MovementMethod
import com.twmeares.osusumesan.ui.RubySpan
import com.twmeares.osusumesan.services.JMDictFuriHelper
import com.twmeares.osusumesan.services.SysDictHelper
import java.lang.Integer.min

class ReadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadingBinding
    private lateinit var tokenizer: OsusumeSanTokenizer
    private lateinit var mainTextView: TextView
    private lateinit var jmDictFuriHelper: JMDictFuriHelper
    private lateinit var dictService: DictionaryLookupService
    private val displayDictCallback = iDictionaryLookupService.Callback(::DisplayDictResult)
    private lateinit var knowledgeService: KnowledgeService
    private lateinit var tokens: List<OsusumeSanToken>
    private val TAG: String = "ReadingActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //test strings for now
        //var text = "頑張り屋"
        //var text = "大人買い" //doesn't work properly due to being tokenized as two words instead of one
        var text = "村岡桃佳選手は、スキーで2つ目の金メダルに挑戦します。"
        //var text = "食べてる"
        //var text = "にほんごをべんきょうする"

        //init
        initMainTextView()
        knowledgeService = KnowledgeService.GetInstance(this)
        dictService = DictionaryLookupService(this)

        jmDictFuriHelper = JMDictFuriHelper(this)
        //jmDictFuriHelper.createDataBase()
        jmDictFuriHelper.openDataBase()


        val useSudachi = false
        if (useSudachi){
            var sysDictHelper =
                SysDictHelper(this)
            sysDictHelper.createDataBase()
            var dict = sysDictHelper.dictionary
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
        tokens = tokenizer.Tokenize(text)

        tokens.forEachIndexed { tokenIdx, token ->
            val reading = token.reading
            val dictForm = token.dictForm
            val basePosition = token.position
            val totalLength = min(dictForm.length, reading.length)
            var end = basePosition + totalLength

            AddFurigana(token, ssb)

            if (token.isKanjiWord || token.isKanaWord) {
                ssb.setSpan(GenClickableSpan(token), basePosition, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        mainTextView.text = ssb
        mainTextView.setMovementMethod(MovementMethod.getInstance())

        //TODO is this necessary/what does it do?
        mainTextView.setLinksClickable(true);
    }

    fun GenClickableSpan(token: OsusumeSanToken): ClickableSpan {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                var word = token.dictForm
                var reading = token.reading
                var isFuriganaEnabled = token.isFuriganaEnabled
                Log.d(TAG, "Search dictionary for " + word)
                dictService.Search(word, reading, isFuriganaEnabled, displayDictCallback)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = Color.DKGRAY
                ds.bgColor = Color.TRANSPARENT
            }
        }
        return clickableSpan
    }

    // Display the dictionary info in a gloss dialog
    fun DisplayDictResult(dictResult: DictionaryResult) {
        GlossDialog.newInstance(dictResult).show(supportFragmentManager, GlossDialog.TAG)
    }

    // Change the furigana setting for the word
    fun UpdateFurigana(word: String, enableFurigana: Boolean ){
        var knowledgeUpdated = false
        var isKnown = !enableFurigana
        if (enableFurigana == false) {
            // disable furigana
            tokens.forEachIndexed { tokenIdx, token ->
                if (token.dictForm.equals(word)){
                    if (knowledgeUpdated == false){
                        knowledgeService.UpdateKnowledge(word, token.reading, isKnown)
                        knowledgeUpdated = true
                    }
                    token.isFuriganaEnabled = enableFurigana
                    // remove the spans for this word (could be multiple)
                    val totalLength = min(token.dictForm.length, token.reading.length)
                    var start = token.position
                    var end = token.position + totalLength
                    var spannable = mainTextView.text as Spannable
                    var spansToRemove = spannable.getSpans(start, end, RubySpan::class.java)
                    spansToRemove.forEach { span ->
                        spannable.removeSpan(span)
                    }
                }
            }

        } else {
            // enable furigana
            var spannable = mainTextView.text as Spannable
            tokens.forEachIndexed { tokenIdx, token ->
                if (token.dictForm.equals(word)){
                    if (knowledgeUpdated == false){
                        knowledgeService.UpdateKnowledge(word, token.reading, isKnown)
                        knowledgeUpdated = true
                    }

                    AddFurigana(token, spannable)
                }
            }
        }
    }

    // removes the selection (highlight) form the text input
    fun ClearTextSelection(){
        var spannable = mainTextView.text as Spannable
        Selection.removeSelection(spannable)
    }

    fun AddFurigana(token: OsusumeSanToken, ssb: Spannable){
        val reading = token.reading
        val dictForm = token.dictForm
        val basePosition = token.position
        val totalLength = min(dictForm.length, reading.length)
        var start = basePosition
        var end = basePosition + totalLength
        val underline = false
        token.isFuriganaEnabled = !knowledgeService.IsKnown(dictForm, reading)

        // TODO add a check to allow showing furigana only for the first n times. Can use a dict
        // and if the number is exceeded then don't do this inner section.
        var furiganaLimitExceeded = false //TODO replace with the real thing.

        if (furiganaLimitExceeded == false) {
            // Add furigana to the tokens that contain kanji.
            if (token.isKanjiWord && token.isFuriganaEnabled && reading != null && dictForm != null){
                val furiHelper = jmDictFuriHelper.getFuriganaFromDB(dictForm, reading)
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
        }
    }
}