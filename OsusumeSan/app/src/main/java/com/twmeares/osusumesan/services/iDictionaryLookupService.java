package com.twmeares.osusumesan.services;

import org.json.JSONArray;

public interface iDictionaryLookupService {

    public JSONArray Search(String word, Callback callback);

    public interface Callback {
        void DisplayDictResult(JSONArray result);
    }
}
