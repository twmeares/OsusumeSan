package com.twmeares.osusumesan.models;

import static com.twmeares.osusumesan.utils.OsusumeSanUtils.isHiragana;
import static com.twmeares.osusumesan.utils.OsusumeSanUtils.isKatakana;

import com.atilika.kuromoji.ipadic.Token;
import com.mariten.kanatools.KanaConverter;
import com.worksap.nlp.sudachi.Morpheme;

public class OsusumeSanToken {
    Token token;
    Morpheme morph;
    String reading;
    Boolean useSudachi = false;
    private static String REGEX_KANJI = ".*[\\u3400-\\u4dbf\\u4e00-\\u9fff\\uf900-\\ufaff].*";

    OsusumeSanToken(Token token){
        this.token = token;
        ConfigureToken();
    }

    OsusumeSanToken(Morpheme morph){
        this.morph = morph;
        useSudachi = true;
        ConfigureToken();
    }

    public Token getToken(){
        return token;
    }

    public Boolean getIsKanjiWord() {
        if (useSudachi){
            return morph.dictionaryForm().matches(REGEX_KANJI);
        } else {
            return token.getBaseForm().matches(REGEX_KANJI);
        }
        //return !isKatakana(token.getBaseForm()) && !isHiragana(token.getBaseForm());
    }

    public String getReading(){
        return reading;
    }

    public String getDictForm() {
        if (useSudachi){
            return morph.dictionaryForm();
        } else {
            return token.getBaseForm();
        }
    }

    public int getPosition() {
        if (useSudachi){
            return morph.begin();
        } else {
            return token.getPosition();
        }
    }

    public Boolean getIsFuriganaEnabled(){
        //TODO for now just return true. Eventually consult knowledge model.
        return true;
    }

    private void ConfigureToken(){
        if (getIsKanjiWord()){
            // Convert reading from dictionary supplied katakana to hiragana
            if (useSudachi){
                reading = morph.readingForm();
            } else {
                reading = token.getReading();
            }
            int conversion_flags = KanaConverter.OP_ZEN_KATA_TO_ZEN_HIRA;
            reading = KanaConverter.convertKana(reading, conversion_flags);
        } else {
            if (useSudachi){
                reading = morph.readingForm();
            } else {
                reading = token.getReading();
            }

        }
    }

}
