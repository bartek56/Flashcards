package com.example.bartosz.fiszki.DataBase.SQLite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by bartosz on 26.02.17.
 */

public abstract class Database extends SQLiteOpenHelper {

    private String file;
    private static final String TAG = "Database";

    public Database(Context context, String FILENAME) {
        super(context, FILENAME,null,1);
        file = FILENAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try
        {
            db.execSQL(
                    "create table flashcard(" +
                            "id integer primary key autoincrement," +
                            "engWord text," +
                            "plWord text," +
                            "engSentence text," +
                            "plSentence text);" +
                            "");

            db.execSQL(
                    "create table inne(" +
                            "id integer primary key autoincrement," +
                            "idFlashcard integer,"+
                            "known boolean);"+
                            "");

            Log.d(TAG,"created tables "+file);

        }
        catch (SQLiteException ex)
        {
            Log.e(TAG,"onCreate "+" "+file+" "+ ex);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
