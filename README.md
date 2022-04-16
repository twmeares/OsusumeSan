# OsusumeSan
Android App for Japanese Reading Recommendations

# Library Dependencies
JMDictFurigana for nicely placing furigana over the corresponding kanji. https://github.com/Doublevil/JmdictFurigana

Kuromoji

Sudachi

RubySpan for displaying furigana in Android as ruby characters using ReplacementSpan. https://github.com/mljli/rubyspan

Kanatools for kana text conversion. https://github.com/mariten/kanatools-java

# Scripts
Several python jupyter notebooks were used for creating sqlite databases for use in the android app

JMDictDBCreator: converts the txt file version of jmdictfurigana to a sqlite DB

KnowledgeDBCreator: takes input files of vocabulary from textbooks/JLPT levels and created a sqlite DB for holding the users known/unknown words. The DB is used throughout the android app to track the users known words.

Aozora_extract: queries the API's from https://github.com/aozorahack/pubserver2 to get article content from Aozora Bunko. The article content is then graded according to the linear regression formula from Lee and Hasebe at http://jhlee.sakura.ne.jp/papers/lee-et-al2016rb.pdf. The python code implements custom logic to extract features from the text using a heuristic. These features include the number of Kango (Chinese origin), Wago (Japanese origin), verbs, auxillary, and sentence length. These stats are the input to the linear regression. The rules for extracting Kango and Wago are based on loose grammatical and phonetic concepts. The code works well to extract the words but there are many edge cases and the extraction is not guaranteed to be 100% correct.

#Resources used for creating the app's vocab list by book/resource

JLPT Vocab CSV's taken from https://gist.github.com/nakaly/c2c855766ce1661e11396b09760b9598

Genki 1 & 2 vocab gathered from  http://genki.japantimes.co.jp/resources/saku_tango

Tobira vocab was taken from the anki desks at https://tobiraweb.9640.jp/contents/%e6%bc%a2%e5%ad%97%e3%83%bb%e8%aa%9e%e5%bd%99%e6%95%99%e6%9d%90/
