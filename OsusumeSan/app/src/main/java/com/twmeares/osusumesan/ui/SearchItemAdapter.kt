package com.twmeares.osusumesan.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.models.DictionaryResult
import com.twmeares.osusumesan.services.iDictionaryLookupService

// Onclick setup based on https://stackoverflow.com/questions/24471109/recyclerview-onclick?page=1&tab=scoredesc#tab-top
// General adapter code based on https://developer.android.com/codelabs/basic-android-kotlin-training-recyclerview-scrollable-list?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-2-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-recyclerview-scrollable-list#4

/**
 * Adapter for the [RecyclerView] in [SearchActivity].
 */
class SearchItemAdapter : RecyclerView.Adapter<SearchItemAdapter.ItemViewHolder> {

    private val TAG: String = "SearchItemAdapter"
    private val context: Context
    private val dataset: List<DictionaryResult>
    private var dictService: iDictionaryLookupService
    private val displayDictCallback = iDictionaryLookupService.Callback(::GetSingleDictResult)

    constructor(context: Context, dataset: List<DictionaryResult>, dictService: iDictionaryLookupService ) : super() {
        this.context = context
        this.dataset = dataset
        this.dictService = dictService
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
        if (!dictService.IsInQueue(item.dictForm) && item.meanings == null){
            // Call the dict service if the word isn't already in the lookup queue and its meaning is empty
            Log.d(TAG, "calling Jisho search for ${item.dictForm}")
            dictService.Search(item.dictForm, "", false, displayDictCallback)
        }
        var title = item.dictForm + "   " + item.reading
        title = title.trim()
        val titleSSB = SpannableStringBuilder(title)
        if (!item.dictForm.equals("")){
            val titleBoldStart = 0
            val titleBoldEnd = item.dictForm.length
            titleSSB.setSpan(StyleSpan(Typeface.BOLD), titleBoldStart, titleBoldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        holder.titleTextView.text = titleSSB

        if (item.meanings != null){
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
        } else {
            // set it empty
            holder.detailsTextView.text = ""
        }


        if (!"[]".equals(item.jlptLvl)) {
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

    fun GetSingleDictResult(dictResult: DictionaryResult) {
        var item = dataset.firstOrNull() { it.meanings == null
                && ( it.dictForm.equals(dictResult.dictForm)
                || (it.dictForm.equals(dictResult.reading) && !dictResult.reading.equals(""))  )}
        if (item != null){
            // Update the dataset with the result from the dictionary service.
            Log.d(TAG, "got dictionary result for ${item.dictForm}. Reading is ${dictResult.reading}")
            item.dictForm = dictResult.dictForm
            item.reading = dictResult.reading
            item.meanings = dictResult.meanings
            item.jlptLvl = dictResult.jlptLvl
            item.tags = dictResult.tags
            item.pos = dictResult.pos
            this.notifyDataSetChanged()
        }
    }
}
