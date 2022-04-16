package com.twmeares.osusumesan.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.models.KnowledgeItem

// Onclick setup based on https://stackoverflow.com/questions/24471109/recyclerview-onclick?page=1&tab=scoredesc#tab-top
// General adapter code based on https://developer.android.com/codelabs/basic-android-kotlin-training-recyclerview-scrollable-list?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-2-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-recyclerview-scrollable-list#4

/**
 * Adapter for the [RecyclerView] in [KnowledgeListActivity].
 */
class KnowledgeItemAdapter : RecyclerView.Adapter<KnowledgeItemAdapter.ItemViewHolder> {

    private val context: Context
    private val dataset: List<KnowledgeItem>

    constructor(context: Context, dataset: List<KnowledgeItem>) : super() {
        this.context = context
        this.dataset = dataset
    }

    private lateinit var onItemClickListener: OnItemClickListener

    // Provide a reference to the views for each data item
    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val isKnownCheckBox: CheckBox = view.findViewById(R.id.knowledge_item_isknown)
        val wordTextView: TextView = view.findViewById(R.id.knowledge_item_word)
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
            .inflate(R.layout.list_knowledge_item, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.isKnownCheckBox.isChecked = item.isKnown
        holder.wordTextView.text = item.word
        // TODO consider adding the item click action back or adding another button for edit.
        // This could allow users to correct words that were incorrect or have odd characters like ().
//        holder.itemView.setOnClickListener(View.OnClickListener {
//            onItemClickListener.onItemClick(
//                position
//            )
//        })
        holder.isKnownCheckBox.setOnClickListener(View.OnClickListener {
            onItemClickListener.onItemClick(
                position
            )
        })
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount() = dataset.size

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
