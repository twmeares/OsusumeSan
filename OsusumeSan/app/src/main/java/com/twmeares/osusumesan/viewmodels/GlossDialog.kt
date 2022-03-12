package com.twmeares.osusumesan.viewmodels

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.*
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.twmeares.osusumesan.databinding.GlossFragmentBinding
import android.view.Gravity
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
        //TODO use span for style??
//        val glossDetailsSSB = SpannableStringBuilder(arguments?.getString(KEY_SUBTITLE))
//        //glossDetailsSSB.setSpan(StyleSpan(Typeface.BOLD), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        binding.glossDetails.text = glossDetailsSSB
        var dictResult: DictionaryResult = arguments?.getSerializable(KEY_DICT_RESULT) as DictionaryResult
        binding.glossTitle.text = dictResult.dictForm
        binding.glossDetails.text = dictResult.meanings.first()
    }

    private fun setupClickListeners(view: View) {
        binding.btnPositive.setOnClickListener {
            // TODO: Do some task here
            dismiss()
        }
        binding.btnNegative.setOnClickListener {
            // TODO: Do some task here
            // probably going to remove this listener and it's button.
            dismiss()
        }
    }

}