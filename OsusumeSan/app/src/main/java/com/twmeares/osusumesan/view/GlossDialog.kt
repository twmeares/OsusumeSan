package com.twmeares.osusumesan.view

import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.*
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.twmeares.osusumesan.databinding.GlossFragmentBinding
import android.view.Gravity
import com.twmeares.osusumesan.R
import com.twmeares.osusumesan.models.DictionaryResult


//portions of this dialog class based on https://blog.mindorks.com/implementing-dialog-fragment-in-android

class GlossDialog : DialogFragment() {

    companion object {
        const val TAG = "GlossDialog"

        private const val KEY_DICT_RESULT = "KEY_DICT_RESULT"
        private var _binding: GlossFragmentBinding? = null
        // This property is only valid between onCreateView and
        // onDestroyView.
        private val binding get() = _binding!!

        fun newInstance(dictResult: DictionaryResult): GlossDialog {
            val args = Bundle()

            args.putSerializable(KEY_DICT_RESULT, dictResult)
            val fragment = GlossDialog()
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = GlossFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
        setupClickListeners(view)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val window: Window? = dialog?.window
        if (window != null){
            val wlp = window.attributes
            wlp.gravity = Gravity.BOTTOM
            wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            window.attributes = wlp
        }
    }

    private fun setupView(view: View) {
        var dictResult: DictionaryResult = arguments?.getSerializable(KEY_DICT_RESULT) as DictionaryResult
        var title = dictResult.dictForm + "   " + dictResult.reading
        title = title.trim()
        val glossTitileSSB = SpannableStringBuilder(title)
        if (!dictResult.dictForm.equals("")){
            val titleBoldStart = 0
            val titleBoldEnd = dictResult.dictForm.length
            glossTitileSSB.setSpan(StyleSpan(Typeface.BOLD), titleBoldStart, titleBoldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.glossTitle.text = glossTitileSSB
        var glossDetails = StringBuilder()
        if (dictResult.meanings.size > 0) {
            glossDetails.append(dictResult.meanings.first())
        }

        if (dictResult.pos.size > 0) {
            glossDetails.append(" (")
            glossDetails.append(dictResult.pos.first())
            glossDetails.append(")")
        }


        if (dictResult.tags.size > 0 && !dictResult.tags.first().equals("")) {
            glossDetails.append(" (")
            glossDetails.append(dictResult.tags.first())
            glossDetails.append(")")
        }

        if (!dictResult.jlptLvl.equals("[]")) {
            glossDetails.append(" ")
            glossDetails.append(dictResult.jlptLvl)
        }

        binding.glossDetails.text = glossDetails.toString()
    }

    private fun setupClickListeners(view: View) {
        var dictResult: DictionaryResult = arguments?.getSerializable(KEY_DICT_RESULT) as DictionaryResult
        if (dictResult.dictForm.equals("")){
            // hide the btn and return early for words that don't have kanji i.e. no dictForm = "".
            binding.btnFurigana.visibility = View.GONE
            return
        }

        if (dictResult.isFuriganaEnabled){
            binding.btnFurigana.text = getString(R.string.hide_furigana)
        } else {
            binding.btnFurigana.text = getString(R.string.show_furigana)
        }

        binding.btnFurigana.setOnClickListener {
            // TODO: Do some task here
            // TODO pass the value of isFuriganaEnabled to whatever method gets added here.
            val activity : ReadingActivity? = getActivity() as? ReadingActivity
            if(activity != null){
                activity.UpdateFurigana(dictResult.dictForm, !dictResult.isFuriganaEnabled)
            }
            dismiss()
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity : ReadingActivity? = getActivity() as? ReadingActivity
        if(activity != null){
            activity.ClearTextSelection()
        }
    }

}