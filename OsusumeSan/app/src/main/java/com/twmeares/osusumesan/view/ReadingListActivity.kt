package com.twmeares.osusumesan.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.models.Article
import com.twmeares.osusumesan.ui.ArticleItemAdapter
import android.widget.Toast
import com.twmeares.osusumesan.services.AozoraStatsHelper
import com.twmeares.osusumesan.services.KnowledgeService


class ReadingListActivity : AppCompatActivity() {
    private lateinit var articleList: List<Article>
    private lateinit var aozoraStatsHelper: AozoraStatsHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_list)
        aozoraStatsHelper = AozoraStatsHelper.GetInstance(this)

        articleList = aozoraStatsHelper.getAllBookDetails()

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        val articleItemAdapter = ArticleItemAdapter(this, articleList)
        recyclerView.adapter = articleItemAdapter
        articleItemAdapter.setOnItemClickListener(object:ArticleItemAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                OpenArticle(position)
            }
        })

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
    }

    fun OpenArticle(position: Int){
        val intent = Intent(this, ReadingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("article", articleList[position])
        this.startActivity(intent)
    }


}