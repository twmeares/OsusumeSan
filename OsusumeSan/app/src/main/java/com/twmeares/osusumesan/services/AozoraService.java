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
import com.twmeares.osusumesan.models.Article;
import com.twmeares.osusumesan.models.DictionaryResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AozoraService {
    private final String URL_PREFIX = "https://pubserver2.herokuapp.com/api/v0.1/";
    // In case the above url ever stops working it seems there is another version at
    // http://www.aozorahack.net/api/v0.1, but it's an older version of the same thing essentially.
    private static final String TAG = "AozoraService";
    private RequestQueue queue;
    private Context context;

    public AozoraService(Context context){
        this.context = context;
        queue = Volley.newRequestQueue(this.context);
    }

    public interface Callback {
        void OnArticleReceived(Article result);
    }

    public void FetchArticle(Article article, Callback callback) {
        queue.cancelAll(this);

        StringRequest stringRequest = FetchArticleRequest(article, callback);
        stringRequest.setTag(this);

        queue.add(stringRequest);
    }

    private StringRequest FetchArticleRequest(Article article, Callback callback) {

        String url = URL_PREFIX + "books/" + article.getBookId() + "/content?format=txt";

        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String text = response;
                            article.setText(response);
                            article.ProcessText();
                            callback.OnArticleReceived(article);
                        } catch (Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "problem with parsing json. EX:" + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // display a simple message on the screen
                        Toast.makeText(context, "Got an error response from Aozora Bunko while searching.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error from Aozora Bunko while searching. " + error.getMessage());
                    }
                });
    }
}
