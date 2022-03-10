package com.twmeares.osusumesan.services;

import org.json.JSONObject;

public interface iDictionaryLookupService {

    public void Search(String word, Callback callback);

    public interface Callback {
        void DisplayDictResult(JSONObject result);
    }
}
