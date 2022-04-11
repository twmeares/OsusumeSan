package com.twmeares.osusumesan.models;

import static com.twmeares.osusumesan.utils.OsusumeSanUtils.isHiragana;
import static com.twmeares.osusumesan.utils.OsusumeSanUtils.isKatakana;

import com.atilika.kuromoji.ipadic.Token;
import com.mariten.kanatools.KanaConverter;
import com.twmeares.osusumesan.services.KnowledgeService;
import com.worksap.nlp.sudachi.Morpheme;

public class OsusumeSanToken {
    private Token token;
    private Morpheme morph;
    private String reading;
    private String literal;
    private String dictForm;
    private Boolean useSudachi = false;
    private Boolean isFuriganaEnabled = true;
    // Reference for unicode blocks: https://stackoverflow.com/questions/43418812/check-whether-a-string-contains-japanese-chinese-characters
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
        return dictForm.matches(REGEX_KANJI);
    }

    public Boolean getIsKanaWord() {
        return isKatakana(dictForm) || isHiragana(dictForm);
    }

    public Boolean getIsHiraganaWord() {
        return isHiragana(dictForm);
    }

    public String getReading(){
        return reading;
    }

    public String getDictForm() { return dictForm; }

    public String getLiteral() {
        return literal;
    }

    public int getPosition() {
        if (useSudachi){
            return morph.begin();
        } else {
            return token.getPosition();
        }
    }

    public Boolean getIsFuriganaEnabled(){
        return isFuriganaEnabled;
    }

    public void setIsFuriganaEnabled(Boolean isEnabled){
        isFuriganaEnabled = isEnabled;
    }

    private void ConfigureToken(){
        if (useSudachi){
            reading = morph.readingForm();
            dictForm = morph.dictionaryForm();
            literal = morph.surface();
        } else {
            reading = token.getReading();
            dictForm = token.getBaseForm();
            literal = token.getSurface();
        }

        // If literal is hiragana/katakana while reading/dictForm are * it means the token wasn't
        // found in sudachi/kuromoji. Override them with the literal's value
        if (useSudachi && reading.equals("") && (isKatakana(literal) || isHiragana(literal))) {
            // Sudachi uses reading = "" to represent this case.
            reading = literal;
            dictForm = literal;
        }
        else if (reading.equals("*") && dictForm.equals("*") && (isKatakana(literal) || isHiragana(literal)) ) {
            // Kuromoji uses "*" to represent this case.
            reading = literal;
            dictForm = literal;
        }

        if (getIsKanjiWord() || getIsHiraganaWord()){
            // Convert reading from dictionary supplied katakana to hiragana
            int conversion_flags = KanaConverter.OP_ZEN_KATA_TO_ZEN_HIRA;
            reading = KanaConverter.convertKana(reading, conversion_flags);
        }
    }

}
