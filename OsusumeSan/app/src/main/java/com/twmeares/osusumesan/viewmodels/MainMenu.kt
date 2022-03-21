package com.twmeares.osusumesan.viewmodels

import android.content.Intent

import android.app.Activity
import android.util.Log
import com.twmeares.osusumesan.view.ReadingActivity


class MainMenu() {
    private var activity: Activity? = null
    private val TAG: String = "MainMenu"

//    fun MainMenu(context: Activity?) {
//        activity = context
//    }

    constructor(context: Activity?) : this() {
        this.activity = context
    }

    fun StartReadingList() {
        Log.i(TAG, "MainMenu StartReadingList")
//        val intent = Intent(activity, ReadingListActivity class)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        activity!!.startActivity(intent)
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
//        val intent = Intent(activity, InputTextActivity class)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        activity!!.startActivity(intent)
    }

    fun StartSettings() {
        Log.i(TAG, "MainMenu StartSettings")
//        val intent = Intent(activity, SettingsActivity class)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        activity!!.startActivity(intent)
    }
}