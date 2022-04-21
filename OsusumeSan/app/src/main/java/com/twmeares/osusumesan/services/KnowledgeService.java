package com.twmeares.osusumesan.services;
// This code taken from stackexchange
// https://stackoverflow.com/questions/9109438/how-to-use-an-existing-database-with-an-android-application

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.twmeares.osusumesan.models.KnowledgeItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class KnowledgeService extends SQLiteOpenHelper{
    // Data Base Name.
    private static final String DATABASE_NAME = "knowledge.db";
    //The Android's default system path of your application database.
    private static String DB_PATH;
    // Data Base Version.
    private static final int DATABASE_VERSION = 1;
    private final String TAG = "KnowledgeService";

    private static KnowledgeService instance;

    public Context context;
    static SQLiteDatabase sqliteDataBase;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     * Parameters of super() are    1. Context
     *                              2. Data Base Name.
     *                              3. Cursor Factory.
     *                              4. Data Base Version.
     */
    public KnowledgeService(Context context) {
        super(context, DATABASE_NAME, null ,DATABASE_VERSION);
        this.context = context;
        DB_PATH = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }

    public static KnowledgeService GetInstance(Context context) throws IOException {
        if (instance == null){
            instance = new KnowledgeService(context);
            instance.createDataBase();
        }

        if (instance.sqliteDataBase == null) {
            instance.openDataBase();
        }

        return instance;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * By calling this method and empty database will be created into the default system path
     * of your application so we are gonna be able to overwrite that database with our database.
     * */
    public void createDataBase() throws IOException{
        //check if the database exists
        boolean databaseExist = checkDataBase();

        if(databaseExist){
            // Do Nothing.
        }else{
            this.getWritableDatabase();
            copyDataBase();
        }// end if else dbExist
    } // end createDataBase().

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    public boolean checkDataBase(){
        File databaseFile = new File(DB_PATH + DATABASE_NAME);
        return databaseFile.exists();
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring byte stream.
     * */
    private void copyDataBase() throws IOException{
        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DATABASE_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DATABASE_NAME;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        //transfer bytes from the input file to the output file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    /**
     * This method opens the data base connection.
     * First it create the path up till data base of the device.
     * Then create connection with data base.
     */
    public void openDataBase() throws SQLException{
        //Open the database
        String myPath = DB_PATH + DATABASE_NAME;
        sqliteDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     * This Method is used to close the data base connection.
     */
    @Override
    public synchronized void close() {
        if(sqliteDataBase != null)
            sqliteDataBase.close();
        super.close();
    }

    /**
     * Check if a word is known or unkown to the user. default will be false for unknown words
     */
    public Boolean IsKnown(String word){
        String query = String.format("select isknown From knowledge where word = '%s'",
                word);
        Cursor cursor = sqliteDataBase.rawQuery(query, null);
        Boolean isKnown = false;
        if(cursor.getCount()>0){
            if(cursor.moveToFirst()){
                do{
                    isKnown = cursor.getInt(0) == 1 ? true : false;
                }while (cursor.moveToNext());
            }
        }
        cursor.close();
        return isKnown;
    }

    /**
     * Get the isknown value for the words in the list.
     */
    public HashMap<String, Boolean> GetIsKnownForWordsList(ArrayList<String> wordList){
        String words = String.join("', '", wordList);
        words = "'" + words + "'";
        String query = String.format("select isknown, word From knowledge where word in (%s)",
        words);
        Cursor cursor = sqliteDataBase.rawQuery(query, null);
        HashMap<String, Boolean> outputMap = new HashMap<>();
        if(cursor.getCount()>0){
            if(cursor.moveToFirst()){
                do{
                    outputMap.put(cursor.getString(1), cursor.getInt(0) == 1 ? true : false);
                }while (cursor.moveToNext());
            }
        }
        cursor.close();
        return outputMap;
    }

    /**
     * return the list of known/unknown words for the user.
     */
    public List<KnowledgeItem> GetKnowledge(){
        String query = String.format("select isknown, word From knowledge order by word");
        Cursor cursor = sqliteDataBase.rawQuery(query, null);
        List<KnowledgeItem> knowledgeList = new ArrayList<>();
        if(cursor.getCount()>0){
            if(cursor.moveToFirst()){
                do{
                    boolean isKnown = cursor.getInt(0) == 1 ? true : false;
                    String word = cursor.getString(1);
                    knowledgeList.add(new KnowledgeItem(isKnown, word));
                }while (cursor.moveToNext());
            }
        }
        cursor.close();
        return knowledgeList;
    }

    /**
     * Get percentage of known words from query list
     */
    public Float GetPercentKnown(JSONObject wordList){
        Float percentKnown = 0f;
        try {
            JSONArray verbList = wordList.getJSONArray("verb");
            JSONArray kangoList = wordList.getJSONArray("kango");
            JSONArray wagoList = wordList.getJSONArray("wago");
            int verbLength = verbList.length();
            int kangoLength = kangoList.length();
            int wagoLength = wagoList.length();
            Float totalWords = Float.valueOf(verbLength + kangoLength + wagoLength);
            String inClause = (verbLength > 0 ? verbList.toString() + "," : "")
                    + (kangoLength > 0 ? kangoList.toString() + "," : "")
                    + (wagoLength > 0 ? wagoList.toString() : "");
            inClause = inClause.replace("[", "").replace("]", "").replace("\"", "'");
            String query = String.format("select count(*) From knowledge where isknown = 1 and word in (%s)", inClause);
            Cursor cursor = sqliteDataBase.rawQuery(query, null);
            int countKnown = 0;
            if(cursor.getCount()>0){
                if(cursor.moveToFirst()){
                    do{
                        countKnown = cursor.getInt(0);
                    }while (cursor.moveToNext());
                }
            }
            cursor.close();
            percentKnown = (countKnown / totalWords) * 100f;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return percentKnown;
    }

    /**
     * mark a given word as known/unknown to update knowledge model.
     */
    public void UpdateKnowledge(String word, Boolean isKnown){
        try {
            //insert/replace to handle both new and old words.
            String query = String.format("insert or replace into knowledge (word, book, jlptlvl, isknown) " +
                            "values ('%s', " +
                            "(select book from knowledge where word = '%s'), " +
                            "(select jlptlvl from knowledge where word = '%s'), " +
                            "%d)",
                        word, word, word, (isKnown ? 1: 0));
            sqliteDataBase.execSQL(query);
        } catch (Exception ex){
            String msg = ex.getMessage();
            Log.e(TAG, msg);
        }
    }

    /*
     * Set isKnown value for all words in the book.
     */
    public void UpdateKnowledgeBook(String book, Boolean isKnown){
        try {
            ContentValues cv = new ContentValues();
            cv.put("isknown", (isKnown ? 1: 0));
            book = "%" + book + "%";
            int rowsAffected = sqliteDataBase.update("knowledge", cv, "book like ?", new String[]{book});
            Log.d(TAG, String.valueOf(rowsAffected) + " rows updated.");
        } catch (Exception ex){
            String msg = ex.getMessage();
            Log.e(TAG, msg);
        }
    }

    /*
     * Set isKnown value for all words in the JLPT level.
     */
    public void UpdateKnowledgeJLPT(String jlptLvl, Boolean isKnown){
        try {
            jlptLvl = "%" + jlptLvl + "%";
            ContentValues cv = new ContentValues();
            cv.put("isknown", (isKnown ? 1: 0));
            int rowsAffected = sqliteDataBase.update("knowledge", cv, "jlptlvl like ?", new String[]{jlptLvl});
            Log.d(TAG, String.valueOf(rowsAffected) + " rows updated.");
        } catch (Exception ex){
            String msg = ex.getMessage();
            Log.e(TAG, msg);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // No need to write the create table query.
        // As we are using Pre built data base.
        // Which is ReadOnly.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No need to write the update table query.
        // As we are using Pre built data base.
        // Which is ReadOnly.
        // We should not update it as requirements of application.
    }
}