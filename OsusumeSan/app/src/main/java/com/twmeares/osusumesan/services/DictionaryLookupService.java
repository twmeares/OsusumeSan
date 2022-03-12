package com.twmeares.osusumesan.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.twmeares.osusumesan.models.DictionaryResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DictionaryLookupService implements iDictionaryLookupService{
    private final String URL_PREFIX = "https://jisho.org/api/v1/search/words?keyword=";
    private static final String TAG = "DictionaryLookupService";
    private RequestQueue queue;
    private Context context;

    public DictionaryLookupService(Context context){
        this.context = context;
        queue = Volley.newRequestQueue(this.context);
    }

    // Searches dictionary and returns a single exact match only.
    @Override
    public void Search(String word, Callback callback) {
        queue.cancelAll(this);

        StringRequest stringRequest = BuildSearchStringRequest(word, callback);
        stringRequest.setTag(this);

        queue.add(stringRequest);
    }

    // TODO: in the future could create a SearchMany method which returns close matches.
    // Would just need to remove the matchFound logic and return if any result is found.

    private StringRequest BuildSearchStringRequest(String word, Callback callback) {
        //Code modified based on https://wtmimura.com/post/calling-api-on-android-studio/
        String url = URL_PREFIX + word;

        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray result = new JSONObject(response).getJSONArray("data");
                            Boolean matchFound = false;
                            for (int i = 0 ; i < result.length(); i++) {
                                JSONObject entry = result.getJSONObject(i);
                                if (word.equals(entry.getString("slug"))){
                                    matchFound = true;
                                    //entry.put("matchFound", true);

                                    String dictForm = entry.getJSONArray("japanese").getJSONObject(0).optString("word", "");
                                    String reading = entry.getJSONArray("japanese").getJSONObject(0).getString("reading");
                                    List<String> meanings = new ArrayList<>();
                                    List<String> pos = new ArrayList<>();
                                    JSONArray sensesArray = entry.getJSONArray("senses");

                                    // each entry can have multiple senses
                                    for (int j = 0 ; j < sensesArray.length(); j++) {
                                        StringBuilder sensesBuilder = new StringBuilder();
                                        StringBuilder posBuilder = new StringBuilder();
                                        JSONArray senses = sensesArray.getJSONObject(j).getJSONArray("english_definitions");
                                        JSONArray posArray = sensesArray.getJSONObject(j).getJSONArray("parts_of_speech");
                                        // each english definition can have multiple values
                                        // this for is a big ugly but tostring on the array and .Join didn't give the format
                                        // I wanted and I didn't want to strip quotes in case they were part of the entry.
                                        for (int k = 0 ; k < senses.length(); k++) {
                                            if (k>0){
                                                sensesBuilder.append(", ");
                                            }
                                            String sense = senses.getString(k);
                                            sensesBuilder.append(sense);
                                        }
                                        meanings.add(sensesBuilder.toString());

                                        // extract the parts of seach for this SensesArray Entry.
                                        for (int k = 0 ; k < posArray.length(); k++) {
                                            if (k>0){
                                                posBuilder.append(", ");
                                            }
                                            String posValue = posArray.getString(k);
                                            posBuilder.append(posValue);
                                        }
                                        pos.add(posBuilder.toString());
                                    }
                                    String jlptLvl = entry.getString("jlpt")
                                            .replace("jlpt-", "")
                                            .replace("\"", "")
                                            .replace("[", "")
                                            .replace("]", "");
                                    DictionaryResult dictResult = new DictionaryResult(dictForm, reading, meanings, jlptLvl, pos);
                                    callback.DisplayDictResult(dictResult);
                                    break;
                                }
                            }
                            if (matchFound == false){
                                Log.d(TAG, "No exact dictionary match found");
                                //JSONObject mismatchedResult = new JSONObject();
                                //mismatchedResult.put("matchFound", false);
                                //mismatchedResult.put("searchQuery", url.substring(URL_PREFIX.length()));
                                //mismatchedResult.put("result", result);
                                //callback.DisplayDictResult(mismatchedResult);
                                String searchQuery = url.substring(URL_PREFIX.length());
                                String msg = "No exact match found for " + searchQuery;
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "problem with parsing json. EX:" + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // display a simple message on the screen
                        Toast.makeText(context, "Got an error response from Jisho while searching.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error from Jisho while searching. " + error.getMessage());
                    }
                });
    }


}
