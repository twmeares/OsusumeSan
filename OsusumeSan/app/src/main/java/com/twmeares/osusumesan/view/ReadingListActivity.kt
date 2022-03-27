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
            Article("title 1", 1.0, "summary 1 asd fasd fasdf asdf asdf asdf asdf asdf asdf asdf asdf"),
            Article("title 2", 2.0, "summary 2"),
            Article("title 3", 3.0, "summary 3"),
            Article("title 4", 3.1, "summary 4"),
            Article("title 5", 3.3, "summary 5"),
            Article("title 6", 3.3, "summary 6"),
            Article("title 7", 3.4, "summary 7"),
            Article("title 8", 3.8, "summary 8"),
            Article("title 9", 4.1, "summary 9"),
            Article("title 10", 4.2, "summary 10"),
            Article("title 11", 4.7, "summary 11"),
            Article("title 12", 5.0, "summary 12"),
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
        //TODO launch the Reading activity for the selected article.
        val msg = "TODO Launch the Reading Activity for article " + articleList[position].title
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        // Need to grab whatever necessary information to launch the reading activity.
        //val intent = Intent(this, ReadingActivity::class.java)
        //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        // TODO do we need some put extra here?
        //intent.putExtra("bookText", true)
        //this.startActivity(intent)
    }


}