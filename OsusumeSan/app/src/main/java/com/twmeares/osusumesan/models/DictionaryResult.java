package com.twmeares.osusumesan.models;

import java.io.Serializable;
import java.util.List;

public class DictionaryResult implements Serializable {

    private String reading;
    private String dictForm;
    private List<String> meanings;
    private String jlptLvl;

    public DictionaryResult(String dictForm, String reading, List<String> meanings, String jlptLvl){
        this.reading = reading;
        this.dictForm = dictForm;
        this.meanings = meanings;
        this.jlptLvl = jlptLvl;
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

    public String getJlptLvl() {
        return jlptLvl;
    }

}
