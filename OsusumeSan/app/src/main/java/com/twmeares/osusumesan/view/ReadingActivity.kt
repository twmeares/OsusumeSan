package com.twmeares.osusumesan.view

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.*
import android.text.style.ClickableSpan
import android.util.Log
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.databinding.ActivityReadingBinding
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

import com.twmeares.osusumesan.services.*
import android.text.StaticLayout
import androidx.startup.AppInitializer
import com.twmeares.osusumesan.models.*
import org.json.JSONObject


class ReadingActivity : AppCompatActivity() {
    private var curPageStartOffset: Int = 0
    private lateinit var binding: ActivityReadingBinding
    private lateinit var tokenizer: OsusumeSanTokenizer
    private lateinit var mainTextView: CustomTextView
    private lateinit var jmDictFuriHelper: JMDictFuriHelper
    private lateinit var dictService: iDictionaryLookupService
    private lateinit var aozoraService: AozoraService
    private lateinit var knowledgeService: KnowledgeService
    private lateinit var tokens: List<OsusumeSanToken>
    private lateinit var curPageText: String
    private lateinit var fullText: String //full article text without any pagination
    private lateinit var article: Article
    private val TAG: String = "ReadingActivity"
    private val displayDictCallback = iDictionaryLookupService.Callback(::DisplayDictResult)
    private val aozoraArticleCallback = AozoraService.Callback(::LoadAozoraResult)
    private var currentPageNum: Int = 1
    private var isTextMultiPage: Boolean = false
    private var lastLineCutCharNum: Int = 0
    private var furiTrackerMap: MutableMap<String, FuriTracker> = mutableMapOf<String, FuriTracker>()
    private var furiganaTapper: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
                startReading(currentPageNum)
            }
        } else if (inputArticle != null){
            // fetch the book data
            article = inputArticle
            fetchArticleFromAozora()
        }
        else {
            // Resume Last Reading
            val savedBookID = sharedPref.getString("bookId", null)
            if (savedBookID != null){
                // Resume reading the article based on bookId
                currentPageNum = sharedPref.getInt("currentPageNum", 1)
                article = Article(savedBookID)
                fetchArticleFromAozora()
            } else {
                // Check for fullText (AKA resume saved user input)
                val savedFullText = sharedPref.getString("fullText", null)
                if (savedFullText != null){
                    fullText = savedFullText
                    currentPageNum = sharedPref.getInt("currentPageNum", 1)
                } else {
                    // Neither found so display a default.
                    fullText = "Welcome to OsusumeSan. Please select an article from the reading " +
                            "list or use the input text feature."
                }

                GlobalScope.launch(Dispatchers.IO){
                    startReading(currentPageNum)
                }
            }
        }

        setupClickListeners()
    }


    override fun onPause() {
        super.onPause()
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preference.edit()
        editor.putInt("currentPageNum", currentPageNum)
        if (this::article.isInitialized){
            // Store current page and bookID
            editor.putString("bookId", article.bookId)
            editor.remove("fullText")
        } else {
            // Opened with user input, so save the whole input.
            // TODO is this gonna be too big?
            editor.putString("fullText", fullText)
            editor.remove("bookId")
        }
        editor.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.vocab_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title.equals(getString(R.string.vocab_title))) {
            // Start the search activity with the unique words supplied as an input.
            Log.d(TAG, "Opening vocab list")

            var inputSearchList = arrayListOf<DictionaryResult>()
            var wordList = arrayListOf<String>()
            if (this::article.isInitialized){
                // get the list of unique words for this article.
                var articleWordList = JSONObject(article.wordList)
                var kangoWords = articleWordList.getJSONArray("kango")
                var wagoWords = articleWordList.getJSONArray("wago")
                var verbWords = articleWordList.getJSONArray("verb")

                for (idx in 0 until kangoWords.length()) {
                    val word = kangoWords.getString(idx)
                    //inputSearchList.add(DictionaryResult(word, "", null, null, null, null, false))
                    wordList.add(word)
                }
                for (idx in 0 until wagoWords.length()) {
                    val word = wagoWords.getString(idx)
                    //inputSearchList.add(DictionaryResult(word, "", null, null, null, null, false))
                    wordList.add(word)
                }
                for (idx in 0 until verbWords.length()) {
                    val word = verbWords.getString(idx)
                    //inputSearchList.add(DictionaryResult(word, "", null, null, null, null, false))
                    wordList.add(word)
                }

            } else {
                // user input text. Create a word list based on tokens that are
                // not known aka have furigana enabled.
                val tokenMap: MutableMap<String, Int> = mutableMapOf<String, Int>()
                var allTokens = tokenizer.Tokenize(fullText)
                allTokens.forEach{ token ->
                    if ((token.isKanaWord || token.isKanjiWord) && !tokenMap.containsKey(token.dictForm)) {
                        tokenMap.put(token.dictForm, 1)
                        //inputSearchList.add(DictionaryResult(token.dictForm, token.reading, null, null, null, null, false))
                        wordList.add(token.dictForm)
                    }
                }
            }

            // Keep only the words that are unknown.
            var wordIsKnownMap = knowledgeService.GetIsKnownForWordsList(wordList)
            wordList.forEach { word ->
                val isKnown = wordIsKnownMap[word]
                if (isKnown == null){
                    inputSearchList.add(DictionaryResult(word, "", null, null, null, null, false))
                } else if (isKnown == false){
                    inputSearchList.add(DictionaryResult(word, "", null, null, null, null, false))
                }
            }
            val intent = Intent(this, SearchActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("inputSearchList", inputSearchList)
            this.startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
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

                    val reading = "" // There is no way to reliably determine the reading in this situation.
                    dictService.Search(searchSelection, reading, false, displayDictCallback)
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


                AddFurigana(token, ssb)

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
                        // Only updating knowledge for the first match of the token against the dictForm.
                        // This is to prevent calling updateKnowledge multiple times if the word appears
                        // in the text more than once.
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
                        // Only updating knowledge for the first match of the token against the dictForm.
                        // This is to prevent calling updateKnowledge multiple times if the word appears
                        // in the text more than once.
                        knowledgeService.UpdateKnowledge(word, isKnown)
                        knowledgeUpdated = true
                        // User clicked show furigana so clearing the tapper count map to ensure it gets shown.
                        furiTrackerMap.remove(word)
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
        if (!token.isKanjiWord) {
            // No need for furigana if the token contains zero kanji characters.
            return
        }

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

        if (token.isFuriganaEnabled == false){
            return
        }

        // Only show the furigana for a given word n times as determined by furitracker.
        // Word can only be shown n times where n = furiganaTapper
        var furiganaLimitExceeded = false
        val furiTracker: FuriTracker? = furiTrackerMap[dictForm]
        val wordPosInFullText: Int = basePosition + curPageStartOffset
        if (furiTracker != null){
            // TLDR; don't update the tracker for pages that have already been shown.
            // Only update for furiganaLimitExceeded or furiTrackerMap if the current position is
            // after the last position where the word was shown. This is to avoid re-updating
            // If a users goes back and forth between pages.
            if (wordPosInFullText > furiTracker.lastPositionShown){
                if (furiTracker.countShown >= furiganaTapper) {
                    furiganaLimitExceeded = true
                    // Must override token level isFuriganaEnabled to be able to show the proper text in
                    // the gloss.
                    token.isFuriganaEnabled = false
                } else {
                    furiTracker.countShown += 1
                    furiTracker.lastPositionShown = wordPosInFullText
                }
            }
        } else {
            furiTrackerMap[dictForm] = FuriTracker(wordPosInFullText, 1)
        }



        if (furiganaLimitExceeded == false) {
            // Add furigana to the tokens that contain kanji.
            if (token.isKanjiWord && reading != null && dictForm != null){
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
        GlobalScope.launch(Dispatchers.IO){
            fullText = result.text
            startReading(currentPageNum)
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
            curPageStartOffset = startPos
            // Account for any characters that got cut off from the previous page.
            if (pageNum > currentPageNum) {
                curPageStartOffset - lastLineCutCharNum
                curPageStartOffset = Math.max(0, curPageStartOffset) //don't allow negative.
            }
            curPageText = fullText.substring(curPageStartOffset, endPos)
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

    private fun fetchArticleFromAozora(){
        curPageText = "Fetching text from Aozora Bunko."
        aozoraService = AozoraService(this)
        aozoraService.FetchArticle(article, aozoraArticleCallback)
        GlobalScope.launch(Dispatchers.IO){
            initialize()
        }
    }

}