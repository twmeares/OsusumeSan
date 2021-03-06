package com.twmeares.osusumesan.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.twmeares.osusumesan.databinding.ActivityInputTextBinding
import com.twmeares.osusumesan.databinding.ActivityReadingBinding

class InputTextActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputTextBinding
    private val TAG: String = "InputTextActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListeners(binding.root)
    }

    private fun setupClickListeners(view: View) {

        binding.btnInputText.setOnClickListener {
            Log.i(TAG, "clicked process text")

            val inputText = binding.editTextInputBox.text.toString()
            if (inputText == null || inputText.equals("")){
                val msg = "Input text cannot be empty."
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, ReadingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("inputText", inputText)
                this.startActivity(intent)
            }
        }

    }

}