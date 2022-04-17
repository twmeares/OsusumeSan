package com.twmeares.osusumesan.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.models.DictionaryResult

// Onclick setup based on https://stackoverflow.com/questions/24471109/recyclerview-onclick?page=1&tab=scoredesc#tab-top
// General adapter code based on https://developer.android.com/codelabs/basic-android-kotlin-training-recyclerview-scrollable-list?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-2-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-recyclerview-scrollable-list#4

/**
 * Adapter for the [RecyclerView] in [SearchActivity].
 */
class SearchItemAdapter : RecyclerView.Adapter<SearchItemAdapter.ItemViewHolder> {

    private val context: Context
    private val dataset: List<DictionaryResult>

    constructor(context: Context, dataset: List<DictionaryResult>) : super() {
        this.context = context
        this.dataset = dataset
    }

    private lateinit var onItemClickListener: OnItemClickListener

    // Provide a reference to the views for each data item
    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val addBtn: ImageButton = view.findViewById(R.id.search_item_add)
        val titleTextView: TextView = view.findViewById(R.id.search_item_title)
        val detailsTextView: TextView = view.findViewById(R.id.search_item_details)
        val jlptTextView: TextView = view.findViewById(R.id.search_item_jlpt)
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
            .inflate(R.layout.list_search_item, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        var title = item.dictForm + "   " + item.reading
        title = title.trim()
        val titleSSB = SpannableStringBuilder(title)
        if (!item.dictForm.equals("")){
            val titleBoldStart = 0
            val titleBoldEnd = item.dictForm.length
            titleSSB.setSpan(StyleSpan(Typeface.BOLD), titleBoldStart, titleBoldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        holder.titleTextView.text = titleSSB
        
        val detailsBuilder = StringBuilder()
        for (idx in 0 until item.meanings.size){
            if (idx > 0){
                detailsBuilder.append("\n")
            }

            detailsBuilder.append((idx + 1).toString() + ". ")
            detailsBuilder.append(item.meanings[idx])

            if (idx < item.pos.size && !item.pos[idx].equals("")) {
                detailsBuilder.append(" (")
                detailsBuilder.append(item.pos[idx])
                detailsBuilder.append(")")
            }


            if (idx < item.tags.size && !item.tags[idx].equals("")) {
                detailsBuilder.append(" (")
                detailsBuilder.append(item.tags[idx])
                detailsBuilder.append(")")
            }
        }
        holder.detailsTextView.text = detailsBuilder.toString()
        

        if (!item.jlptLvl.equals("[]")) {
            holder.jlptTextView.text = item.jlptLvl
        }
        else {
            // must set to empty as not setting it at all causes the view to display incorrect data.
            holder.jlptTextView.text = "" // set to empty
        }
        
        holder.addBtn.setOnClickListener(View.OnClickListener {
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
