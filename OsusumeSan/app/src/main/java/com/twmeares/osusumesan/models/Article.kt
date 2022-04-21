package com.twmeares.osusumesan.models

import java.io.Serializable
import java.util.regex.Pattern
import java.util.regex.Pattern.DOTALL


class Article(val title: String, val level: Double, val summary: String, val bookId: String) : Serializable {
    var text: String = ""
    var wordList: String = "" //String form of jsonObject to get around java serialization problem
    fun ProcessText(){
        //remove ruby from text, etc.
        val rubyRegex = """(《.*?》)"""
        text = text.replace(rubyRegex.toRegex(), "")
        val dashes = "-------------------------------------------------------"
        val dashExpression = "(" + dashes + ".*" + dashes + ")"
        text = text.replace(dashExpression.toRegex(RegexOption.DOT_MATCHES_ALL), "")
        val indentRegex = """(［＃.*?］)"""
        text = text.replace(indentRegex.toRegex(), "\t")
        text = text.replace("""(\r\n\r\n)""".toRegex(), "\r\n")
        text = text.replace("\r\n", "\n")
    }
}
