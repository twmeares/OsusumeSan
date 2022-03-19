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
import com.mariten.kanatools.KanaConverter;
import com.twmeares.osusumesan.models.DictionaryResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public void Search(String word, String reading, Boolean isFuriganaEnabled, Callback callback) {
        queue.cancelAll(this);

        StringRequest stringRequest = BuildSearchStringRequest(word, reading, isFuriganaEnabled, callback);
        stringRequest.setTag(this);

        queue.add(stringRequest);
    }

    // TODO: in the future could create a SearchMany method which returns close matches.
    // Would just need to remove the matchFound logic and return if any result is found.

    private StringRequest BuildSearchStringRequest(String word, String reading, Boolean isFuriganaEnabled, Callback callback) {
        //Code modified based on https://wtmimura.com/post/calling-api-on-android-studio/
        String url = URL_PREFIX + word;

        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray result = new JSONObject(response).getJSONArray("data");
                            Boolean matchFound = false;
                            Boolean readingOnlyMatch = false;
                            for (int i = 0 ; i < result.length(); i++) {
                                JSONObject entry = result.getJSONObject(i);
                                String pattern = word + "-{0,1}[0-9]{0,9}";
                                Pattern r = Pattern.compile(pattern);
                                Matcher m = r.matcher(entry.getString("slug"));
                                if (!entry.has("japanese") || !entry.getJSONArray("japanese").getJSONObject(0).has("reading")) {
                                   continue;
                                }
                                String entryReading = entry.getJSONArray("japanese").getJSONObject(0).getString("reading");

                                int conversion_flags = KanaConverter.OP_ZEN_KATA_TO_ZEN_HIRA;
                                if (m.find()
                                    && ( reading.equals(entryReading)
                                    || reading.equals( KanaConverter.convertKana(entryReading, conversion_flags)) ))
                                {
                                    matchFound = true;
                                    DictionaryResult dictResult = ExtractDictionaryResult(word, reading, isFuriganaEnabled, entry);
                                    callback.DisplayDictResult(dictResult);
                                    break;
                                } else if ( reading.equals(entryReading)
                                        || reading.equals( KanaConverter.convertKana(entryReading, conversion_flags)) )
                                {
                                    // Since multi matches could be found with only the reading we'll only consider the first one
                                    if (readingOnlyMatch == false) {
                                        readingOnlyMatch = true;
                                        // Check for a match of only the reading without checking the slug.
                                        // This case is useful for words that are usually only kana or
                                        // words that usually have kanji but the kanji wasn't used in the
                                        // input text.
                                        DictionaryResult dictResult = ExtractDictionaryResult(word, reading, isFuriganaEnabled, entry);
                                        callback.DisplayDictResult(dictResult);
                                        break;
                                    }
                                } else if ( word.equals(entryReading) || word.equals( KanaConverter.convertKana(entryReading, conversion_flags)) )
                                {
                                    // Since multi matches could be found with only the reading we'll only consider the first one
                                    if (readingOnlyMatch == false) {
                                        readingOnlyMatch = true;
                                        // Check for a match of only the reading without checking the slug.
                                        // This case is useful for words that are usually only kana or
                                        // words that usually have kanji but the kanji wasn't used in the
                                        // input text.

                                        // throwing out the reading here and passing the word twice since the reading is likely too
                                        // specific to the current usage and that's why this case matched for word isntead of reading
                                        DictionaryResult dictResult = ExtractDictionaryResult(word, word, isFuriganaEnabled, entry);
                                        callback.DisplayDictResult(dictResult);
                                        break;
                                    }
                                }
                            }
                            if (matchFound == false && readingOnlyMatch == false){
                                //Likely the case for proper names.
                                Log.d(TAG, "No exact dictionary match found");
                                String msg = "No exact match found for " + word;
                                List<String> meanings = new ArrayList<>();
                                List<String> pos = new ArrayList<>();
                                List<String> tags = new ArrayList<>();
                                meanings.add(msg);
                                DictionaryResult dictResult = new DictionaryResult(word, reading, meanings, "", pos, tags, isFuriganaEnabled);
                                callback.DisplayDictResult(dictResult);
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

    private DictionaryResult ExtractDictionaryResult(String word, String reading, Boolean isFuriganaEnabled, JSONObject entry) throws JSONException {
        String dictForm = entry.getJSONArray("japanese").getJSONObject(0).optString("word", "");
        //String reading = entry.getJSONArray("japanese").getJSONObject(0).getString("reading");
        List<String> meanings = new ArrayList<>();
        List<String> pos = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        JSONArray sensesArray = entry.getJSONArray("senses");

        // each entry can have multiple senses
        for (int j = 0 ; j < sensesArray.length(); j++) {
            StringBuilder sensesBuilder = new StringBuilder();
            StringBuilder posBuilder = new StringBuilder();
            StringBuilder tagsBuilder = new StringBuilder();
            JSONArray senses = sensesArray.getJSONObject(j).getJSONArray("english_definitions");
            JSONArray posArray = sensesArray.getJSONObject(j).getJSONArray("parts_of_speech");
            JSONArray tagsArray = sensesArray.getJSONObject(j).getJSONArray("tags");
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

            // extract the tags for this SensesArray Entry. e.g. (Usually written kana only)
            for (int k = 0 ; k < tagsArray.length(); k++) {
                if (k>0){
                    tagsBuilder.append(", ");
                }
                String tagValue = tagsArray.getString(k);
                tagsBuilder.append(tagValue);
            }
            tags.add(tagsBuilder.toString());
        }
        String jlptLvl = entry.getString("jlpt")
                .replace("jlpt-", "")
                .replace("\"", "");
        DictionaryResult dictResult = new DictionaryResult(dictForm, reading, meanings, jlptLvl, pos, tags, isFuriganaEnabled);
        return dictResult;
    }


}
