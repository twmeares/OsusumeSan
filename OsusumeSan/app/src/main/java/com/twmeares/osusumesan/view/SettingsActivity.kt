package com.twmeares.osusumesan.view

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.twmeares.osusumesan.R

import androidx.preference.EditTextPreference

import android.content.SharedPreferences
import android.util.Log
import android.view.View
import com.twmeares.osusumesan.databinding.SettingsActivityBinding
import com.twmeares.osusumesan.services.KnowledgeService


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding
    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListeners(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupClickListeners(view: View) {

        binding.btnEditKnowledge.setOnClickListener {
            Log.i(TAG, "clicked Edit Knowledge")
            openKnowledgeList()
        }
    }

    fun openKnowledgeList() {
        val intent = Intent(this, KnowledgeListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(intent)
    }


    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

        private lateinit var knowledgeService: KnowledgeService
        private lateinit var books: List<String>
        private lateinit var jlptLvls: List<String>

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            books = listOf(
                getString(R.string.tobira_key),
                getString(R.string.genki1_key),
                getString(R.string.genki2_key)
            )

            jlptLvls = listOf(
                getString(R.string.n1_key),
                getString(R.string.n2_key),
                getString(R.string.n3_key),
                getString(R.string.n4_key),
                getString(R.string.n5_key)
            )

            //force the preference to be numeric only.
            val editTextPreference =
                preferenceManager.findPreference<EditTextPreference>("furigana_tapper_preference")
            editTextPreference!!.setOnBindEditTextListener { editText ->
                editText.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            }

            knowledgeService = KnowledgeService.GetInstance(this.context)

        }

        override fun onResume() {
            super.onResume()
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        override fun onPause() {
            super.onPause()
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            // Update the knowledge model with the settings based on user's choice.
            if (books.contains(key)) {
                val isKnown = sharedPreferences.getBoolean(key, false)
                val book = key.replace("_preference", "")
                knowledgeService.UpdateKnowledgeBook(book, isKnown)
            } else if(jlptLvls.contains(key)){
                val isKnown = sharedPreferences.getBoolean(key, false)
                val jlptLvl = key.replace("_preference", "")
                knowledgeService.UpdateKnowledgeJLPT(jlptLvl, isKnown)
            }
        }
    }
}