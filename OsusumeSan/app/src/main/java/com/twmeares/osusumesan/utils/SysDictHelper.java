package com.twmeares.osusumesan.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import android.content.Context;

import com.worksap.nlp.sudachi.Config;
import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Settings;


public class SysDictHelper {
        // Data Base Name.
        private static final String FILE_NAME = "system_small.dic";
        //static final String COMMON_SETTINGS = "{\"systemDict\":\"system.dic\",\"oovProviderPlugin\":[{\"class\":\"com.worksap.nlp.sudachi.SimpleOovProviderPlugin\",\"oovPOS\":[\"名詞\",\"普通名詞\",\"一般\",\"*\",\"*\",\"*\"],\"leftId\":8,\"rightId\":8,\"cost\":6000}],\"userDict\":[";
        private static final String SETTINGS = "{\"systemDict\":\"" + FILE_NAME + "\"}";
        //The Android's default system path of your application database.
        private static String FILE_PATH;
        // Data Base Version.
        private static final int DATABASE_VERSION = 1;
        // Table Names of Data Base.
        static final String TABLE_Name = "tableName";

        public Context context;


        /**
         * Constructor
         * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
         * @param context
         * Parameters of super() are    1. Context
         *                              2. Data Base Name.
         *                              3. Cursor Factory.
         *                              4. Data Base Version.
         */
        public SysDictHelper(Context context) {
            this.context = context;
            //DB_PATH = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
            FILE_PATH = context.getFileStreamPath(FILE_NAME).getAbsolutePath().replace(FILE_NAME, "");
        }

        /**
         * Creates a empty database on the system and rewrites it with your own database.
         * By calling this method and empty database will be created into the default system path
         * of your application so we are gonna be able to overwrite that database with our database.
         * */
        public void createDataBase() throws IOException {
            //check if the database exists
            boolean databaseExist = checkDataBase();

            if(databaseExist){
                // Do Nothing.
            }else{
                copyDataBase();
            }// end if else dbExist
        } // end createDataBase().

        /**
         * Check if the database already exist to avoid re-copying the file each time you open the application.
         * @return true if it exists, false if it doesn't
         */
        public boolean checkDataBase(){
            File databaseFile = new File(FILE_PATH  + FILE_NAME);
            return databaseFile.exists();
        }

        /**
         * Copies your database from your local assets-folder to the just created empty database in the
         * system folder, from where it can be accessed and handled.
         * This is done by transferring byte stream.
         * */
        private void copyDataBase() throws IOException{
            //Open your local db as the input stream
            InputStream myInput = context.getAssets().open(FILE_NAME);
            // Path to the just created empty db
            String outFileName = FILE_PATH + FILE_NAME;
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

        public Dictionary getDictionary(){
            try{
                //Config config = Config.fromSettings(Settings.parseSettings(FILE_PATH, SETTINGS));
                Config config = Config.fromClasspath();
                Path path = Paths.get(FILE_PATH + FILE_NAME);
                config.systemDictionary(path);
                Dictionary dict = new DictionaryFactory().create(config);
                //Dictionary dict = new DictionaryFactory().create(FILE_PATH, SETTINGS);
                return dict;
            } catch (IOException ex){
                return null;
            }
        }
    }