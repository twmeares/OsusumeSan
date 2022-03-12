package com.twmeares.osusumesan.services;

import com.twmeares.osusumesan.models.DictionaryResult;

import org.json.JSONObject;

public interface iDictionaryLookupService {

    public void Search(String word, Callback callback);

    public interface Callback {
        void DisplayDictResult(DictionaryResult result);
    }
}
