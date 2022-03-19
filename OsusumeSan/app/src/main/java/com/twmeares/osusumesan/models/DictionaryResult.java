package com.twmeares.osusumesan.models;

import java.io.Serializable;
import java.util.List;

public class DictionaryResult implements Serializable {

    private String reading;
    private String dictForm;
    private List<String> meanings;
    private List<String> pos;
    private List<String> tags;
    private String jlptLvl;
    private Boolean isFuriganaEnabled;

    public DictionaryResult(String dictForm, String reading, List<String> meanings, String jlptLvl,
                            List<String> pos, List<String> tags, Boolean isFuriganaEnabled){
        this.reading = reading;
        this.dictForm = dictForm;
        this.meanings = meanings;
        this.jlptLvl = jlptLvl;
        this.pos = pos;
        this.tags = tags;
        this.isFuriganaEnabled = isFuriganaEnabled;
    }

    public String getReading() {
        return reading;
    }

    public String getDictForm() {
        return dictForm;
    }

    public List<String> getMeanings() {
        return meanings;
    }

    public List<String> getPos() {
        return pos;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getJlptLvl() {
        return jlptLvl;
    }

    public Boolean getIsFuriganaEnabled() {
        return isFuriganaEnabled;
    }
}
