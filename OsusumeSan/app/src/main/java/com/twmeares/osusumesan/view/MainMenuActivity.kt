package com.twmeares.osusumesan.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.twmeares.osusumesan.databinding.ActivityMainMenuBinding
import com.twmeares.osusumesan.viewmodels.MainMenu

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var mainMenu: MainMenu
    private val TAG: String = "MainMenuActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainMenu = MainMenu(this)
        //TODO add one time only content here like loading db's
        setupClickListeners(binding.root)
    }

    override fun onResume() {
        super.onResume()
        //TODO eventually need to check for enable/disable the resume button
    }

    private fun setupClickListeners(view: View) {

        binding.btnReadingList.setOnClickListener {
            Log.i(TAG, "clicked btnReadingList")
            mainMenu.StartReadingList()
        }

        binding.btnResumeReading.setOnClickListener {
            Log.i(TAG, "clicked btnResumeReading")
            mainMenu.StartResumeReading()
        }

        binding.btnInputText.setOnClickListener {
            Log.i(TAG, "clicked btnInputText")
            mainMenu.StartInputText()
        }

        binding.btnSettings.setOnClickListener {
            Log.i(TAG, "clicked btnSettings")
            mainMenu.StartSettings()
        }

    }
}