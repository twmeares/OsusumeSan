package com.twmeares.osusumesan.services

import android.content.Context
import androidx.startup.Initializer
import com.twmeares.osusumesan.models.OsusumeSanTokenizer

class TokenizerInitializer : Initializer<OsusumeSanTokenizer> {
    override fun create(context: Context): OsusumeSanTokenizer {
        val useSudachi = false
        var tokenizer: OsusumeSanTokenizer
        if (useSudachi){
            var sysDictHelper =
                SysDictHelper(context)
            sysDictHelper.createDataBase()
            var dict = sysDictHelper.dictionary
            tokenizer = OsusumeSanTokenizer(dict)
        } else {
            tokenizer = OsusumeSanTokenizer()
        }
        return tokenizer
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}