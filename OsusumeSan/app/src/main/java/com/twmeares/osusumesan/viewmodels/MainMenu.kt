package com.twmeares.osusumesan.viewmodels

import android.content.Intent

import android.app.Activity
import android.util.Log
import com.twmeares.osusumesan.services.JMDictFuriHelper
import com.twmeares.osusumesan.services.KnowledgeService
import com.twmeares.osusumesan.view.InputTextActivity
import com.twmeares.osusumesan.view.ReadingActivity
import com.twmeares.osusumesan.view.ReadingListActivity
import com.twmeares.osusumesan.view.SettingsActivity


class MainMenu() {
    private var activity: Activity? = null
    private val TAG: String = "MainMenu"
    private lateinit var knowledgeService: KnowledgeService
    private lateinit var jmDictFuriHelper: JMDictFuriHelper
    private var runOnce: Boolean = true

//    fun MainMenu(context: Activity?) {
//        activity = context
//    }

    constructor(context: Activity?) : this() {
        this.activity = context
        knowledgeService = KnowledgeService.GetInstance(this.activity)

        if(runOnce){
            // TODO swtich to app startup ? see https://developer.android.com/topic/libraries/app-startup
//            jmDictFuriHelper = JMDictFuriHelper(this.activity)
//            jmDictFuriHelper.createDataBase()
            runOnce = false
        }
    }

    fun StartReadingList() {
        Log.i(TAG, "MainMenu StartReadingList")
        val intent = Intent(activity, ReadingListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity!!.startActivity(intent)
    }

    fun StartResumeReading() {
        Log.i(TAG, "MainMenu StartResumeReading")
        val intent = Intent(activity, ReadingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        // TODO probably need some putExtra to tell the activity which reading task to load.
        // Might need to handle differently between switch between pages and going back to reading
        // vs when the app was completely closed and then trying to go back to the last reading.
        //intent.putExtra("jobOffer", true)
        activity!!.startActivity(intent)
    }

    fun StartInputText() {
        Log.i(TAG, "MainMenu StartInputText")
        val intent = Intent(activity, InputTextActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity!!.startActivity(intent)
    }

    fun StartSettings() {
        Log.i(TAG, "MainMenu StartSettings")
        val intent = Intent(activity, SettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity!!.startActivity(intent)
    }
}