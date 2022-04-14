package com.twmeares.osusumesan.view

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.*
import android.text.style.ClickableSpan
import android.util.Log
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.databinding.ActivityReadingBinding
import com.twmeares.osusumesan.models.DictionaryResult
import com.twmeares.osusumesan.models.OsusumeSanToken
import com.twmeares.osusumesan.models.OsusumeSanTokenizer
import com.twmeares.osusumesan.ui.MovementMethod
import com.twmeares.osusumesan.ui.RubySpan
import java.lang.Integer.min
import android.content.SharedPreferences
import android.view.*
import androidx.preference.PreferenceManager
import com.twmeares.osusumesan.ui.CustomTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import android.widget.Toast

import com.twmeares.osusumesan.models.Article
import com.twmeares.osusumesan.services.*
import android.text.StaticLayout
import androidx.startup.AppInitializer


class ReadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadingBinding
    private lateinit var tokenizer: OsusumeSanTokenizer
    private lateinit var mainTextView: CustomTextView
    private lateinit var jmDictFuriHelper: JMDictFuriHelper
    private lateinit var dictService: DictionaryLookupService
    private lateinit var aozoraService: AozoraService
    private lateinit var knowledgeService: KnowledgeService
    private lateinit var tokens: List<OsusumeSanToken>
    private lateinit var curPageText: String
    private lateinit var fullText: String //full article text without any pagination
    private lateinit var article: Article
    private val TAG: String = "ReadingActivity"
    private val displayDictCallback = iDictionaryLookupService.Callback(::DisplayDictResult)
    private val aozoraArticleCallback = AozoraService.Callback(::LoadAozoraResult)
    private var currentPageNum: Int = 0
    private var isTextMultiPage: Boolean = false
    private var lastLineCutCharNum: Int = 0
    private var furiCountMap: MutableMap<String, Int> = mutableMapOf<String, Int>()
    private var furiganaTapper: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initMainTextView()
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val fallbackVal = 99
        furiganaTapper = sharedPref.getString("furigana_tapper_preference", "99")?.toIntOrNull() ?: fallbackVal

        val inputText = intent.getStringExtra("inputText")
        //val bookText = intent.getStringExtra("bookText")
        val inputArticle: Article? = intent.getSerializableExtra("article") as? Article
        if(inputText != null){
            fullText = inputText.toString()
            Log.i(TAG, "Received input text of length " + fullText.length)
            //getOnePageOfText(1)
            GlobalScope.launch(Dispatchers.IO){
                startReading(1)
            }
        } else if (inputArticle != null){
            // fetch the book data
            article = inputArticle
            curPageText = "Fetching text from Aozora Bunko."
            aozoraService = AozoraService(this)
            aozoraService.FetchArticle(article, aozoraArticleCallback)
            GlobalScope.launch(Dispatchers.IO){
                initialize()
            }
        }
        else {
            // TODO eventually remove this section or put some other default.
            //test strings for now
            //var text = "頑張り屋"
            //var text = "大人買い" //doesn't work properly due to being tokenized as two words instead of one
            fullText = "村岡桃佳選手は、スキーで2つ目の金メダルに挑戦します。"
            //fullText = "たくさん"
            //fullText = "べんきょう"
            //fullText = "花見"
            //fullText = "花粉"
            //var text = "食べてる"
            //var text = "にほんごをべんきょうする"
            //text = "憚る" //"憚かる" // this word isn't recognized in kuromoji but is in sudachi
            //fullText = "花花花花花花花花"

            //getOnePageOfText(1)
            GlobalScope.launch(Dispatchers.IO){
                startReading(1)
            }
        }

        setupClickListeners()


    }

    override fun onResume() {
        super.onResume()
    }

    fun initMainTextView(){
        mainTextView = findViewById<View>(R.id.MainTextView) as CustomTextView
        mainTextView.textSize = 28f
        mainTextView.setLineSpacing(0f, 1.5f) // IMPORTANT!
        val textSize = mainTextView.textSize
        //val tenDp = (25 * getResources().getDisplayMetrics().density).toInt()
        val pad = (textSize / 2 + 5).toInt()
        mainTextView.setPadding(pad, (textSize / 2 + 5).toInt(), pad, 0)
        //mainTextView.setPadding(0, (textSize / 2 + 5).toInt(), 0, 0)

        // Setting a transparent shadow is one work around to text getting chopped off.
        // But it is said to have bad impact on performance.
        mainTextView.setShadowLayer(mainTextView.textSize/2, 0f, 0f, Color.TRANSPARENT)

//dont think we need this        //registerForContextMenu(mainTextView)
        mainTextView.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                //Would be nice to have a custom icon for this
                menu.add(0, R.id.MainTextView, 0, "Search")//TODO this set icon isn't working. .setIcon(android.R.drawable.ic_menu_search) //groupId, itemId, order, title
                menu.removeItem(android.R.id.shareText)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return true
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                if (item.title.equals("Search"))
                {
                    val searchSelection = mainTextView.text.substring(mainTextView.selectionStart, mainTextView.selectionEnd)
                    Log.d(TAG, "Manual selection search started for " + searchSelection)
                    mode.finish()
                    // TODO search with the word = exact selection, and maybe reading as the combined readings of the selection if it spans multiple tokens?
                    // Probably need to make sure that this doesn't show the enable/disable furi btn since that code wont work properly for this word.
                    //dictService.Search(word, reading, isFuriganaEnabled, displayDictCallback)
                    return true
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {

            }
        }

        mainTextView.setLinksClickable(true);
        mainTextView.setTextIsSelectable(true)
        mainTextView.setMovementMethod(MovementMethod.getInstance())


        mainTextView.setOnClickListener(View.OnClickListener {
            //Log.d(TAG, "got regular click")
            //Log.d(TAG, "highlight start " + mainTextView.highlightStart + " end " + mainTextView.highlightEnd)
            if (mainTextView.isHasHighlight &&
                (mainTextView.highlightStart != mainTextView.selectionStart
                        || mainTextView.highlightEnd != mainTextView.selectionEnd)) {
                /*
                 After allowing arbitrary text selection to enable the copy/paste menu, the text
                 selection on word click to show the gloss was broken. Seems it was getting erased.
                 I think it's happening during an onclick event that is arriving after the ontouch
                 was handled to trigger the gloss.
                 So this code checks if a "highlight" was requested as part of the
                 regular word click and reapplies it if the "highlight" (aka selection) was removed by the
                 extra onclick after the ontouch. This is ugly, but I couldn't find any way to
                 stop the selection from being cleared since that part isn't happening in my code.
                 So this is the best workaround I could figure out.
                 */
                Selection.setSelection(mainTextView.text as Spannable, mainTextView.highlightStart, mainTextView.highlightEnd)
                Log.d(TAG, "Selection was removed. Setting it again.")
            }
        })
    }

    fun safeInt(text: String, fallback: Int): Int {
        return text.toIntOrNull() ?: fallback
    }

    // Sets up the activity for reading
    suspend fun startReading(pageNum: Int){
        initialize()
        GlobalScope.launch(Dispatchers.Main){
            getOnePageOfText(pageNum)
            displayText(curPageText)
        }
    }

    // Initializes the services
    suspend fun initialize(){
        if(!this::knowledgeService.isInitialized){
            knowledgeService = KnowledgeService.GetInstance(this)
        }

        if (!this::dictService.isInitialized){
            dictService = DictionaryLookupService(this)
        }

        if (!this::jmDictFuriHelper.isInitialized){
            jmDictFuriHelper = JMDictFuriHelper(this)
            jmDictFuriHelper.createDataBase()
            jmDictFuriHelper.openDataBase()
        }


        if (!this::tokenizer.isInitialized){
            tokenizer = AppInitializer.getInstance(this).initializeComponent(TokenizerInitializer::class.java)
        }
    }

    // Genereates the furigana and clickable spans
    suspend fun configureText(text: String): SpannableStringBuilder{
        return GlobalScope.async(Dispatchers.IO) {
            val ssb = SpannableStringBuilder(text)
            tokens = tokenizer.Tokenize(text)

            tokens.forEachIndexed { tokenIdx, token ->
                val reading = token.reading
                val dictForm = token.dictForm
                val basePosition = token.position
                val totalLength = min(dictForm.length, reading.length)
                var end = basePosition + totalLength

                if (token.isKanjiWord) {
                    AddFurigana(token, ssb)
                }


                if (token.isKanjiWord || token.isKanaWord) {
                    ssb.setSpan(
                        GenClickableSpan(token),
                        basePosition,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            //check if the last word will get cut off
            if (isTextMultiPage == true){
                val sb = StaticLayout.Builder.obtain(ssb, 0, ssb.length, mainTextView.paint,
                    mainTextView.measuredWidth - mainTextView.paddingLeft - mainTextView.paddingRight)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(mainTextView.lineSpacingExtra, mainTextView.lineSpacingMultiplier)
                    .setIncludePad(mainTextView.includeFontPadding)
                val layoutWithSpans = sb.build()

                val lastPos = layoutWithSpans.getLineEnd(Math.min(mainTextView.maxLines - 1, layoutWithSpans.lineCount-1))
                // Check if the text will "actually" fit the screen and if there is any difference
                // between the length we tried to fit and the length that will actually fit.
                lastLineCutCharNum = ssb.length - lastPos
            }

            return@async ssb
        }.await()
    }

    // Adds the spannable text to the TextView
    suspend fun displayText(text: String){
        var ssb = configureText(text)
        mainTextView.text = ssb
    }

    // Generates clickable span that launches a GlossDialog on click.
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
                        knowledgeService.UpdateKnowledge(word, isKnown)
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
                        knowledgeService.UpdateKnowledge(word, isKnown)
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
        mainTextView.isHasHighlight = false
    }

    fun AddFurigana(token: OsusumeSanToken, ssb: Spannable){
        // TODO create setting to allow a user to totally turn off furigana. Replace the hardcoded
        // bool with the setting.
        var furiTotallyDisabled = false
        if (furiTotallyDisabled == true){
            return
        }

        val reading = token.reading
        val dictForm = token.dictForm
        val basePosition = token.position
        val totalLength = min(dictForm.length, reading.length)
        var start = basePosition
        var end = basePosition + totalLength
        val underline = false
        token.isFuriganaEnabled = !knowledgeService.IsKnown(dictForm)

        // Only show the furigana for a given word n times as determined by furiganaTapper.
        var furiganaLimitExceeded = false
        val count: Int? = furiCountMap[dictForm]
        if (count != null){
            if (count >= furiganaTapper) {
                furiganaLimitExceeded = true
            } else {
                furiCountMap[dictForm] = count + 1
            }
        } else {
            furiCountMap[dictForm] = 1
        }



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

    fun LoadAozoraResult(result: Article){
        article = result

        // Limit the amount to show for now.
        GlobalScope.launch(Dispatchers.IO){
            //displayText(result.text.substring(0, 200))
            fullText = result.text
            //getOnePageOfText(1)
            startReading(1)
        }
    }

    fun getOnePageOfText(pageNum: Int): Boolean{
        if (pageNum < 1){
            val msg = "Already on first page."
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            return false
        }

        if (mainTextView.measuredWidth == 0) {
            // Somehow we got to measuring the width before the textView was drawn.
            // Manually calling measure screws up the text display and half of the screen
            // shows as blank. Need a better way of dealing with this.
            // Check out https://stackoverflow.com/questions/24430429/getmeasuredheight-and-getmeasuredwidth-returns-0-after-view-measure
            curPageText = "problem loading the text. Please retry."
            Log.e(TAG, "Reached getOnePageOfText before the textView width was set.")
            return false
        }

        val sb = StaticLayout.Builder.obtain(fullText, 0, fullText.length, mainTextView.paint,
            mainTextView.measuredWidth - mainTextView.paddingLeft - mainTextView.paddingRight)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(mainTextView.lineSpacingExtra, mainTextView.lineSpacingMultiplier)
            .setIncludePad(mainTextView.includeFontPadding)
        val textLayout = sb.build()

        if (textLayout.lineCount <= mainTextView.maxLines){
            // all fits on one page
            curPageText = fullText
            isTextMultiPage = false
            //TODO disable the prev/next buttons if only one page of text in total
            return true
        } else {
            // just grab maxLines worth of text offset by the pageNum
            isTextMultiPage = true
            val lastLineIdx = textLayout.lineCount - 1
            val startLineNum = mainTextView.maxLines * (pageNum - 1)
            if (startLineNum > lastLineIdx) {
                val msg = "Already on last page."
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                return false
            }
            val endLineNum = Math.min((mainTextView.maxLines * pageNum) - 1, lastLineIdx)
            val startPos = textLayout.getLineStart(startLineNum)
            val endPos = textLayout.getLineEnd(endLineNum)
            var startOffset = startPos
            // Account for any characters that got cut off from the previous page.
            if (pageNum > currentPageNum) {
                startOffset - lastLineCutCharNum
                startOffset = Math.max(0, startOffset) //don't allow negative.
            }
            curPageText = fullText.substring(startOffset, endPos)
            currentPageNum = pageNum
            return true
        }

    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            if (getOnePageOfText(currentPageNum + 1)) {
                GlobalScope.launch(Dispatchers.Main){
                    displayText(curPageText)
                }
            }
        }

        binding.btnPrev.setOnClickListener {
            if(getOnePageOfText(currentPageNum - 1)){
                GlobalScope.launch(Dispatchers.Main){
                    displayText(curPageText)
                }
            }
        }
    }

}