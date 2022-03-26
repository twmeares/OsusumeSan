package com.twmeares.osusumesan.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.models.Article
import com.twmeares.osusumesan.ui.ArticleItemAdapter

class ReadingListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_list)

        // Initialize data.
        val myDataset = listOf<Article>(
            Article("title 1", 1.0, "summary 1"),
            Article("title 2", 2.0, "summary 2"),
            Article("title 3", 3.0, "summary 3")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        //TODO create the ItemAdapter class. Name it ArticleItemAdapter.
        recyclerView.adapter = ArticleItemAdapter(this, myDataset)

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
    }
}