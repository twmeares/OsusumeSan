package com.twmeares.osusumesan.viewmodels

import android.os.Bundle
import android.view.*
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.twmeares.osusumesan.databinding.GlossFragmentBinding
import android.view.Gravity




//portions of this dialog class based on https://blog.mindorks.com/implementing-dialog-fragment-in-android

class GlossDialog : DialogFragment() {

    companion object {
        const val TAG = "GlossDialog"

        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_SUBTITLE = "KEY_SUBTITLE"
        private var _binding: GlossFragmentBinding? = null
        // This property is only valid between onCreateView and
        // onDestroyView.
        private val binding get() = _binding!!

        fun newInstance(title: String, subTitle: String): GlossDialog {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putString(KEY_SUBTITLE, subTitle)
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
        binding.glossTitle.text = arguments?.getString(KEY_TITLE)
        binding.glossDetails.text = arguments?.getString(KEY_SUBTITLE)
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