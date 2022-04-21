package com.twmeares.osusumesan.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.databinding.ActivitySearchBinding
import com.twmeares.osusumesan.models.DictionaryResult
import com.twmeares.osusumesan.services.DictionaryLookupService
import com.twmeares.osusumesan.services.KnowledgeService
import com.twmeares.osusumesan.services.iDictionaryLookupService
import com.twmeares.osusumesan.ui.SearchItemAdapter
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar

class SearchActivity : AppCompatActivity() {
    private lateinit var searchItemAdapter: SearchItemAdapter
    private lateinit var binding: ActivitySearchBinding
    private val TAG = "SearchActivity"
    private lateinit var dictService: iDictionaryLookupService
    private val displayDictCallback = iDictionaryLookupService.MultiResultCallback(::DisplayDictResults)
    private lateinit var searchList: MutableList<DictionaryResult>
    private lateinit var knowledgeService: KnowledgeService
    private val context: Context = this
    private var inputSearchList: MutableList<DictionaryResult>? = null
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        knowledgeService = KnowledgeService.GetInstance(this)
        dictService = DictionaryLookupService(this)
        searchList = mutableListOf<DictionaryResult>()

        // If the searchActivity was opened from the readingActivity an inputSearchList will be passed.
        inputSearchList = intent.getSerializableExtra("inputSearchList") as? MutableList<DictionaryResult>


        val recyclerView = findViewById<RecyclerView>(R.id.search_recycler_view)
        searchItemAdapter = SearchItemAdapter(this, searchList, dictService)
        recyclerView.adapter = searchItemAdapter
        searchItemAdapter.setOnItemClickListener(object: SearchItemAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                UpdateKnowledge(position)
            }
        })

        recyclerView.setHasFixedSize(false)

        if (inputSearchList != null && inputSearchList!!.size > 0){
            DisplayDictResults(inputSearchList!!)
            binding.searchHeader.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val search: MenuItem? = menu?.findItem(R.id.search)
        val searchView: SearchView = search?.actionView as SearchView
        searchView.maxWidth = binding.root.measuredWidth
        searchView.queryHint = "Search Dictionary"

        // Request focus to automatically bring up the keyboard when the activity loads,
        // but only when no input list was supplied in the intent.
        if (inputSearchList != null){
            searchView.isIconifiedByDefault = true
        } else {
            searchView.isIconifiedByDefault = false
            searchView.requestFocus()
        }

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                // Not planning to handle textChange. Only textSubmit will be used.
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query == null || query.equals("")){
                    val toast = Toast.makeText(context, "Type a word to search.",  Toast.LENGTH_LONG)
                    toast.show()
                } else {
                    dictService.SearchMultiResult(query, displayDictCallback)
                }
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    fun DisplayDictResults(dictResultList: List<DictionaryResult>) {
        if (dictResultList == null || dictResultList.size == 0){
            // Display a message for no results
            binding.searchRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.searchHeader.visibility = View.GONE
            binding.searchRecyclerView.visibility = View.VISIBLE
            searchList.clear()
            searchList.addAll(dictResultList)
            searchItemAdapter.notifyDataSetChanged()
            hideSoftKeyboard()
        }
    }

    fun UpdateKnowledge(position: Int){
        // Using snackbar b/c it shows with a black background and is actually visible over the
        // recyclerView that is behind it. Toast don't allow color customization after Android 11.
        val snackbar =
            Snackbar.make(binding.searchRecyclerView, "Vocab knowledge updated.", Snackbar.LENGTH_LONG)
        snackbar.show()


        val searchItem = searchList[position]
        Log.d(TAG, "Adding dictForm " + searchItem.dictForm + " and reading " + searchItem.reading + " to the knowledge DB.")
        // Item was clicked. Set the isKnown value to the opposite of what it previously was.
        // Add the reading and dict form to the knowledge DB.
        if (!searchItem.dictForm.equals("")){
            // could contain multiple readings.
            val dictForms = searchItem.dictForm.split(", ")
            dictForms.forEach { dictForm ->
                knowledgeService.UpdateKnowledge(dictForm, true)
            }
        }

        if (!searchItem.reading.equals("")){
            // could contain multiple readings.
            val readings = searchItem.reading.split(", ")
            readings.forEach { reading ->
                knowledgeService.UpdateKnowledge(reading, true)
            }
        }
    }

    fun hideSoftKeyboard() {
        val inputMethodManager: InputMethodManager =
            this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (this.currentFocus != null){
            inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
    }
}