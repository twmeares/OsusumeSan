package com.twmeares.osusumesan.models;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import java.util.List;
import java.util.stream.Collectors;

public class OsusumeSanTokenizer {

    public OsusumeSanTokenizer(){
        tokenizer = new Tokenizer();
    }

    Tokenizer tokenizer;

    public List<OsusumeSanToken> Tokenize(String text){
        try {
            List<Token> tokens = tokenizer.tokenize(text);
            List<OsusumeSanToken> osusumeSanTokens = tokens.stream().map(OsusumeSanToken::new).collect(Collectors.toList());
            return osusumeSanTokens;
        } catch (Exception ex){
            //TODO log exception, don't think there's a need to throw it.
            return null;
        }

    }
}
