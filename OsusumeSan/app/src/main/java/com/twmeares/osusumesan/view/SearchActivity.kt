package com.twmeares.osusumesan.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.SearchView
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.databinding.ActivitySearchBinding
import com.twmeares.osusumesan.models.DictionaryResult

import com.twmeares.osusumesan.services.KnowledgeService
import com.twmeares.osusumesan.ui.SearchItemAdapter

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val TAG = "SearchActivity"
    
    private lateinit var searchList: List<DictionaryResult>
    private lateinit var knowledgeService: KnowledgeService
    private val context: Context = this
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        knowledgeService = KnowledgeService.GetInstance(this)

        // TODO display something useful on the screen when the list is empty?
        searchList = emptyList()

        val recyclerView = findViewById<RecyclerView>(R.id.search_recycler_view)

        val searchItemAdapter = SearchItemAdapter(this, searchList)
        recyclerView.adapter = searchItemAdapter
        searchItemAdapter.setOnItemClickListener(object: SearchItemAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                UpdateKnowledge(position)
            }
        })

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val search: MenuItem? = menu?.findItem(R.id.search)
        val searchView: SearchView = search?.actionView as SearchView
        searchView.isIconifiedByDefault = false
        searchView.maxWidth = binding.root.measuredWidth
        searchView.queryHint = "Search Dictionary"

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                // Not planning to handle textChange. Only textSubmit will be used.
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                // TODO add a call to the dictionary lookup service and populate the searchList
                Toast.makeText(context, "Searching for " + query,  Toast.LENGTH_LONG).show()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    fun UpdateKnowledge(position: Int){
        val searchItem = searchList[position]
        // Item was clicked. Set the isKnown value to the opposite of what it previously was.
        knowledgeService.UpdateKnowledge(searchItem.dictForm, true)
    }
}