# OsusumeSan
OsusumeSan is an Android smartphone app for assisting foreign language learners in reading Japanese, a task that is often difficult due to a lack of access to level appropriate reading material which leads to being overwhelmed by unknown kanji. The app builds a model of the learner’s knowledge to provide level appropriate text recommendations and augments the text to include furigana for words unknown to the user. The app also provides dictionary information on word click in the form of a pop-up. Reading material used in the app is sourced from the online Aozora Bunko database of over 15 thousand works. The app also allows user input text and generates furigana and dictionary pop-up info for the text.  Osusumesan also features a dedicated dictionary search page and is capable of generated a vocab list of words unkonwn to the user for a given text.

This project would not be possible without the many contributions to the field made by previous researchers and developers. The list below details many of those works.

The app is still in alpha testing stage and will be released to the Google Play marketplace later this year.

# Library Dependencies
**JMDictFurigana** for nicely placing furigana over the corresponding kanji. https://github.com/Doublevil/JmdictFurigana

**Kuromoji** is the default morphological parser used. https://www.atilika.org/

**Sudachi** is also supported as an alternative morphological parser. https://github.com/WorksApplications/Sudachi

**RubySpan** for displaying furigana in Android as ruby characters using ReplacementSpan. https://github.com/mljli/rubyspan

**Kanatools** for kana text conversion. https://github.com/mariten/kanatools-java

# Other Dependencies
**Jisho.org** is used to supply the app with dictionary information. The app makes use of an API developed by Jisho to allow keyword search in English, Japanese, or Romaji.

**Aozora Bunko** is used for the app's reading material. Aozora is a collection of over 15 thousand copy right expired Japanese works. OsusumeSan accesses the reading material through the API provided by Ken Sato at https://github.com/aozorahack/pubserver2.

**jReadability's** algorithm for scoring Japanese text difficulty was recreated and used for scoring the difficulty of text from Aozora Bunko. This algorithm has been published by its authors many times. The following paper thoroughly details their work. Hasebe, Y., Lee, J., & Bekeš, A. (2019). Readability measurement of Japanese texts based on leveled corpora. *The Japanese Language from an Empirical Perspective. Corpus-based Studies and Studies on Discourse*, 143-167. Please see https://jreadability.net/ for further details.

# Scripts
Several python jupyter notebooks were used for creating sqlite databases for use in the android app

JMDictDBCreator: converts the txt file version of jmdictfurigana to a sqlite DB

KnowledgeDBCreator: takes input files of vocabulary from textbooks/JLPT levels and created a sqlite DB for holding the users known/unknown words. The DB is used throughout the android app to track the users known words.

Aozora_extract: queries the API's from https://github.com/aozorahack/pubserver2 to get article content from Aozora Bunko. The article content is then graded according to the linear regression formula from Lee and Hasebe at http://jhlee.sakura.ne.jp/papers/lee-et-al2016rb.pdf. The python code implements custom logic to extract features from the text using a heuristic. These features include the number of Kango (Chinese origin), Wago (Japanese origin), verbs, auxillary, and sentence length. These stats are the input to the linear regression. The rules for extracting Kango and Wago are based on loose grammatical and phonetic concepts. The code works well to extract the words but there are many edge cases and the extraction is not guaranteed to be 100% correct.

#Resources used for creating the app's vocab list by book/resource

JLPT Vocab CSV's taken from https://gist.github.com/nakaly/c2c855766ce1661e11396b09760b9598

Genki 1 & 2 vocab gathered from  http://genki.japantimes.co.jp/resources/saku_tango

Tobira vocab was taken from the anki desks at https://tobiraweb.9640.jp/contents/%e6%bc%a2%e5%ad%97%e3%83%bb%e8%aa%9e%e5%bd%99%e6%95%99%e6%9d%90/
