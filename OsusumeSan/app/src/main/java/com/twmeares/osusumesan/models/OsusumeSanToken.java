package com.twmeares.osusumesan.models;

import static com.twmeares.osusumesan.utils.OsusumeSanUtils.isHiragana;
import static com.twmeares.osusumesan.utils.OsusumeSanUtils.isKatakana;

import com.atilika.kuromoji.ipadic.Token;
import com.mariten.kanatools.KanaConverter;

public class OsusumeSanToken {
    Token token;
    String reading;
    private static String REGEX_KANJI = ".*[\\u3400-\\u4dbf\\u4e00-\\u9fff\\uf900-\\ufaff].*";

    OsusumeSanToken(Token token){
        this.token = token;
        ConfigureToken();
    }

    public Token getToken(){
        return token;
    }

    public Boolean getIsKanjiWord() {
        return token.getBaseForm().matches(REGEX_KANJI);
        //return !isKatakana(token.getBaseForm()) && !isHiragana(token.getBaseForm());
    }

    public String getReading(){
        return reading;
    }

    public String getDictForm() {
        return token.getBaseForm();
    }

    public int getPosition() { return token.getPosition(); }

    public Boolean getIsFuriganaEnabled(){
        //TODO for now just return true. Eventually consult knowledge model.
        return true;
    }

    private void ConfigureToken(){
        if (getIsKanjiWord()){
            // Convert reading from dictionary supplied katakana to hiragana
            reading = token.getReading();
            int conversion_flags = KanaConverter.OP_ZEN_KATA_TO_ZEN_HIRA;
            reading = KanaConverter.convertKana(reading, conversion_flags);
        } else {
            reading = token.getReading();
        }
    }

}
