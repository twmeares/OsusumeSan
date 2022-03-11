package com.twmeares.osusumesan.services;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DictionaryLookupService implements iDictionaryLookupService{
    private final String URL_PREFIX = "https://jisho.org/api/v1/search/words?keyword=";
    private RequestQueue queue;
    private Context context;

    public DictionaryLookupService(Context context){
        this.context = context;
        queue = Volley.newRequestQueue(this.context);
    }

    @Override
    public void Search(String word, Callback callback) {
        queue.cancelAll(this);


        StringRequest searchrequest = BuildSearchStringRequest(word, callback);
        searchrequest.setTag(this);

        queue.add(searchrequest);
    }

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
                                    entry.put("matchFound", true);
                                    callback.DisplayDictResult(entry);
                                }
                            }
                            if (matchFound == false){
                                //TODO log no match found
                                JSONObject mismatchedResult = new JSONObject();
                                mismatchedResult.put("matchFound", false);
                                mismatchedResult.put("searchQuery", url.substring(URL_PREFIX.length()));
                                mismatchedResult.put("result", result);
                                callback.DisplayDictResult(mismatchedResult);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            //TODO add logging
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // display a simple message on the screen
                        Toast.makeText(context, "Got an error response from Jisho while searching.", Toast.LENGTH_LONG).show();
                        //TODO add logging
                    }
                });
    }


}
