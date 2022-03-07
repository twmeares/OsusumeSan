package com.twmeares.osusumesan.models;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.Morpheme;

import java.util.List;
import java.util.stream.Collectors;

public class OsusumeSanTokenizer {

    public OsusumeSanTokenizer(){
        tokenizer = new Tokenizer();
    }
    public OsusumeSanTokenizer(Dictionary dict){
        sudachiTokenizer = dict.create();
    }

    com.worksap.nlp.sudachi.Tokenizer sudachiTokenizer;
    Tokenizer tokenizer;

    public List<OsusumeSanToken> Tokenize(String text){
        try {
            if (tokenizer == null) {
                List<Morpheme> tokens = sudachiTokenizer.tokenize(text);
                List<OsusumeSanToken> osusumeSanTokens = tokens.stream().map(OsusumeSanToken::new).collect(Collectors.toList());
                return osusumeSanTokens;
            } else {
                List<Token> tokens = tokenizer.tokenize(text);
                List<OsusumeSanToken> osusumeSanTokens = tokens.stream().map(OsusumeSanToken::new).collect(Collectors.toList());
                return osusumeSanTokens;
            }
        } catch (Exception ex){
            //TODO log exception, don't think there's a need to throw it.
            return null;
        }

    }
}
