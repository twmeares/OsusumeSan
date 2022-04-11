package com.twmeares.osusumesan.services;
// This code taken from stackexchange
// https://stackoverflow.com/questions/9109438/how-to-use-an-existing-database-with-an-android-application

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.twmeares.osusumesan.models.Article;

import org.json.JSONException;
import org.json.JSONObject;

public class AozoraStatsHelper extends SQLiteOpenHelper{
    // Data Base Name.
    private static final String DATABASE_NAME = "aozoraStats.db";
    //The Android's default system path of your application database.
    private static String DB_PATH;
    // Data Base Version.
    private static final int DATABASE_VERSION = 1;
    private final String TAG = "AozoraStatsHelper";

    private static AozoraStatsHelper instance;

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
    public AozoraStatsHelper(Context context) {
        super(context, DATABASE_NAME, null ,DATABASE_VERSION);
        this.context = context;
        DB_PATH = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }

    public static AozoraStatsHelper GetInstance(Context context) throws IOException {
        if (instance == null){
            instance = new AozoraStatsHelper(context);
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
     * fetch the unique word list for the given book id.
     */
    public JSONObject getUniqueWords(int bookId){
        String query = String.format("select wordlist From aozoraStats where bookid = %d",
                bookId);
        Cursor cursor = sqliteDataBase.rawQuery(query, null);
        JSONObject uniqueWords = null;
        if(cursor.getCount()>0){
            if(cursor.moveToFirst()){
                do{
                    try {
                        uniqueWords = new JSONObject(cursor.getString(0));
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }while (cursor.moveToNext());
            }
        }
        cursor.close();
        return uniqueWords;
    }

    public JSONObject getUniqueWords(String bookId){
        try{
            int bookIdInt = Integer.parseInt(bookId);
            return getUniqueWords(bookIdInt);
        }
        catch (NumberFormatException ex){
            Log.e(TAG, bookId + " is not a proper bookId.");
            return null;
        }
    }

    public List<Article> getAllBookDetails(){
        String query = String.format("select bookid, title, authors, difficulty From aozoraStats order by difficulty desc");
        Cursor cursor = sqliteDataBase.rawQuery(query, null);
        List<Article> articleList = new ArrayList<>();
        if(cursor.getCount()>0){
            if(cursor.moveToFirst()){
                do{
                    try {
                        String bookId = cursor.getString(0);
                        String title = cursor.getString(1);
                        String authors = cursor.getString(2);
                        Double difficulty = cursor.getDouble(3);
                        articleList.add(new Article(title, difficulty, authors, bookId));
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }while (cursor.moveToNext());
            }
        }
        cursor.close();
        return articleList;
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