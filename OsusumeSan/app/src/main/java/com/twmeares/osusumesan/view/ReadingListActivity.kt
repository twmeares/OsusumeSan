package com.twmeares.osusumesan.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.models.Article
import com.twmeares.osusumesan.ui.ArticleItemAdapter
import android.widget.Toast




class ReadingListActivity : AppCompatActivity() {
    private lateinit var articleList: List<Article>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_list)

        // TODO replace this with some method for getting the real data.
        articleList = listOf<Article>(
            Article("こころ", 1.0, "夏目 漱石", "773"),
            Article("人間失格", 2.0, "太宰 治", "301"),
            Article("title 3", 3.0, "summary 3", "127"),
            Article("title 4", 3.1, "summary 4", "456"),
            Article("title 5", 3.3, "summary 5", "789"),
            Article("title 6", 3.3, "summary 6", "2093"),
            Article("title 7", 3.4, "summary 7", "57624"),
            Article("title 8", 3.8, "summary 8", "752"),
//            Article("title 9", 4.1, "summary 9"),
//            Article("title 10", 4.2, "summary 10"),
//            Article("title 11", 4.7, "summary 11"),
//            Article("title 12", 5.0, "summary 12"),
        )

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