package com.twmeares.osusumesan.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.models.Article
import com.twmeares.osusumesan.services.AozoraStatsHelper
import com.twmeares.osusumesan.services.KnowledgeService

// Onclick setup based on https://stackoverflow.com/questions/24471109/recyclerview-onclick?page=1&tab=scoredesc#tab-top
// General adapter code based on https://developer.android.com/codelabs/basic-android-kotlin-training-recyclerview-scrollable-list?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-2-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-recyclerview-scrollable-list#4

/**
 * Adapter for the [RecyclerView] in [ReadingListActivity].
 */
class ArticleItemAdapter : RecyclerView.Adapter<ArticleItemAdapter.ItemViewHolder> {

    private val context: Context
    private val dataset: List<Article>

    constructor(context: Context, dataset: List<Article>) : super() {
        this.context = context
        this.dataset = dataset
        aozoraStatsHelper = AozoraStatsHelper.GetInstance(context)
        knowledgeService = KnowledgeService.GetInstance(context)
    }

    private lateinit var onItemClickListener: OnItemClickListener
    private lateinit var aozoraStatsHelper: AozoraStatsHelper
    private lateinit var knowledgeService: KnowledgeService

    // Provide a reference to the views for each data item
    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.article_item_title)
        val levelTextView: TextView = view.findViewById(R.id.article_item_level)
        val summaryTextView: TextView = view.findViewById(R.id.article_item_summary)
        val percentKnownTextView: TextView = view.findViewById(R.id.article_item_known_words)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_article_item, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.titleTextView.text = item.title
        holder.levelTextView.text = item.level.toString()
        holder.summaryTextView.text = item.summary
        holder.itemView.setOnClickListener(View.OnClickListener {
            onItemClickListener.onItemClick(
                position
            )
        })

        // TODO look into making this async and then updating the display as the calculation finishes.
        // Check article against user knowledge level
        val wordList = aozoraStatsHelper.getUniqueWords(item.bookId)
        val percentKnown = knowledgeService.GetPercentKnown(wordList)
        holder.percentKnownTextView.setTextColor(ContextCompat.getColor(context, R.color.purple_700))
        holder.percentKnownTextView.text = "%.0f".format(percentKnown) + "%"
        // store the wordlist with the article item itself.
        // Store as string b/c java fails to serialize json lol.
        item.wordList = wordList.toString()
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount() = dataset.size

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
