package com.twmeares.osusumesan.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.databinding.ActivityKnowledgeListBinding
import com.twmeares.osusumesan.models.KnowledgeItem
import com.twmeares.osusumesan.ui.KnowledgeItemAdapter
import com.twmeares.osusumesan.services.KnowledgeService


class KnowledgeListActivity : AppCompatActivity() {
    private lateinit var knowledgeList: List<KnowledgeItem>
    private lateinit var knowledgeService: KnowledgeService
    private lateinit var binding: ActivityKnowledgeListBinding
    private val TAG = "KnowledgeListActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowledgeListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListeners(binding.root)

        knowledgeService = KnowledgeService.GetInstance(this)

        knowledgeList = knowledgeService.GetKnowledge();

        val recyclerView = findViewById<RecyclerView>(R.id.knowledge_recycler_view)

        val knowledgeItemAdapter = KnowledgeItemAdapter(this, knowledgeList)
        recyclerView.adapter = knowledgeItemAdapter
        knowledgeItemAdapter.setOnItemClickListener(object:KnowledgeItemAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                UpdateKnowledge(position)
            }
        })

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
    }

    fun UpdateKnowledge(position: Int){
        val knowledgeItem = knowledgeList[position]
        // Item was clicked. Set the isKnown value to the opposite of what it previously was.
        knowledgeItem.isKnown = !knowledgeItem.isKnown
        knowledgeService.UpdateKnowledge(knowledgeItem.word, knowledgeItem.isKnown)
    }

    private fun setupClickListeners(view: View) {
        binding.btnAddKnowledge.setOnClickListener {
            Log.i(TAG, "clicked Add new word. Opening search activity.")
            openSearchActivity()
        }
    }

    fun openSearchActivity() {
        val intent = Intent(this, SearchActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(intent)
    }

}