package com.twmeares.osusumesan.services;

import com.twmeares.osusumesan.models.DictionaryResult;

import org.json.JSONObject;

import java.util.List;

public interface iDictionaryLookupService {

    public void Search(String word, String reading, Boolean isFuriganaEnabled, Callback callback);
    public void SearchMultiResult(String word, MultiResultCallback callback);

    public interface Callback {
        void DisplayDictResult(DictionaryResult result);
    }

    public interface MultiResultCallback {
        void DisplayDictResult(List<DictionaryResult> result);
    }
}
